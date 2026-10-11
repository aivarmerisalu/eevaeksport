
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;



import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Product;
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

public class HansaFitnImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(HansaFitnImportDriver.class.getName());

  public static final String SEPARATOR = "\t";
  public static final String PRODUCT_FILE = "tooted.TXT";
  public static final String PRODUCT_PRICES_FILE = "hinnad.TXT";
  public static final String AGENT_FILE = "agent.TXT";
  public static final String CUSTOMER_FILE = "kliendid.TXT";
  public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
  public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.TXT";
  public static final String CUSTOMER_DEBT_FILE = "volg.txt";
  public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
  
  public HansaFitnImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    getProducts(null);
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   //   con = DriverManager.getConnection(url, "mobile","mobile");

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);

    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  private TreeMap prods = new TreeMap();


  	  private Hashtable dates = new Hashtable();
  	  
  	  public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	     StringBuffer buf = new StringBuffer();
  	     try {
  	     	
  	     //	this.getPriceMatrixData();
  	     	
  	    // 	TreeMap sto  = getStocks();
  	     //	TreeMap cost  = getCosts();
  	     	
  	     	
  	     	
  	       FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
  	       BufferedReader reader = new BufferedReader(fr);
  	     String line = null;
  	     while((line = reader.readLine()) != null){
  	     	
  	     	if(line.indexOf("artikel4") != -1){
  	     		 while((line = reader.readLine()) != null && line.indexOf("ITVc1") == -1){
  	     		
  	     		 line = line.replaceAll("\t","; ");
  	     		//System.out.println(line);
  	     		if(line.length()>100){
  	     		
  	       StringTokenizer st = new StringTokenizer(line, ";");
  	       if (st.hasMoreTokens()){
  	    	   st.nextToken();st.nextToken();st.nextToken();
  	         String id = st.nextToken().trim();
  	         Product prod = new Product(id);
  	         String out = "";
  	         String name = "";
  	         if (st.hasMoreTokens()){
  	        	 name = st.nextToken().trim();
  	           prod.setName(name);
  	           if(id.equals("106967")){
  	     
  	           }
  	     
  	           if (st.hasMoreTokens()){
  	           	String unit = st.nextToken().trim();
  	           	if(unit.length()>0){
  	           		prod.setUnit(unit);
  	           	}
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	            if (st.hasMoreTokens()){
  	            	String pr = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
  	            	if(pr.length() == 0){
  	            		pr = "0";
  	            	}
  	                double price = Double.parseDouble(pr);
  	                if (price > 0){
  	                  prod.setPrice(price);}
  	               //   System.out.println("price" + prod.getPrice());

  	               	st.nextToken();
  	               	String gr = st.nextToken().trim();
  	               	if (gr.length()>0){
  	               		prod.setGroup(gr);
  	               	}
  	               	
  	               	
  	               	if(hinnad.get(prod.getID()) != null){
  	               	//	prod.setPrice(Double.parseDouble((String) hinnad.get(prod.getID())));
  	               	}
  	               	
  	           	String ii = "";

  	           	
  	           	
  	           	ii =	st.nextToken();
  	           	ii =st.nextToken();      
  	           	ii =st.nextToken();
  	          // 	System.out.println("ii" + ii);
  	           	String cos = ii.trim().replaceAll(",",".").replaceAll(" ","");
  	           	if(cos.length() == 0){
  	        		cos = "0";
  	        	}
  	           	 double cost = Double.parseDouble(cos);
  	           	 
  	           	 double marg = (1-cost/prod.getPrice())*100 ;
  	           	 if(marg >0){
  	           	 	 prod.setMargin((int) marg + "");
  	                //	System.out.println("price" + prod.getPrice() + " " + (int)marg + " " + cos );
  	                	 
  	           	 }
  	          
  	           	String sortcode =  st.nextToken();
  	           	if(sortcode != null && sortcode.trim().length() > 0 ){
  	           		prod.setSorting(sortcode);
  	           	}
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken(); 
  	           	ii =	st.nextToken();
  	           	String ean2 =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();

  	           	String ean = st.nextToken().trim();
  	           	if(ean.trim().length()>0){
  	           		prod.setEAN(ean);
  	           	}
  	         	if(ean2.trim().length()>0){
  	           		prod.setEAN(ean2.trim());
  	           	}

  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	    out=   	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	            st.nextToken();
  	          	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	          
  	             if (st.hasMoreTokens()){
  	             	String ca = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
  	             	if(ca.trim().length()==0){
  	             		ca = "0";
  	             	}
  	                 double inCases = Double.parseDouble(ca);
  	                 if (inCases > 0){
  	                   prod.setInCases(inCases);
  	               //System.out.println("CAse" + prod.getInCases());
  	             }
  	              }
                  }
  	           }
  	         }
  	    
  	         if(prod.getName().length()>0){
  	         	if( !prod.getName().startsWith("-") && !out.trim().equals("1") && id.length()>3){
  	                 tm.put(prod.getName(), prod);
  	        	     prods.put(prod.getID(), prod);
  	         	}
  	         }
  	            
  	       }
  	     		 }	
  	      		}
  	     	}
  	     }
  	     } catch (IOException e){
  	       e.printStackTrace();
  	     }
  	 
  	     
  	     return tm;
  	   }
  	   
  	   
  private String getDir(){
    String dir = parent.getProps().getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
  }
   
  private HashMap payers = new HashMap();
 

  public String identifyCustomer(String id,String regnr) throws SQLException {
  
    String acc = null;
    return acc;

  }

 
  

  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    return null;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	   trouble = "Imoprdin tellimust " + order.getAgent() + "." + order.getDokNr() + ".txt aga SAP faili ei tekkinud ";
		
	 Order ord =  order;	
	 try{
		  java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
				 
		        public boolean equals(Object obj) {
		          return false;
		        }

		       public int compare(Object o1, Object o2) {
		          OrderLine ol1 = (OrderLine)o1;
		          OrderLine ol2 = (OrderLine)o2;
		          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
		        }
		           }); 
	 }catch(Exception r ){
		 log.error(r);
	 }
		
   		           ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    	   
      trouble = null;
      setNumAddedLines(counter);
  }

  

  
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+"").substring(2));
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }

  public static String getDateString(java.util.Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    String month = (cal.get(Calendar.MONTH) + 1) + "";
    if (month.length() == 1) month = "0" + month;
    String day = cal.get(Calendar.DAY_OF_MONTH) + "";
    if (day.length() == 1) day = "0" + day;
    String dateString = day + "." + month + "." + cal.get(Calendar.YEAR);
    return dateString;
  }

  public static String getTimeString(Calendar cal){
	  //  2013-08-07T22:54:19
	

	    String month = (cal.get(Calendar.MONTH) + 1) + "";
	    if (month.length() == 1) month = "0" + month;
	    String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	    if (day.length() == 1) day = "0" + day;
	    String dateString =  cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T" + cal.get(Calendar.HOUR_OF_DAY) + ":" +
	    cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
	    return dateString;
	  }

  

  
  private String lineSep = "\n";
  
  
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	 nf.setGroupingUsed(false);

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
		
			 String doknr = order.getDokNr();
	             

	         String ls = System.getProperty("line.separator");
	         String fixagent = order.getAgent();       
	     
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
	  
	      
	           String agent = order.getAgent();
	       
	           Calendar cal = Calendar.getInstance();
	          
	           if (shipDate != null) {
	                 cal = shipDate;
	               }
	         
	           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
	           sb.append("<document type=\"DISPATCH_ORDER\">" + ls);
	      
	           sb.append("<header>" + ls);
	           /*<header>
					<xmlIdentifier>c1345be238fbf0f6080df593cc22d4fe</xmlIdentifier>
					<timestamp>2013-08-07T22:54:19</timestamp>
					<partyId>ROBOCOP-WEB</partyId>
				</header>*/
	           
	           sb.append("<timestamp>" + escape(getTimeString(Calendar.getInstance())) + "</timestamp>" + ls);
	           sb.append("<xmlIdentifier>" + Calendar.getInstance().getTimeInMillis() + "</xmlIdentifier>" + ls);
	           sb.append("<partyId>ROBOCOP-MS</partyId>" + ls); 	
               sb.append("</header>" + ls);
	          
               sb.append("<orders>" + ls);
               sb.append("<order>" + ls);
               
               String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
               
	           sb.append("<orderDate>" +escape(getDateString(Calendar.getInstance())) + "</orderDate>" + ls);
	           sb.append("<orderNo>" + escape(agent + "-" + sss )+ "</orderNo>" + ls);
	           sb.append("<orderType>STANDARD</orderType> " + ls);
	          
	           sb.append("<deliveryDocumentation>" + ls);
               sb.append("<deliveryDocument>" + ls);
                 sb.append("<type>SAATELEHT</type>");
                 sb.append("<documentNo>" +  escape(agent + "-" + sss ) + "</documentNo>");
                 sb.append("<printQty>3</printQty>");
	           sb.append("</deliveryDocument>" + ls);
	           sb.append("</deliveryDocumentation>" + ls);
	           
	           
	           sb.append("<customer>" + ls);
                 sb.append("<customerNo>" + escape(custID) + "</customerNo>");
                 sb.append("<name>" + escape(custID) + "</name>");
               sb.append("</customer>" + ls);
	           
/*	                   
									<courier>
							<deliveryAddress>
								<deliveryName>Kustas KĆµiv</deliveryName>
								<deliveryAddress>Kaare tee 3</deliveryAddress>
								<deliveryCity>Viimsi</deliveryCity>
								<deliveryZip>74010</deliveryZip>
								<deliveryCounty>Harjumaa</deliveryCounty>
								<deliveryCountry>EE</deliveryCountry>
								<deliveryEmail>kustas.koiv@gmail.com</deliveryEmail>
								<deliveryPhone>+372 5131364</deliveryPhone>
								<deliveryMobile>+372 5131364</deliveryMobile>
							</deliveryAddress>
							<Itella type="SMARTPOST">
								<placeID>125</placeID>
								<deliveryName>Kustas KĆµiv</deliveryName>
								<notificationEmail>kustas.koiv@gmail.com</notificationEmail>
								<notificationSMS>+372 5131364</notificationSMS>
							</Itella>
						</courier>*/
	           
               
	           if(ref != null && ref.trim().length() > 0){
	         	  sb.append("<RefInfo>" + ls);
	               sb.append("<SourceDocument type=\"order\">" + ls);
	               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
	               sb.append("</SourceDocument>" + ls);
	               sb.append("</RefInfo>" + ls); 
	           }

	           sb.append("   <orderLines>" + ls);
    
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
	               
	               String nimetus = "";
	               if(prods.get(line.getToode()) != null){
	               	nimetus = ((Product)prods.get(line.getToode())).getName();
	               }
	               sb.append("      <orderLine>" + ls);
	               sb.append("      <lineNo>"  + (i+1) + "</lineNo>" + ls);
	               sb.append("      <orderQty>" + escape(getNF().format(line.getKogus())) + "</orderQty>" + ls);
	               sb.append("      <article>" + ls);
	               sb.append("           <articleCode>" + escape( line.getToode() ) + "</articleCode>" + ls);
	               sb.append("           <name>" + escape( nimetus ) + "</name>" + ls);
	               sb.append("      </article>" + ls);
	               sb.append("      </orderLine>" + ls);
	             }
                   sb.append("   </orderLines>" + ls);
	           
	     	  /* String comm = order.getInfo();
	             if(comm != null && comm.length()>100){
	             	comm = comm.substring(0,100);
	             }
	             
	             if(comm != null && comm.trim().length() > 0 ){
	             	  sb.append("   <AdditionalInfo>" +ls);
	                   sb.append("       <Extension extensionId=\"internalremarks\">" + ls);
	                   sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
	                   sb.append("       </Extension>");
	                     //-<Extension extensionId="externalremarks"><InfoContent>Käesoleva arve aluseks olev nõue on loovutatud AS-ile SEB Liising ja kõik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu käesoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
	                   sb.append("   </AdditionalInfo>" + ls);	
	             }*/
	           
	             sb.append("</order>" + ls);
	           sb.append("</orders>" + ls);
	           sb.append("</document>" + ls);
	 
	      counter = counter + cou;
	    	//
	      String fn = doknr;
	      if(doknr.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
	      System.out.println(fileName);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        fos.write(sb.toString().getBytes());
	        fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
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

  
 private void makeOutputHansa(Order order){

     StringBuffer sb = new StringBuffer();
     Calendar cal = Calendar.getInstance();
     Calendar cal2 = Calendar.getInstance();
    
try{
	 if (order.getPaymTerm() > 0) {
	        cal.add(Calendar.DAY_OF_MONTH,order.getPaymTerm());
	      }else {
	        cal.add(Calendar.DAY_OF_MONTH, 1);
	       }
}catch(Exception e){

}


    // sb.append("compnr	1\t" + lineSep);
     sb.append("format\t" + lineSep);
     sb.append("1\t44\t1\t0\t0\t" + lineSep + lineSep  );//viimane 1 on ANSI kui 0 siis UTF
     sb.append("codepage\tUTF-8" + lineSep  + lineSep);
     
     sb.append("commentstring\t" + lineSep);
     sb.append("\t" + lineSep );
     sb.append( "\n" );
     sb.append("ORVc" + lineSep);

     String kogusumma = "0";
     String kmsumma = "0";
     String km = "=";
     String laokood = "L";
     String objekt = "";
     String myygikonto = "1310";
     String valuuta = "";
     String hinnakiri = "H3";
     String muudaladu = "1";

     String addr = "";
    String indeks = "";
    String linn = "";
    String tanav = "";

    if(addr.indexOf("|") != -1){
   	 tanav = addr.substring(0,addr.indexOf("|"));
   	 linn = addr.substring(addr.indexOf("|")+1);
    }

    if(linn.indexOf("|")!= -1){
   	 indeks =linn.substring(linn.indexOf("|")+1);
   	 linn = linn.substring(0,linn.indexOf("|"));
    }



    String juraddr = order.getInfo();
    String jurindeks = "";
    String jurlinn = "";
    String jurtanav = "";
 

  
  	  laokood = "";
  	  objekt = "";
  	  hinnakiri = "AS";
  	  myygikonto = "10311";
  	  valuuta = "EUR";
  	  muudaladu = "0";

  	  String klnimi = "";
  	  String address = "";
  
//340773	06.02.2013	1836	KAUBABUSS OÜ	Ridaelamu 5-6, Maaritsa, Põlvamaa	Tele. 56 231248				
//0	2	0	0	S		18	0			0		MAR	MAR						EUR			0		0			
//0						0	0,00	30,04		6,01	36,05	0	1			15				0	36,05
//1	15,6466			4,64				15,5					54,000	0,000	2,352		36,05
  	  //1	0	0						0	0		0	0	

  	  sb.append(order.getDokNr() + "\t" + getDateString(cal2) + "\t" + order.getCompany() + 
  			  "\t" + klnimi +"\t" + address + "\t				0	2	0	0	S		18	0			0		" + order.getAgent()+ "\t"+ order.getAgent()+ "\t					EUR			0		0			0						0	0,00	30,04		6,01	36,05	0	1			15				0	36,05			1	15,6466			4,64				15,5					54,000	0,000	2,352		36,05													1	0	0						0	0		0	0	");
  	  
   /*  sb.append(order.getDokNr() + "\t" + getDateString(cal2) + "\t" + order.getCompany() +  "\t" + getDateString(cal)+ "\t"

                     + getDOM().getTellijaNimi() + "\t" + jurtanav + "\t" + jurlinn + "\t" + jurindeks + "\t\t\t0\t" + getDOM().getMaksetahtaeg() +  "\t\t0\t0\t" //viimane null on kinnitus
                     + getDOM().getMaksetahtaeg() +"\t0,000\t0\t" + hinnakiri + "\t\t\t1\t0\t\t" + objekt + "\t0\t" + myygikonto + "\t\t\t\t" +  getDOM().getAgendinimi() + "\t\t"
                       + getDateString(cal2) + "\t" + valuuta + "\t\t" + muudaladu +"\t\t\tAIVARM\t\t\t\t\t\t\t0,00\t\t0,00\t"
                       + kogusumma + "\t\t" + km + "\t" + kmsumma + "\t\t\t" + getDOM().getTellijaNimi() + "\t\t\t\t\t" + laokood + "\t\t\t\t1\t\t"
                       + "16 960 000 010 140\t\t\t0,00\t0\t\t0\t" + kmsumma  + "\t\t\t" + rate1+ "\t" + rate2 + "\t\t\t\t"
                       + "0,00\t0,00\t0\t\t\t\t\t0\t\t0\t0\t\t0\t\t\t5,000\t0,074\t0,000\t\t" + kmsumma
                       + "\t\t" + addr + "\t\t\t\t0\t\t0\t0\t\t0\t\t\t\t01:01:01\t\t\t0\t\t0\t0\t\t\t"  +  getDateString(cal2)
                       + "\t\t\t\t\t\t0\t0\t\t\t"   +  lineSep);



    */
    
     
     Iterator iter = order.getOrderLines().iterator();
     while(iter.hasNext()){
       OrderLine line = (OrderLine)iter.next();
//1	87121	22	v L MI make	120,00	1 700,16	35,6	300100	397		30,82	1 022,04		1										TK

       double kate = (line.getPrice()*line.getKogus())*(100-line.getDiscount())/100-20*line.getKogus();
System.out.println("Kate " + kate + " " + line.getPrice()*line.getKogus() + " " + line.getDiscount()  + " " + this.getNumAddedLines() );
String kmkood = "3";

String nimetus = "";
if(prods.get(line.getToode()) != null){
	nimetus = ((Product)prods.get(line.getToode())).getName();
}

       sb.append("1\t" + line.getToode()  + "\t"+ ((line.getKogus()+"").replace('.',','))+
                       "\t" + ((line.getPrice()+"").replace('.',',')) + "\t" +
                       (((line.getPrice()*line.getKogus())+"").replace('.',',')) + "\t0" //+ (line.getDiscount()+"").replace(',','.')
                               + "\t40121" + "\t"  + "\t\t"
                        + ((line.getKogus()+"").replace('.',','))
                                + "\t" +"\t" + ("") // ((kate+"").replace('.',','))
                                + "\t " + nimetus + "\t" + kmkood + "\t\t\t\t\t\t" + ((20*line.getKogus())+"").replace('.',',')
                                + "\t\t\t\t\t" + line.getUnit() + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t1,65\t\t\t\t\t\t\t0\t"
                +  lineSep);
      counter++;
     }
     //(line.getDiscount()+"").replace('.',',')  +   "\t\t"
      sb.append(lineSep);


     Calendar cal1 = Calendar.getInstance();
     String ss = (cal1.get(Calendar.SECOND)+"");
     if(ss.length()>1){
             ss = ss.substring(0,1);
     }
     String fileName = (parent.getProps().getProperty("conv.dir") + order.getAgent())  + "." + order.getDokNr()  +  ".txt";

    try{

        OutputStream os = new FileOutputStream(fileName);
                OutputStreamWriter osw = new OutputStreamWriter(os);
              
                osw.write(sb.toString());
                osw.close();
                os.close();


    }catch(Exception e){
    	e.printStackTrace();
    }
    
 }
 

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	private HashMap	hinnad = new HashMap();
	
	  
 
 
  public String getDriverVersion(){
    return "HansaFitnImport";
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



  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
