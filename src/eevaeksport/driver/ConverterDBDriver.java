package eevaeksport.driver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ee.merisoft.sm.Product;
import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ConverterDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,FixLoader,WebReporter {

  public ConverterDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
  public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getPilotDayData(Agent agent){
	return null;
         }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   
	return lines;

   }

   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           return lines;
   }

   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
      return null;
   }

       public String getSaveStockInfo(){
       return null;
       }



   public Vector getDebt(String agentCode){
        return null;
  }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }

   public TreeMap getProdGroups() throws SQLException {
             return null;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
        return null;
        }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
            tm.put("HOUSE", new Agent("HOUSE"));
     return tm;
   }




       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
     return new TreeMap();
   }


   public TreeMap getProducts(Agent agent){
	   TreeMap res = new TreeMap();
	   
	 JSONArray prods =  Converter.getWebProducts( rcompany, user ,  pass);
	 
	 for(int i = 0; i < prods.length(); i++) {
		 
		 try {
			JSONObject pr = (JSONObject) prods.get(i);
			//System.out.println("prod " + pr.toString());
			String code = (String)pr.get("code");
			String ean = (String)pr.get("EAN");
			String name = (String)pr.get("name");
			//System.out.println("prod " + code + " " + ean + " " + name );
			Product prod = new Product(code);
			prod.setName(name);
			prod.setEAN(ean);
			 eanprods.put(ean, code);
			 res.put(name, prod);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		 
	 }
      return res;
   }

   
   public String getAssortmentStr(Agent agent){
       return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     return new Vector();
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Converter";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
  
  

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
  }
  private TreeMap eanprods = null;
  
  @Override
  public void FixLoader() {
  	
  eanprods =  Converter.loadMore();
  	  
  	  if(eanprods == null) {
  		  eanprods = new TreeMap();
  	  }
  	this.getProducts(null);
  	TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  	System.out.println("Contracts " + contracts.toString());
  	Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
  	System.exit(0);
  }

  
  
  public static Properties loadProps(String directory){
	    Properties props = new Properties();
	    try {
	      FileInputStream in = new FileInputStream(directory + "/invoiceSettings.txt");
	      props.load(in);
	      in.close();
	    } catch (IOException e){
	       // showError(e);
	      }
	    return props;
	  }
  
  
 
  
@Override
public void WebReporter() {
	
	
	try{
		
		String currentWorkingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
	    System.out.println("Current working directory: " + currentWorkingDirectory);
	    
	    
		 Workbook workbook = null;
		 TreeMap groups = new TreeMap();

	        
		//excel sisse
         File folder = new File(currentWorkingDirectory + "/xlsxSisse");
         File[] listOfFiles = folder.listFiles();

         Properties props = loadProps(currentWorkingDirectory);
         
         
         
         
         NumberFormat formatter = NumberFormat.getNumberInstance();
         formatter.setMaximumFractionDigits(2);
         formatter.setMinimumFractionDigits(2);
         formatter.setGroupingUsed(false);
         
         NumberFormat formatter4 = NumberFormat.getNumberInstance();
         formatter4.setMaximumFractionDigits(4);
         formatter4.setMinimumFractionDigits(4);
         formatter4.setGroupingUsed(false);
         
         //Arve andmed InvoiceInformation
         String invoiceDate =  "30.11.2022";
         //String invoiceDateDue =  "21.12.2022";
         //String referenceNr =  "";
         
         //Arve saatja SellerParty
         String seller =  "Ohaka Energia OÜ";
         String sellerRegnr =  "14789735";
         String sellerVatnr =  "EE102206184";
         //String sellerAddress =  "Paemurru tn 8:Maardu:74117";
         
         
         //Arve saaja BuyerParty
         String buyer =  "Eesti Energia AS";
         String buyerRegnr =  "10421629";
         String buyerVatnr =  "EE100366327";
         //String buyerAddress =  "Lelle tn 22:Tallinn:11318";
         

         
         
         for (File file : listOfFiles) {
             if (file.isFile() && file.getName().endsWith(".xlsx")) {
            	 System.out.println(file.getName());
            	 FileInputStream ff = new FileInputStream(file);		// read the xlsx file
            	 workbook = new XSSFWorkbook(ff);
            	 ff.close();	
            	 Sheet sheet = workbook.getSheetAt(0); 
            	 
            	 
            	 
            	 Row  row = null;
            	 
            	 
     			if(sheet != null){
    				
					int rida = 4;
					int veerg = 1;
					row = sheet.getRow(rida);
					
					for (int r = rida; r < sheet.getLastRowNum()+1; r++) {
						row = sheet.getRow(r);
						 //System.out.println(row.getCell(2).toString());
						 //siia try/catch lisada
						 String[] data = {row.getCell(1).toString(), row.getCell(2).toString(), row.getCell(3).toString(), row.getCell(4).toString()};
						 groups.put(r-rida, data); //keyd algavad nullist
						
					}
					
					
					
					// Naites kirjas:
					// <ItemSum> + <AddSum> + <VATSum> peab võrduma <ItemTotal> summaga.
					// Koikide ridade <ItemTotal> summad + <Rounding> peavad võrduma <TotalSum> vaartusega.
					String arveID="";
					String dateToday="";
					String dateTodayArve="";
					String datePay="";
					String userSpecifDate = props.getProperty("kuupaev");
					String userSpecifDueDate = props.getProperty("maksetahtpaev");
					
			        //Kuupaevad
			        Calendar calendar = Calendar.getInstance();
			        //Calendar calendar1 = Calendar.getInstance();

			        //calendar.set(Calendar.MONTH, 11);
			        arveID = calendar.getTimeInMillis() + "";
			        
			        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
			        DateFormat datePFormat = new SimpleDateFormat("dd.MM.yyyy"); 
			        
			        
			        
			        dateToday = dateFormat.format(calendar.getTime());
			        
			        calendar.add(Calendar.MONTH, -1);
			        //calendar1.add(Calendar.MONTH, -1);
			        
			        //dateTodayArve = dateToday;
			        
			        //calendar1.add(Calendar.DATE, 21);
			        //datePay = dateFormat.format(calendar1.getTime());
			        
			        calendar.set(Calendar.DAY_OF_MONTH, 1); 	// set the calendar to the first day of the month
			        //String firstDayOfMonth = dateFormat.format(calendar.getTime());
			        String firstDayOfMonthP = datePFormat.format(calendar.getTime());
			        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			        String lastDayOfMonth = dateFormat.format(calendar.getTime());
			        String lastDayOfMonthP = datePFormat.format(calendar.getTime());
			        dateTodayArve = lastDayOfMonth;
			        
			        calendar.add(Calendar.DATE, 21);
			        datePay = dateFormat.format(calendar.getTime());
			        
			         if(!userSpecifDate.equals("0")) {
			        	 calendar.setTime(dateFormat.parse(userSpecifDate));
			        	 dateTodayArve = userSpecifDate;
				         calendar.set(Calendar.DAY_OF_MONTH, 1); 	// set the calendar to the first day of the month
		        	     firstDayOfMonthP = datePFormat.format(calendar.getTime());
				         calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				         lastDayOfMonthP = datePFormat.format(calendar.getTime());
				         lastDayOfMonth = dateFormat.format(calendar.getTime());
				         
			         }
			         
			         if(!userSpecifDueDate.equals("0")) {
			        	 datePay = userSpecifDueDate;
			         }
			        
			        
					double invoiceSumma = 0; 			//
					double invoiceSummaRound = 0; 		
					double invoiceVATSumma = 0; 		//VATSum
					double invoiceVATSummaRound = 0; 	//
					double itemTotalSumma = 0;
					double itemTotalSummaRound = 0;
					double roundingErrorTotal = 0;
					double roundingError = 0;			//
					double roundingErrorVAT = 0;		//
					double totalSumma = 0;
					double totalVATSumma = 0;

					

					//mitu dokumenti on vaja teha kui 120rida ühes dokumendis
					int dokArv = (int) Math.ceil(groups.size()/120f);
					//System.out.println(dokArv);
					
					
					for (int a = 0; a < dokArv; a++) {
						//Teeme uue dokumendi
						
						 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				         DocumentBuilder db = dbf.newDocumentBuilder();
				         Document doc = db.newDocument();
				         
				         
				         String arvenr = props.getProperty("arvenr" + (a+1));
				         
				         

				         

				         	// E_Invoice element
				            Element eInvoice = doc.createElement("E_Invoice");
				            doc.appendChild(eInvoice);
				            // Header element
				            Element header = doc.createElement("Header");
				            eInvoice.appendChild(header);
				            // Date element
				            Element date = doc.createElement("Date");
				            date.appendChild(doc.createTextNode(dateToday));
				            header.appendChild(date);
				            // FileId element
				            Element fileId = doc.createElement("FileId");
				            fileId.appendChild(doc.createTextNode(arveID));
				            header.appendChild(fileId);
				            // Version element
				            Element version = doc.createElement("Version");
				            version.appendChild(doc.createTextNode("1.1"));
				            header.appendChild(version);
				            // Invoice element
				            Element invoice = doc.createElement("Invoice");
				            invoice.setAttribute("invoiceId", arvenr);
				            invoice.setAttribute("regNumber", buyerRegnr);
				            invoice.setAttribute("sellerRegnumber", sellerRegnr);
				            eInvoice.appendChild(invoice);
				            // InvoiceParties element
				            Element invoiceParties = doc.createElement("InvoiceParties");
				            invoice.appendChild(invoiceParties);
				            // SellerParty element
				            Element sellerParty = doc.createElement("SellerParty");
				            invoiceParties.appendChild(sellerParty);
				            // Name element
				            Element name = doc.createElement("Name");
				            name.appendChild(doc.createTextNode(seller));
				            sellerParty.appendChild(name);
				            // RegNumber element
				            Element regNumber = doc.createElement("RegNumber");
				            regNumber.appendChild(doc.createTextNode(sellerRegnr));
				            sellerParty.appendChild(regNumber);
				            // BuyerParty element
				            Element buyerParty = doc.createElement("BuyerParty");
				            invoiceParties.appendChild(buyerParty);
				            // Name element
				            Element name1 = doc.createElement("Name");
				            name1.appendChild(doc.createTextNode(buyer));
				            buyerParty.appendChild(name1);
				            // RegNumber element
				            Element regNumber1 = doc.createElement("RegNumber");
				            regNumber1.appendChild(doc.createTextNode(buyerRegnr));
				            buyerParty.appendChild(regNumber1);
				            // VATRegNumber element
				            Element vatRegNumber = doc.createElement("VATRegNumber");
				            vatRegNumber.appendChild(doc.createTextNode(buyerVatnr));
				            buyerParty.appendChild(vatRegNumber);
				            // ContactData element
				            Element contactData = doc.createElement("ContactData");
				            buyerParty.appendChild(contactData);
				            // LegalAddress element
				            Element legalAddress = doc.createElement("LegalAddress");
				            contactData.appendChild(legalAddress);
				            // PostalAddress1 element
				            Element postalAddress1 = doc.createElement("PostalAddress1");
				            postalAddress1.appendChild(doc.createTextNode("Lelle tn 22"));
				            legalAddress.appendChild(postalAddress1);
				            // City element
				            Element city = doc.createElement("City");
				            city.appendChild(doc.createTextNode("Kesklinna linnaosa, Tallinn"));
				            legalAddress.appendChild(city);
				            // PostalCode element
				            Element postalCode = doc.createElement("PostalCode");
				            postalCode.appendChild(doc.createTextNode("11318"));
				            legalAddress.appendChild(postalCode);
				            // Country element
				            Element country = doc.createElement("Country");
				            country.appendChild(doc.createTextNode("EE"));
				            legalAddress.appendChild(country);
				            // InvoiceInformation element
				            Element invoiceInformation = doc.createElement("InvoiceInformation");
				            invoice.appendChild(invoiceInformation);
				            // Type element
				            Element type = doc.createElement("Type");
				            type.setAttribute("type", "DEB");
				            invoiceInformation.appendChild(type);
				            // DocumentName element
				            Element documentName = doc.createElement("DocumentName");
				            documentName.appendChild(doc.createTextNode("arve"));
				            invoiceInformation.appendChild(documentName);
				            // InvoiceNumber element
				            Element invoiceNumber = doc.createElement("InvoiceNumber");
				            
				            invoiceNumber.appendChild(doc.createTextNode(arvenr));
				            invoiceInformation.appendChild(invoiceNumber);
				            // InvoiceDate element
				            Element invoiceDate2 = doc.createElement("InvoiceDate");
				            invoiceDate2.appendChild(doc.createTextNode(dateTodayArve));
				            invoiceInformation.appendChild(invoiceDate2);
				            // DueDate element
				            Element dueDate = doc.createElement("DueDate");
				            dueDate.appendChild(doc.createTextNode(datePay));
				            invoiceInformation.appendChild(dueDate);
				            // InvoiceSumGroup element
				            Element invoiceSumGroup = doc.createElement("InvoiceSumGroup");
				            invoice.appendChild(invoiceSumGroup);
				            
				            // InvoiceItem element
				            Element invoiceItem = doc.createElement("InvoiceItem");
				            invoice.appendChild(invoiceItem);
				            // InvoiceItemGroup element
				            Element invoiceItemGroup = doc.createElement("InvoiceItemGroup");
				            invoiceItem.appendChild(invoiceItemGroup);
				            
				            
				            
				            invoiceSumma = 0;
				            invoiceSummaRound = 0;
				            invoiceVATSumma = 0;
				            invoiceVATSummaRound = 0;
				            itemTotalSumma = 0;
				            itemTotalSummaRound = 0;
				            
				            
				            //invoiceVATSummaRound = 0;
								//lisame read dokumenti 120 korraga
								for (int b = (120*a); b < (120*(a+1)); b++) {
									if(b == 50) {
										//break;
									}
									
									
									 //try/catch ridade lisamiseks kui read otsa saavad
									try {
										String[] data = {};
										data = (String[]) groups.get(b);
										double itemSumma = (Double.parseDouble(data[1]) * Double.parseDouble(data[2]));
										
										invoiceSumma = invoiceSumma + itemSumma;
										invoiceSummaRound = invoiceSummaRound + Double.parseDouble(formatter.format(itemSumma));
										invoiceVATSumma = invoiceVATSumma + (itemSumma * 0.2);
										invoiceVATSummaRound = invoiceVATSummaRound + Double.parseDouble(formatter.format(itemSumma * 0.2));
										
										itemTotalSumma = itemTotalSumma + (itemSumma + (itemSumma * 0.2));
										//itemTotalSummaRound = itemTotalSummaRound + Double.parseDouble(formatter.format(itemSumma + (itemSumma * 0.2)));
										itemTotalSummaRound = itemTotalSummaRound + Double.parseDouble(formatter.format(itemSumma)) + Double.parseDouble(formatter.format(itemSumma*0.2));
										//Double.parseDouble(data[3])*0.2 arvutab km-i
										//formatter.format(itemSumma + Double.parseDouble(data[3]) * 0.2)))
										
										
									//System.out.println("Dokk: " + a + "  Rida: " + b);
									//System.out.println(a + " " + data[0] + " " + data[1]);
		
						            // ItemEntry element
						            Element itemEntry = doc.createElement("ItemEntry");
						            invoiceItemGroup.appendChild(itemEntry);
						            // Description element
						            Element description = doc.createElement("Description");
						            description.appendChild(doc.createTextNode("Vorku antud elektrienergia " + firstDayOfMonthP.trim() + " - " + lastDayOfMonthP.trim() + ". Mootepunkti EIC-kood: " + data[0]));
						            //description.appendChild(doc.createTextNode("Võrku antud elektrienergia " + firstDayOfMonthP + "-" + lastDayOfMonthP + ". Mõõtepunkti EIC-kood: " + data[0]));
						            itemEntry.appendChild(description);
						            // EAN element
						            Element eankood = doc.createElement("EAN");
						            eankood.appendChild(doc.createTextNode("elektrienergia"));
						            itemEntry.appendChild(eankood);
						            // ItemDetailInfo element
						            Element itemDetailInfo = doc.createElement("ItemDetailInfo");
						            itemEntry.appendChild(itemDetailInfo);
						            // ItemUnit element
						            Element itemUnit = doc.createElement("ItemUnit");
						            itemUnit.appendChild(doc.createTextNode("MWh")); //MWh
						            itemDetailInfo.appendChild(itemUnit);
						            // ItemAmount element
						            Element itemAmount = doc.createElement("ItemAmount");
						            itemAmount.appendChild(doc.createTextNode(data[1])); //MWh
						            itemDetailInfo.appendChild(itemAmount);
						            // ItemPrice element
						            Element itemPrice = doc.createElement("ItemPrice");
						            itemPrice.appendChild(doc.createTextNode(data[2]));//eur/mWh
						            itemDetailInfo.appendChild(itemPrice);
						            
						            // ItemSum element
						            Element itemSum = doc.createElement("ItemSum");
						            itemSum.appendChild(doc.createTextNode(formatter.format(itemSumma)));		//hind
						            itemEntry.appendChild(itemSum);
						            // VAT element
						            Element vat = doc.createElement("VAT");
						            vat.setAttribute("vatId", "TAX");
						            itemEntry.appendChild(vat);
						            // VATRate element
						            Element vatRate = doc.createElement("VATRate");
						            vatRate.appendChild(doc.createTextNode("20.00"));
						            vat.appendChild(vatRate);
						            // VATSum element
						            Element vatSum = doc.createElement("VATSum");	
						            vatSum.appendChild(doc.createTextNode(formatter.format(itemSumma * 0.2))); //hind*0.2
						            vat.appendChild(vatSum);
						            // ItemTotal element
						            Element itemTotal = doc.createElement("ItemTotal");
						            itemTotal.appendChild(doc.createTextNode(formatter.format(Double.parseDouble(formatter.format(itemSumma)) + Double.parseDouble(formatter.format(itemSumma*0.2))))); //hind+hind*0.2
						            itemEntry.appendChild(itemTotal);
						            
									} catch (Exception e) {
										System.out.println("read otsas excelis: " + b);
										//e.printStackTrace();
									}
									
								}
								//roundingError = invoiceSumma-invoiceSummaRound;							//rounding error
								//roundingErrorVAT = invoiceVATSumma - invoiceVATSummaRound;
								//roundingError = invoiceSummaRound-invoiceSumma;							//rounding error
								//roundingErrorVAT = invoiceVATSummaRound - invoiceVATSumma;
								
								//System.out.println("Rounding Error: " + roundingError);
								//System.out.println("Rounding Error VAT: " + roundingErrorVAT);
								//invoiceVATSumma = invoiceSumma*0.2;
								
								roundingErrorTotal = itemTotalSumma - itemTotalSummaRound;
								//roundingErrorTotal = roundingError+roundingErrorVAT;
								totalSumma = invoiceSumma + invoiceVATSumma;
								
								//double totalSummaRound = ;

								
								
								// InvoiceSum element
					            Element invoiceSum = doc.createElement("InvoiceSum");
					            invoiceSum.appendChild(doc.createTextNode(formatter.format(invoiceSummaRound)));
					            invoiceSumGroup.appendChild(invoiceSum);
					            //Rounding element
					            Element rounding = doc.createElement("Rounding");
					            rounding.appendChild(doc.createTextNode(formatter.format(roundingErrorTotal)));
					            invoiceSumGroup.appendChild(rounding);
								
					            // TotalVATSum element
					            Element totalVATSum = doc.createElement("TotalVATSum");
					            totalVATSum.appendChild(doc.createTextNode(formatter.format(invoiceVATSummaRound)));
					            invoiceSumGroup.appendChild(totalVATSum);
					            // TotalSum element
					            Element totalSum = doc.createElement("TotalSum");
					            totalSum.appendChild(doc.createTextNode(formatter.format((totalSumma))));
					            invoiceSumGroup.appendChild(totalSum);
					            //System.out.println("totalSum: " + totalSumma);
					            //System.out.println("totalSumKontrollRounding: " + (itemTotalSummaRound + roundingErrorTotal));
					            						
								
								
			            // PaymentInfo element
			            Element paymentInfo = doc.createElement("PaymentInfo");
			            invoice.appendChild(paymentInfo);
			            // Currency element
			            Element currency = doc.createElement("Currency");
			            currency.appendChild(doc.createTextNode("EUR"));
			            paymentInfo.appendChild(currency);
			            
			          /*  // PaymentRefId element
			            Element paymentRefId = doc.createElement("PaymentRefId");
			            paymentRefId.appendChild(doc.createTextNode("10579"));
			            paymentInfo.appendChild(paymentRefId);*/
			            // PaymentDescription element
			            Element paymentDescription = doc.createElement("PaymentDescription");
			            paymentDescription.appendChild(doc.createTextNode("Arve nr " + arvenr));
			            paymentInfo.appendChild(paymentDescription);
			            // Payable element
			            Element payable = doc.createElement("Payable");
			            payable.appendChild(doc.createTextNode("YES"));
			            paymentInfo.appendChild(payable);
			            
			            // PaymentTotalSum element
			            Element paymentTotalSum = doc.createElement("PaymentTotalSum");
			            paymentTotalSum.appendChild(doc.createTextNode(formatter.format(totalSumma)));
			            paymentInfo.appendChild(paymentTotalSum);
			            // PayerName element
			            Element payerName = doc.createElement("PayerName");
			            payerName.appendChild(doc.createTextNode(buyer));
			            paymentInfo.appendChild(payerName);
			            // PaymentId element
			            Element paymentId = doc.createElement("PaymentId");
			            paymentId.appendChild(doc.createTextNode(arvenr));
			            paymentInfo.appendChild(paymentId);
			            // PayToAccount element
			            Element payToAccount = doc.createElement("PayToAccount");
			            payToAccount.appendChild(doc.createTextNode("EE304204278615034901"));
			            paymentInfo.appendChild(payToAccount);
			            // PayToName element
			            Element payToName = doc.createElement("PayToName");
			            payToName.appendChild(doc.createTextNode(seller));
			            paymentInfo.appendChild(payToName);
			            // Footer element
			            Element footer = doc.createElement("Footer");
			            eInvoice.appendChild(footer);
			            // TotalNumberInvoices element
			            Element totalNumberInvoices = doc.createElement("TotalNumberInvoices");
			            totalNumberInvoices.appendChild(doc.createTextNode("1"));
			            footer.appendChild(totalNumberInvoices);
			            // TotalAmount element
			            Element totalAmount = doc.createElement("TotalAmount");
			            totalAmount.appendChild(doc.createTextNode(formatter.format(totalSumma)));
			            footer.appendChild(totalAmount);
			            //String fileName = "/home/gpc/Desktop/ohakaelekter/valja/" + arvenr + ".xml";
			            String fileName = currentWorkingDirectory + "/xmlValja/" + arvenr + ".xml";
					      System.out.println(fileName);
					      OutputFormat of = new OutputFormat();
					        of.setIndenting(true);
					        of.setLineSeparator(System.getProperty("line.separator"));
					        of.setEncoding("ISO-8859-1");
					        of.setLineWidth(Integer.MAX_VALUE);
					        XMLSerializer ser = new XMLSerializer(of);
					        FileOutputStream fos = new FileOutputStream(fileName);
					        PrintWriter out = new PrintWriter(fos);
					        ser.setOutputCharStream(out);
					        ser.serialize(doc);
					        out.close();
						
						
						
						
					}
					
					
					
					
					
					

					

    			}

                 
                 break;				//ainult esimese exceli faili lugemiseks
             }
         }
		 
		 
		 
		 	//File file = new File("/home/gpc/Desktop/ohakaelekter/sisse/Ohaka Energia OU¨ - 12.2022 - kokkuvo~te.xlsx");
	       //FileInputStream	 ff = new FileInputStream(file);

			//DataFormatter formatter = new DataFormatter();
			
         System.out.println("Tehtud!");

	}catch(Exception e) {
		 e.printStackTrace();
	}	 
	
	
	System.exit(0);
	
}



  }
