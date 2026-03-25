
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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

public class MantingaCSVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MantingaCSVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
    return null;
  }

 
  public String identifyCustomerAGENT(String id) throws SQLException {
   return null;
 }
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }


  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }
  
  
  public List<String[]> readCSVData(String fname) {
      String csvFile = getDir() + fname; // Adjust getDir() accordingly
      CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
      List<String[]> data = new ArrayList<>();

      try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
   		   
              .withCSVParser(parser)
              .build()) {
          String[] nextLine = reader.readNext();// esimest rida ei loe
          while ((nextLine = reader.readNext()) != null) {
              data.add(nextLine); // Process each line without loading everything at once
          }
      } catch (IOException | CsvException e) {
          e.printStackTrace();
      }
      return data;
  }
  
  public TreeMap getCustomers(){


 TreeMap tm = new TreeMap();

 try {
     for (String[] row : readCSVData("Merisoft_MasterData_Customers.csv")) {
    	 int cou = 0;
    	 String id = null;
    	 String name = null;
    	 String name2 = null;
    	 String address = null;
    	 String contact = null;
    	 String payer = null;
    	 String group = null;
    	 String group2 = null;
    	 String chain = null;
    	 String visit = null;
    	 String age = null;
    	 String age2 = null;
    	 Integer block = null;
    	 Integer payerblock = null;
    	 Integer term = null;
    	 
         for (String cell : row) {

        	 cell = cell.replaceAll("\"", " ").trim();
        	 
        	 if(cou == 0) {
                 name = cell.trim();
                 name = name.replaceAll("'", " ").trim();
                 name = name.replaceAll("  ", " ");
                 name2 = Converter.cleanLitLatEst(name);
             }
        	 
        	 if(cou == 1) {
                 id = cell.trim().replace(':', '_');
             }

        	 if(cou == 2) {
                 payer = cell.trim();
             }
        	 
        	 
        	 if(cou == 6) {
                 group = cell.trim();
             }
        	 
        	 if(cou == 7) {
                 address = cell.trim();
             }
        	 
        	 if(cou == 8) {
                 visit = cell.trim();
             }

        	 if(cou == 9) {
                 age = cell.trim();
             }
        	 
        	 if(cou == 10) {
                 age2 = cell.trim();
             }

        	 if(cou == 11) {
                 term = Integer.parseInt(cell.trim());
             }
        	 
        	 if(cou == 12) {
                 block = Integer.parseInt(cell.trim());
             }

        	 if(cou == 13) {
                 payerblock = Integer.parseInt(cell.trim());
             }
         	// ,"agent","teles","term","block","payerblock","info","grupp","chain","maxallow","ette","cuinfo","minam"
        	 
        	 if(cou == 14) {
                 contact = cell.trim();
             }
        	 
        	 if(cou == 15) {
                 group2 = cell.trim();
             }
        	 
        	 if(cou == 16) {
                 chain = cell.trim();
             }


        	 if(cou == 17) {
               //  group = cell.trim();
             }

        
        	 
             cou++;
         }
         Customer  cust = new Customer(id);
         cust.setName( Converter.enCodeEst(name).trim() + " " + Converter.enCodeEst(address));
         cust.setAddress( Converter.adressLink(Converter.enCodeEst(address), ""));
         cust.restStreet = address;
         cust.restPerson = contact;
         
         if(block == 1) {
        	 cust.setSalesBlock(true);
         }
         
         if(payerblock == 1) {
        	 cust.setSalesBlock(true);
         }
         
         cust.setPersonalData( Converter.enCodeEst(contact));
         String setg = "";
         if(group != null && group.trim().length() > 0 ) {
        	 cust.setGroup(group);
             setg = group+ "|";
             
         }
         if(group2 != null && group2.trim().length() > 0 ) {
         	  cust.setGroup(setg + group2);
          }
         
         if(age != null && age.trim().length() > 0 ) {
        	 cust.addAgent(new Agent(age));
    	 }
         if(age2 != null && age2.trim().length() > 0 ) {
        	 cust.addAgent(new Agent(age2));
    	 }
          
         if(chain != null && chain.trim().length() > 0 ) {
        	  cust.setChain(chain);
         }
         if(payer != null && payer.trim().length() > 0 ) {
       	  cust.setPayer(payer);
        }
         
         if(visit != null && visit.trim().length() > 0 ) {
          	  cust.setVisit(visit);
           }
        
         if(term != null && term > 0) {
         	 cust.setPaymTerm(term+"");
         }
       
         
         tm.put(cust.getID(), cust);	
         
     }
 } catch (Exception e) {
     e.printStackTrace();
 }
 System.out.println("cust " + tm.toString());
 return tm;
}



  private TreeMap custs = null;
  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if(!order.getCompany().equals("OTHERSTUFF")){
		 // Vector splitted =  splitOrder(order);
	 	 // Enumeration en = splitted.elements();
	 	 try {
			makeEans();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 	 if(custs == null) {
	 		 custs = getCustomers();
	 	 }
	 	 
	 		  Order ord = order;
	 		 
	 		 	 Vector lin = ord.getOrderLines();
	      	 	 
	      	 	 boolean tagastus = false;
	      	 	 for (int i = 0; i < lin.size(); i++) {
	      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (ord.getStatus() == 5 || ord.getStatus() == 4)){
	      	 			tagastus = true;
	      	 		}
	      	 		if(ol.getKogus() < 0 ) {
	      	 			tagastus = true;
	      	 		}
	      	 	 }  	
	 			  
	  		 	  boolean repr = false;
	  		 	 	 for (int i = 0; i < lin.size(); i++) {
	  		 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	  		 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	  		 	 			repr = true;
	  		 	 			tagastus = false;
	  		 	 		}
	  		 	 	 } 
	 		 makeOutput(ord,tagastus, repr);
	   } else {
		  counter = order.getOrderLines().size();
	  }
		
	 
  }
  
    private HashMap eans = null;
    private HashMap prodnames = null;
    
    private void makeEans() throws Exception{
  	 
  	  	if(eans == null){
  	  		eans = new HashMap();
  	  		prodnames = new HashMap();
  	  	
  	      for (String[] row : readCSVData("Merisoft_MasterData_Products.csv")) {
       	   
  	    	   int cou = 0;
  	         String id = null;
  	      	 String name = null;
  	      	 String ean = null;

  	     	   for (String cell : row) {

  	          	 cell = cell.replaceAll("\"", " ").trim();
  	         	 if(cou == 0) {
  	                   id = cell.trim();
  	             }
  	         	 if(cou == 1) {
  	                name = cell.trim().replaceAll("'", " ").trim().replaceAll("  ", " ");
  	             }
  	         	 if(cou == 5) {
  	                ean = cell.trim();
  	             }
  	              cou++;
  	          }
  	        
  	     	   if(ean != null) {
  	     		   eans.put(id, ean);
  	     	     }
  	     	     prodnames.put(id, name);
  	           }
  	     	 }
 		   
  	  	  //  	System.out.println("oioi " + eans.size() + " " + prodnames.size());
  	  	
  	  }
  

    
    private void makeOutput(Order order, boolean tagastus, boolean repr) {

		
if(prods == null) {
	this.getProducts();
}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element root = doc.createElement("Document-Order");
			doc.appendChild(root);

			// String filiaalStr = null;
			// String tarnijaKoodStr = null;

			Node docheader = doc.createElement("Document-Header");
			root.appendChild(docheader);

			Node un2 = doc.createElement("DocumentType");
			Text unVal2 = doc.createTextNode("MeriSoft");
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			Node un3 = doc.createElement("DocumentSource");
			Text unVal3 = doc.createTextNode("S");
			un3.appendChild(unVal3);
			docheader.appendChild(un3);
			try {

				long ll = Long.parseLong(order.getAgentCode());
				Node un4 = doc.createElement("DateCreated");

				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date(ll));

				String month = (cal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				String day = cal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;

				String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
				if (hour.length() == 1)
					hour = "0" + hour;
				String minute = cal.get(Calendar.MINUTE) + "";
				if (minute.length() == 1)
					minute = "0" + minute;

				String second = cal.get(Calendar.SECOND) + "";
				if (second.length() == 1)
					second = "0" + second;

				// [ HH:MM:SS]

				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "[" + hour + ":" + minute + ":"
						+ second + "]";

				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);

			} catch (Exception e) {

			}

			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
			}

			long uni = Calendar.getInstance().getTimeInMillis();
			Node header = doc.createElement("Order-Header");

			Node nr = doc.createElement("OrderNumber");
			Text nrVal = doc.createTextNode("MS-" + number + "");
			nr.appendChild(nrVal);
			header.appendChild(nr);

			Node age = doc.createElement("SalesAgent");
			Text ageVal = doc.createTextNode(order.getAgent());
			age.appendChild(ageVal);
			header.appendChild(age);

			String comm = "";
			if (order.getInfo() != null && order.getInfo().trim().length() > 0) {

				Node com = doc.createElement("Remarks");
				Text comVal = doc.createTextNode(order.getInfo());
				com.appendChild(comVal);
				header.appendChild(com);
				comm = order.getInfo();

				Node com2 = doc.createElement("comment");
				Text comVal2 = doc.createTextNode(order.getInfo());
				com2.appendChild(comVal2);
				header.appendChild(com2);

			}
			System.out.println("Remarks " + comm);

			Node kuupaev = doc.createElement("OrderDate");
			Calendar cal = Calendar.getInstance();

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			String year = cal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			String dateString = year + "-" + month + "-" + day;
			Text kuupaevVal = doc.createTextNode(dateString);
			kuupaev.appendChild(kuupaevVal);
			header.appendChild(kuupaev);

			Node curr = doc.createElement("OrderCurrency");
			Text currVal = doc.createTextNode("EUR");
			curr.appendChild(currVal);
			header.appendChild(curr);

			Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
			Calendar delcal = Calendar.getInstance();
			delcal.setTime(order.getShipCal().getTime());

			month = (delcal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			day = delcal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			year = delcal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			dateString = year + "-" + month + "-" + day;
			Text delkuupaevVal = doc.createTextNode(dateString);
			delkuupaev.appendChild(delkuupaevVal);
			header.appendChild(delkuupaev);

			Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
			Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
			expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
			header.appendChild(expectedDeliveryTime);

			root.appendChild(header);

			Node parties = doc.createElement("Document-Parties");
			Node seller = doc.createElement("DeliveryPoint");

			Node ise = doc.createElement("CodeBySupplier");
			Text nimiVal = doc.createTextNode(order.getCompany());
			ise.appendChild(nimiVal);
			Node ise2 = doc.createElement("ILN");
			Text nimiVal2 = doc.createTextNode(order.getCompany());
			ise2.appendChild(nimiVal2);

			seller.appendChild(ise);
			seller.appendChild(ise2);

			parties.appendChild(seller);

			Node receiver = doc.createElement("Receiver");

			Node iseR = doc.createElement("CodeBySupplier");
			Text nimiRVal = doc.createTextNode(order.getCompany());
			iseR.appendChild(nimiRVal);
			Node ise2R = doc.createElement("ILN");
			Text nimiVal2R = doc.createTextNode(order.getCompany());
			ise2R.appendChild(nimiVal2R);

			receiver.appendChild(iseR);
			receiver.appendChild(ise2R);

			parties.appendChild(receiver);

			Node buyer = doc.createElement("Buyer");

			Node iseX2 = doc.createElement("ILN");
			Text nimiXVal2 = doc.createTextNode(order.getCompany());
			iseX2.appendChild(nimiXVal2);

			Node iseX = doc.createElement("CodeByBuyer");
			Text nimiXVal = doc.createTextNode(order.getCompany());
			iseX.appendChild(nimiXVal);

			buyer.appendChild(iseX2);
			buyer.appendChild(iseX);

			parties.appendChild(seller);

			root.appendChild(parties);

			// RIDADE LISAMINE
			Node read = doc.createElement("Order-Lines");

			int coun = 0;
			Iterator iter = order.getOrderLines().iterator();
			while (iter.hasNext()) {
				OrderLine line = (OrderLine) iter.next();
				String toode = line.getToode();
				if (toode != null) {
					toode = toode.trim();
				} else {
					toode = "";
				}
				if (line.getKogus() != 0 && !toode.equals("")) {

					coun++;
					addLine(line, read, doc, coun, order);
				}
			}

			root.appendChild(read);

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim()
					+ ".xml";
			System.out.println(fileName);
			OutputFormat of = new OutputFormat();
			of.setIndenting(true);
			of.setLineSeparator(System.getProperty("line.separator"));
			of.setEncoding("ISO-8859-1");
			XMLSerializer ser = new XMLSerializer(of);
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter out = new PrintWriter(fos);
			ser.setOutputCharStream(out);
			ser.serialize(doc);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private TreeMap prods = null;
    
    
    public void getProducts(){
 	   
        prods = new TreeMap(); 
 	  try {
            for (String[] row : readCSVData("Merisoft_MasterData_Products.csv")) {
     	int cou = 0;
         String id = null;
       	 String name = null;
       	 String ean = null;
       	 
      

      	   for (String cell : row) {

           	 cell = cell.replaceAll("\"", " ").trim();
          	 if(cou == 0) {
                    id = cell.trim();
              }
          	 if(cou == 1) {
                 name = cell.trim().replaceAll("'", " ").trim().replaceAll("  ", " ");
              }
          	if(cou == 5) {
                 ean = cell.trim();
              }
          	   cou++;
           }
          
          
          Product prod = new Product(id);

              if(ean != null && ean.trim().length() > 4) {
                prod.setEAN(ean);
              }
              
              prod.setName(name);
             	   prods.put(prod.getID(), prod); 
            }
            
             }catch (Exception u){
         		u.printStackTrace();
         	}
            
    }


    
	private void addLine(OrderLine line, Node read, Document doc, int counter, Order order) throws SQLException {

		Node rida = doc.createElement("Line");
		read.appendChild(rida);

		Node ridaitem = doc.createElement("Line-Item");

		Node linenum = doc.createElement("LineNumber");
		Text linnum = doc.createTextNode(counter + "");
		linenum.appendChild(linnum);
		ridaitem.appendChild(linenum);


		
		Product pr = (Product)prods.get(line.getToode());
		String toode = line.getToode();

		Node btoote_kood = doc.createElement("BuyerItemCode");
		Text btoote_koodVal = doc.createTextNode(toode);
		btoote_kood.appendChild(btoote_koodVal);
		ridaitem.appendChild(btoote_kood);

		Node toote_kood = doc.createElement("SupplierItemCode");
		Text toote_koodVal = doc.createTextNode(toode);
		toote_kood.appendChild(toote_koodVal);
		ridaitem.appendChild(toote_kood);
		String ean = pr.getEAN();
		if(ean != null) {
			Node ean_kood = doc.createElement("EAN");
			Text ean_koodVal = doc.createTextNode(ean);
			ean_kood.appendChild(ean_koodVal);
			ridaitem.appendChild(ean_kood);		
		}
	

		String nimi = pr.getName();
		if (nimi != null) {
			Text nimetusVal = doc.createTextNode(nimi.replace('`', ' ').replace('´', ' '));
			Node nimetus = doc.createElement("ItemDescription");
			nimetus.appendChild(nimetusVal);
			ridaitem.appendChild(nimetus);
		}

		double kogus = line.getKogus();
		double price = line.getPrice();
		double disc = line.getDiscount();
		String unit = line.getUnit();

		Node koGus = doc.createElement("OrderedQuantity");

		Text kogusVal = doc.createTextNode(getNF().format(kogus));
		koGus.appendChild(kogusVal);
		ridaitem.appendChild(koGus);

		if (disc > 0) {
			// price = price * (100-disc)/100;
			Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
			Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
			yhiku_hind.appendChild(yhiku_hindVal);
			ridaitem.appendChild(yhiku_hind);
		}

		Node yhiku_va = doc.createElement("TaxRate");
		Text yhiku_vaVla = doc.createTextNode("21");
		yhiku_va.appendChild(yhiku_vaVla);
		ridaitem.appendChild(yhiku_va);

		Node yhik = doc.createElement("UnitOfMeasure");
		Text yhikVal = doc.createTextNode(unit);
		yhik.appendChild(yhikVal);
		ridaitem.appendChild(yhik);

		rida.appendChild(ridaitem);
		addNumLine();
	}

  
	private Vector splitOrder (Order order){
			  
		  Vector result = new Vector();

		  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
			  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
		  }
		  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

		  Order ordS2 = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordS4 = new Order(order.getCompany(),order.getAgentFirstName());
		  
		  ordS2.setAgentCode(order.getAgentCode());
		  ordS2.setDokNr(order.getDokNr());
		  ordS2.setCompName(order.getShipMode());
		  ordS2.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordS2.setStatus(order.getStatus()); 
		  ordS2.setInfo(order.getInfo());
		  ordS2.setDealer("order");

		  ordS4.setAgentCode(order.getAgentCode());
		  ordS4.setDokNr(order.getDokNr());
		  ordS4.setCompName(order.getShipMode());
		  ordS4.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordS4.setStatus(order.getStatus());
		  ordS4.setInfo(order.getInfo());
		  ordS4.setDealer("tester");

		  
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			  
			  double status = lin.getDiscount();

			    if(status > -2){
			     	ordS2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS2.setDokNr(order.getDokNr());
			     	ordS2.setReference(order.getReference());
			     }
			
			    if(status < -1){
			    	 ordS4.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     	ordS4.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "9");
				     	ordS4.setReference(order.getReference()); 
				     	ordS2.setInfo(" ");
			     }
			  
		  }
		  
		  if(ordS2.getOrderLines() != null && ordS2.getOrderLines().size()>0){
			  result.add(ordS2);  
		  }

		  if(ordS4.getOrderLines() != null && ordS4.getOrderLines().size()>0){
			  result.add(ordS4);  
		  }
	      
		  
		  
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }



	public NumberFormat getNF() {
		return this.nf;
	}
	
 
  public String getDriverVersion(){
    return "AxaDunkerImport";
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
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
