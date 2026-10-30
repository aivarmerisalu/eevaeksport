package eevaeksport.driver;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;



public class BC365ValdoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker{

    public BC365ValdoDBDriver(EksportPanel parent) {
        super(parent);
        this.parent = parent;
    }

    private final EksportPanel parent;

    private final String[] operations = {
            "GetAgents",
            "GetCustomerAddress",
            "GetCustomerEntries",
            "GetCustomers",
            "GetInventoryQty",
            "GetItemDiscount",
            "GetItemGroups",
            "GetItems",
            "GetPriceList",
            "GetPromotions",
            "GetSalesHistory",
            "GetShipToAddress",
            "GetShipToAddressRoute"
    };

    /** Paging rule */
    private static final int MAX_OBJECTS_PER_FILE = 5000;

    /* =========================
       Public API
       ========================= */

    @Override
    public void PictureMaker() {
    	 makePictureCat();
    	  System.exit(0);
    	
    }
    
    private void makePictureCat(){
	    
      	
      	String pictssou = parent.getProps().getProperty("pict.dir");
    	    String pictspda = parent.getProps().getProperty("pictpda.dir");
    	    TreeMap phsize = new TreeMap();
    	    
    	    
    	 try {
       	  File f = new File(pictspda +"list.txt");
            if (f.exists())f.delete();
            //Kataloogi sisude kustutamine
        	  File res2 = new File (pictspda);
               String[] ff2 = res2.list();
          
//           	 Lõppes kustutamine 
    		 
    		 TreeMap prods = this.getProducts(null);
           StringBuffer buf = new StringBuffer();
           Iterator enume = prods.keySet().iterator();
      
           while (enume.hasNext()) {
      	    	String asi = (String)enume.next();
      	    	Product prod = (Product)prods.get(asi);
      	    	String toode = prod.getID();  
                 if(toode.length() > 0){
                  	
      	   	  res2 = new File (pictssou);
                ff2 = res2.list();
              
           	 for(int i = 0;ff2.length>i;i++){
               	if( ff2[i].startsWith(toode)){
                 // System.out.println("pilt olemas " + ff2[i]);
              	File in = new File ( pictssou + ff2[i]); 
                	if(in.isFile()){
                	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
                	if(!inpda.exists()) {
                		try {
                		Converter.shrinkAndCopyFile(in,inpda,250);
                		}catch(Exception e) {System.out.println("kehv pilt " + prod.getID());}
                	}
                    	
                    	File kkk =  new File ( pictspda + prod.getID()+ ".jpg"); 
                    	phsize.put(prod.getID(), kkk.length());
              	    }
                   }
                 }
               }
      	   }
          
           
         Iterator itera =   phsize.keySet().iterator();
         while(itera.hasNext()) {
      	   String prid = (String)itera.next();
      		buf.append(prid +".jpg;"  + phsize.get(prid) + "\n");
                 
         }
          
           
           try {
       		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
       		os.write(buf.toString());
       		os.close();
       	  }catch (Exception ex) {
       		ex.printStackTrace();
       	  }
    	 
    	 } catch (Exception e) {
      	    getParent().showError(e);
      	}
        
    }


    

    public void downloadData(String operation) {
        if (operation == null) {
            for (String op : operations) {
                downloadData(op);
            }
            return;
        }

        try {
        	
        	if(operation.equals("GetSalesHistory")) {
        		

        		        DateTimeFormatter apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        		        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        		        // Current week start (Monday)
        		        LocalDate currentWeekStart =
        		                LocalDate.now().with(DayOfWeek.MONDAY);

        		        // Last 20 weeks including current week
        		        for (int i = 0; i < 20; i++) {

        		            LocalDate fromDate = currentWeekStart.minusWeeks(i);
        		            LocalDate toDate = fromDate.plusDays(6);

        		            int weekNumber = fromDate.get(weekFields.weekOfWeekBasedYear());
        		            int weekYear = fromDate.get(weekFields.weekBasedYear());

        		            // WWYYYY
        		            String filename = String.format(
        		                    "%s%02d%d",
        		                    operation,
        		                    weekNumber,
        		                    weekYear
        		            );

        		            JSONObject js = new JSONObject();
        		            js.put("FromEntryNo", 0);
        		            js.put("FromDate", fromDate.format(apiFormatter));
        		            js.put("ToDate", toDate.format(apiFormatter));
        		            js.put("ItemNo", "");
        		            js.put("Limit", 10000);


        		            File file = new File("data" + File.separator + filename + ".json");
        		            if(i > 1) {
        		            	if(!file.exists()) {

                		            String json = callOperation(operation, js.toJSONString());
                              // al üleeelmiseid nädalaid üle ei kirjuta
                		            saveSingleFile(filename, json);
        		            	}
        		            }else {

            		            String json = callOperation(operation, js.toJSONString());
            		            saveSingleFile(filename, json);
        		            }

        		            System.out.println("Saved: " + filename + ".json");
        		        }
        		    
        		    


        	 } else {
		        String json = callOperation(operation, null);
		        System.out.println("result " + operation+ " " + json.length());
		         saveFilePaged(operation, json, MAX_OBJECTS_PER_FILE);
        	}

        } catch (Exception e) {
            System.err.println("Failed to download data for operation: " + operation);
            e.printStackTrace();
        }
  
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	     
  }
	  String user =  null;
	  String pass = null;
	  String rcompany = null;


    private void saveFilePaged(String baseName, String jsonContent, int maxObjectsPerFile) {
        try {
            // Ensure data folder exists
            String folderPath = "data";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Remove "Get" prefix from baseName if present
            String cleanName = baseName;
            if (cleanName.startsWith("Get")) {
                cleanName = cleanName.substring(3);
            }

            // Split JSON array into individual objects
            String trimmed = jsonContent.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                // Not an array, save as single file
                saveSingleFile(cleanName + "1", jsonContent);
                return;
            }

            String arrayContent = trimmed.substring(1, trimmed.length() - 1).trim();
            String[] items = arrayContent.isEmpty() ? new String[0] : arrayContent.split("(?<=\\}),(?=\\{)");
            
            int part = 1;
            for (int i = 0; i < items.length; i += maxObjectsPerFile) {
                StringBuilder chunk = new StringBuilder();
                chunk.append("[");
                int end = Math.min(i + maxObjectsPerFile, items.length);
                for (int j = i; j < end; j++) {
                    chunk.append(items[j]);
                    if (j < end - 1) {
                        chunk.append(",");
                    }
                }
                chunk.append("]");

                // Build filename with part number
                String fileName = cleanName + part;
                part++;

                // Save the chunk
                saveSingleFile(fileName, chunk.toString());
            }

        } catch (Exception e) {
            System.err.println("Failed to save paged files for " + baseName);
            e.printStackTrace();
        }
    }

    private void saveSingleFile(String fileName, String jsonContent) {
        try {
            String folderPath = "data";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folderPath + File.separator + fileName + ".json");

            try (OutputStreamWriter writer =
                         new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                writer.write(jsonContent);
            }

            System.out.println("Saved file: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to save file " + fileName);
            e.printStackTrace();
        }
    }


    /* =========================
       CORE NTLM SOAP CALL
       ========================= */

    public String callOperation(String operation, String json) throws Exception {

        System.out.println("op " + operation);
        String serviceUrl = parent.getProps().getProperty("api.SERVICE_URL");
        String namespace  = parent.getProps().getProperty("api.NAMESPACE");
        String domain     = parent.getProps().getProperty("api.DOMAIN");
        String username   = parent.getProps().getProperty("api.USERNAME");
        String password   = parent.getProps().getProperty("api.PASSWORD");

        URL url = new URL(serviceUrl);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new NTCredentials(username, password, null, domain)
        );

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpPost post = new HttpPost(serviceUrl);
            post.setHeader("Content-Type", "text/xml; charset=utf-8");
            post.setHeader("SOAPAction", namespace + ":" + operation);

            String soapEnvelope = buildSoapEnvelope(namespace, operation);
            if(json != null) {
            	soapEnvelope = buildSoapEnvelope(namespace, operation, json);
            }
            
            post.setEntity(new StringEntity(soapEnvelope, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(post);
                 InputStream is = response.getEntity().getContent()) {

                return extractReturnValue(is);
            }
        }
    }


    /* =========================
       SOAP HELPERS
       ========================= */

    private String buildSoapEnvelope(String namespace, String operation) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ep=\"" + namespace + "\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ep:" + operation + "/>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildSoapEnvelope(
            String namespace,
            String operation,
            String jsonParams
    ) {

        return
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:ep=\"" + namespace + "\">" +
            "<soapenv:Header/>" +
            "<soapenv:Body>" +
                "<ep:" + operation + ">" +
                    "<ep:jsonRequest>" + (jsonParams) + "</ep:jsonRequest>" +
                "</ep:" + operation + ">" +
            "</soapenv:Body>" +
            "</soapenv:Envelope>";
    }
    
    private String extractReturnValue(InputStream xmlStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);
        NodeList nodes = document.getElementsByTagName("return_value");

        if (nodes.getLength() == 0) {
            throw new RuntimeException("SOAP response does not contain <return_value>" );
        }
        StringBuilder sb = new StringBuilder();
        NodeList children = nodes.item(0).getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeValue() != null) {
                sb.append(children.item(i).getNodeValue());
            }
        }
        String tulem = sb.toString().trim();

        return tulem;
    }

    /* =========================
       ABSTRACT METHODS (stubs)
       ========================= */

    public TreeMap getAgents(){
		  TreeMap tm = new TreeMap();
		  
			try {
			org.json.simple.JSONArray cc = 	Converter.readJSONArrFileUTF("data//Agents1.json");
	
			
			  int lenn = cc.size();
		
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) cc.get(i);
	
	            String salesCode = (String)cob.get("code");
	            String salesName = (String)cob.get("name");
	            if (salesCode != null && salesCode.trim().length() > 0) {
	            	Agent agent = new Agent(clean(salesCode));
	            	agent.setName(clean(salesName + " " + salesCode ));
	            	  tm.put(agent.getName(), agent);
	              }
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
	//		getPriceMatrixData();
	  //System.out.println("Managersize " + tm.size());
		//	getCustomerProductsList(null);
	   return tm;
	 }
    
    
    private String clean(String input) {
    	if (input == null) {
    		return null;
    	}
    	return input.replaceAll("\n", "")
        .replaceAll("\r", "")
        .replaceAll("\t", "").trim();
    }
    
    private Date getDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
	    return date;
		
	  }
    
    public TreeMap<String, Customer> getCustomers(Agent agent) {

        if (!"true".equals(getParent().getProps().getProperty("useolddata"))) {
            downloadData(null);
        }
        //DEBT
        
        
        debt = new Vector();

        Date today = new Date(Calendar.getInstance().getTimeInMillis());

        TreeMap<String, Double> volad = new TreeMap<>();
        TreeMap<String, Double> used = new TreeMap<>();
        int debtPart = 1;

        while (true) {
            File file = new File("data/customerEntries" + debtPart + ".json");
            if (!file.exists()) break;
        
        org.json.simple.JSONArray values1 =
                (org.json.simple.JSONArray) Converter.readJSONArrFileUTF("data/customerEntries" + debtPart + ".json");

        int lenn1 = values1.size();
        debtPart++;
        try {

            for (int i = 0; i < lenn1; i++) {

                org.json.simple.JSONObject cob =
                        (org.json.simple.JSONObject) values1.get(i);

                // ===== NEW FIELD MAPPING =====

                String custid = clean((String) cob.get("customerShipToCode"));
                String payer = clean((String) cob.get("customerNo")); // no separate payer in new JSON

                Double balDue = 0.0;
                Object amountObj = cob.get("remainingAmount");
                if (amountObj != null) {
                    try {
                        balDue = Double.parseDouble(amountObj.toString());
                    } catch (Exception ignored) {}
                }

                String postingDate = (String) cob.get("postingDate");

                String dueDate = (String) cob.get("dueDate");
                String invnr = clean((String) cob.get("externalDocumentNo"));
                if (invnr.length() == 0) {
                	invnr = "No Number";
                }
               // {"customerNo":"11971000","customerShipToCode":"1197100004","postingDate":"2025-12-19","dueDate":"2026-01-02","externalDocumentNo":"VLD25_140373","remainingAmount":28.74},
                // ===== CREATE DEBT OBJECT =====

                Debt dd = new Debt();
                dd.setCustID(custid);
                dd.setPayerID(payer);
                dd.setDoknr(invnr);

                Date invDateObj = getDate(postingDate);
                dd.setDate(invDateObj);

                Date dueDateObj = getDate(dueDate);
                dd.setDuedate(dueDateObj);

                int ff = (int) ((Calendar.getInstance().getTimeInMillis()
                        - invDateObj.getTime()) / 86400000);

                dd.setDue(ff + "");
                dd.setSum(balDue);

                debt.add(dd);

                // ===== AGGREGATION LOGIC =====

                if (balDue != 0 && payer != null && payer.length() > 0) {

                    // Used credit (total open)
                    if (used.get(payer) != null) {
                        used.put(payer, balDue + used.get(payer));
                    } else {
                        used.put(payer, balDue);
                    }

                    // Overdue logic (since no dueDate, treat postingDate as due)
                    if (today.after(invDateObj)) {

                        if (volad.get(payer) != null) {
                            volad.put(payer, balDue + volad.get(payer));
                        } else {
                            volad.put(payer, balDue);
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }	
        }
        System.out.println("Total debts loaded: " + debt.size());

        
        
        
        
        TreeMap<String, Customer> tm = new TreeMap<>();

        // ===== READ SHIPTOADDRESS FILES FIRST =====
        HashMap<String, List<String>> shipToMap = new HashMap<>();
        int shipPart = 1;
        while (true) {
            File file = new File("data/ShipToAddress" + shipPart + ".json");
            if (!file.exists()) break;

            //System.out.println("Processing ShipToAddress file: " + file.getName());

            try {
                org.json.simple.JSONArray values =
                    (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(file.getPath());

                for (Object obj : values) {
                    org.json.simple.JSONObject json = (org.json.simple.JSONObject) obj;

                    String customerShipToCode = (String) json.get("customerShipToCode");
                    if (customerShipToCode != null) {
                        customerShipToCode = clean(customerShipToCode);
                            
                    } else continue;

                    String salespersonCode = (String) json.get("salespersonCode");
                    if (salespersonCode != null) {
                        salespersonCode = clean(salespersonCode);
                    } else continue;

                    shipToMap.computeIfAbsent(customerShipToCode, k -> new ArrayList<>())
                             .add(salespersonCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            shipPart++;
        }
        //System.out.println("Finished building ShipTo map with " + shipToMap.size() + " entries.");
        // ===== READ ShipToAddressRoute FILES =====
        int shipRoutePart = 1;
        HashMap<String, List<Agent>> shipToRouteMap = new HashMap<>();
        while (true) {
            File file = new File("data/ShipToAddressRoute" + shipRoutePart + ".json");
            if (!file.exists()) break;

            //System.out.println("Processing ShipToAddressRoute file: " + file.getName());

            try {
                org.json.simple.JSONArray values =
                    (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(file.getPath());

                for (Object obj : values) {
                    org.json.simple.JSONObject json = (org.json.simple.JSONObject) obj;

                    // ===== CLEAN customerShipToCode =====
                    String customerShipToCode = (String) json.get("customerShipToCode");
                    if (customerShipToCode != null) {
                        customerShipToCode = clean(customerShipToCode);
                    }

                    // ===== CLEAN salespersonCode =====
                    String salespersonCode = (String) json.get("salespersonCode");
                    if (salespersonCode != null) {
                        salespersonCode = clean(salespersonCode);
                    }

                    // ===== CLEAN route =====
                    String route = (String) json.get("route");
                    if (route != null) {
                        route = clean(route);
                    }
                    // Mapping routes onto customers using Agent
                    Agent agent1 = new Agent(salespersonCode);
                    agent1.setVisit(route);
                    
                    shipToRouteMap.computeIfAbsent(customerShipToCode, k -> new ArrayList<>())
                    .add(agent1);
                    
                    
                    
                    
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            shipRoutePart++;
        }
       // System.out.println("shipToRouteMap " + shipToRouteMap.toString());
      


        // ===== READ CUSTOMERS FILES =====
        int part = 1;
        while (true) {
            File file = new File("data/Customers" + part + ".json");
            if (!file.exists()) break; // stop when no more parts

           // System.out.println("Processing file: " + file.getName());

            try {
                org.json.simple.JSONArray values =
                    (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(file.getPath());

                int len = values.size();
                for (int i = 0; i < len; i++) {

                    org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

                    // ===== CLEAN "no" FIELD =====
                    String number = (String) cob.get("no");
                    if (number != null) {
                        number = clean(number);
                    } 

                    Customer cust = new Customer(number);
                    cust.setPayer(number);
                    
                   
                    
                    String name = (String) cob.get("name");
                    if (name != null) cust.setName(clean(name.trim()));

                    String address = (String) cob.get("address");
                    if (address != null) {  
                     cust.setAddress(Converter.adressLink(address,"Latvia"));
 	                  name = name + " " + address;
 	                  cust.setName(name);
 	                }

                    String phone = (String) cob.get("phoneNo");
                    if (phone != null && phone.trim().length() > 0) {
                        cust.setPersonalData("Tel:" + clean(phone));
                    }

                    Object creditLimitObj = cob.get("creditLimit");
                    if (creditLimitObj != null) cust.setCredit(String.valueOf(creditLimitObj));

                    Object balanceObj = cob.get("balance");
                    if (balanceObj != null) {
                        try {
                            cust.setUsedCredit(Double.parseDouble(balanceObj.toString()));
                        } catch (Exception ignored) {}
                    }

                    // Optional fields
                    String status = (String) cob.get("status");

                    String paymentMethod = (String) cob.get("paymentMethodCode");
                    //if (paymentMethod != null) { cust.setPaymentMethod(paymentMethod.trim()); }

                    String priceGroup = (String) cob.get("priceGroupCode");
                    if (priceGroup != null) {
                    	cust.setGroup(priceGroup.trim());
                    	cust.addCity("Grupa: " + priceGroup.trim());
                    }
           
                    String storeCat = (String) cob.get("franchise");
                    if (storeCat != null && storeCat.trim().length() > 0) {
                    	cust.setChain(storeCat.trim());
                    	cust.addCity("Franchise: " + storeCat.trim());
                    }

                    Object consignationDaysObj = cob.get("consignationDays");
                    if (consignationDaysObj != null) {
                        cust.setPaymTerm(String.valueOf(consignationDaysObj));
                    }

                    // ===== ASSIGN SALESPERSONS BASED ON customerShipToCode =====
                    List<String> salespersons = shipToMap.get(number);
                    
                    
                    List<Agent> salespersonsRoute = shipToRouteMap.get(number);
                    if (salespersons != null) {
                        for (String ag : salespersons) {
                            boolean olemas = false;

                            if (salespersonsRoute != null) {
                                for (Agent ag1 : salespersonsRoute) {
                                    if (ag1.getID().equals(ag)) {
                                        cust.addAgent(ag1);
                                       
                                        olemas = true;
                                        break;
                                    }
                                }
                            }

                            if (!olemas) {
                                cust.addAgent(new Agent(ag));
                               
                            }
                        }
                    }

                 //   cust.addAgent(new Agent("0078"));
                    
                    if (volad.get(cust.getID() ) != null) {
    	            	cust.setDebt(volad.get(cust.getID()));
    	            }
    	            if (volad.get(cust.getPayer() ) != null) {
    	            	cust.setDebt(volad.get(cust.getPayer()));
    	            }
    	            if (used.get(cust.getID() ) != null) {
    	            	cust.setUsedCredit(used.get(cust.getID()));
    	            }
    	            if (used.get(cust.getPayer()) != null) {
    	            	cust.setUsedCredit(used.get(cust.getPayer()));
    	            }
               

                    if ("A".equals(status)) {
                        tm.put(cust.getName(), cust);
                        System.out.println("pg code " + cust.getGroup());

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            part++;
        }
        System.out.println("Kliendid " + tm.size());
        return tm;
    }


    
    
    
    @Override
    public TreeMap<String, Product> getProducts(Agent agent) {
        TreeMap<String, Product> tm = new TreeMap<>();

        // ===== LOAD STOCKS =====
        HashMap<String, Double> stocks = new HashMap<>();
        int stockPart = 1;
        while (true) {
            File stockFile = new File("data/InventoryQty" + stockPart + ".json");
            if (!stockFile.exists()) break;

            System.out.println("Loading stock file: " + stockFile.getName());

            try {
                org.json.simple.JSONArray stockValues =
                        (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(stockFile.getPath());

                for (int i = 0; i < stockValues.size(); i++) {
                    org.json.simple.JSONObject stockObj =
                            (org.json.simple.JSONObject) stockValues.get(i);

                    String itemNo = (String) stockObj.get("itemNo");
                    Object qtyObj = stockObj.get("availableQty");
                    if (itemNo != null && qtyObj != null) {
                        double qty = Double.parseDouble(qtyObj.toString());
                        stocks.put(clean(itemNo), qty);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stockPart++;
        }

        // ===== LOAD PRODUCTS =====
        int part = 1;
        while (true) {
            File itemFile = new File("data/items" + part + ".json");
            if (!itemFile.exists()) break;

            System.out.println("Processing product file: " + itemFile.getName());

            try {
                org.json.simple.JSONArray values =
                        (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(itemFile.getPath());

                for (int i = 0; i < values.size(); i++) {
                    org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

                    // ===== CLEAN "no" FIELD =====
                    String number = (String) cob.get("no");
                    if (number != null) {
                        number = clean(number);
                    } else {
                        number = "";
                    }
                    String active = (String) cob.get("active");
                    if (active == null || !active.equalsIgnoreCase("A")) {
                        continue; // skip inactive products
                    }
                    
                    // Active / VAT
                    Product prod = new Product(number);
                    prod.setVat("21"); // default VAT
                    
                    // Name & Brand
                    String brand = (String) cob.get("brandDimensionCode");
                    if (brand != null) prod.setGroup(brand.trim());


                    // Barcode
                    String addon = "";
                    String barcode = (String) cob.get("barcode");
                    if (barcode != null && barcode.length() > 0) {
                        prod.setEAN(clean(barcode));
                        if(barcode.length() > 4) {
                        	addon = barcode.substring(barcode.length()-4);
                        }
                    }
                    
                    String desc = (String) cob.get("searchDescription");
                    if (desc != null) {
                   	    prod.setNameSorting(Converter.cleanLitLatEst(desc));
                   	 	 prod.setName(clean( desc.trim() + " " + addon ));
                    }

                    // Unit
                    String unit = (String) cob.get("baseUnitOfMeasure");
                    if (unit != null) prod.setUnit(unit.trim());

                    // Stock
                    if (stocks.get(number) != null) {
                        prod.setStock(stocks.get(number));
                    }

                    // Price
                    Object priceObj = cob.get("unitPrice");
                    if (priceObj != null) {
                        prod.setPrice(Double.parseDouble(priceObj.toString()));
                    }


                  

                    // Units per parcel/pallet
                    Object unitsPerParcel = cob.get("unitsPerParcel");
                    if (unitsPerParcel != null) prod.setInCases(Double.parseDouble(unitsPerParcel.toString()));

                    Object unitsPerPallet = cob.get("unitsPerPallet");
                    if (unitsPerPallet != null) prod.setOnPallet(Double.parseDouble(unitsPerPallet.toString()));

                    Object reorderPoint = cob.get("reorderPoint");
                    if (reorderPoint != null) prod.setOptAmountInUnits(Double.parseDouble(reorderPoint.toString()));

                    

                    tm.put(prod.getName(), prod);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            part++;
        }

        System.out.println("Total products loaded: " + tm.size());
        return tm;
    }

    

    public Vector getCustomerProductsList(String agentCode) {
    	

    	Vector res = new Vector();
    	
    	HashMap<String, CustProd> oldData = null;
    	
    	if("true".equals(getParent().getProps().getProperty("load.oldvec"))) {
    		try {
    			oldData = (HashMap<String, CustProd>)Converter.readObject("data/navicustprods.vec");
    		}catch(Exception e) {e.printStackTrace();}
    		}

    	
    	// siia tuleb teha laadimine et saaks tagastus dokumentte toimetada.
    	
                WeekFields weekFields = WeekFields.ISO;
                LocalDate currentWeekStart =
                        LocalDate.now().with(DayOfWeek.MONDAY);

                for (int r = 0; r < 20; r++) {

                    LocalDate weekDate = currentWeekStart.minusWeeks(r);

                    int weekNumber =
                            weekDate.get(weekFields.weekOfWeekBasedYear());

                    int weekYear =
                            weekDate.get(weekFields.weekBasedYear());

               


                    File fil = new File("data/GetSalesHistory" + weekNumber + weekYear + ".json");

                    try {
                    	if(oldData != null) {
                    	System.out.println("oldata enne " + oldData.size());
                    	}
                 
                    	if(fil.exists()) {
                    	org.json.simple.JSONObject hisData =
                                 (org.json.simple.JSONObject) Converter.readJSONFile(fil.getPath());

                    	
                        org.json.simple.JSONArray hisValues =
                                (org.json.simple.JSONArray) hisData.get("data") ;

                        for (int i = 0; i < hisValues.size(); i++) {
                            org.json.simple.JSONObject hisObj =
                                    (org.json.simple.JSONObject) hisValues.get(i);

                            String cuid = (String) hisObj.get("customerShipToCode");
                            String shipmentNo = (String) hisObj.get("shipmentNo");
                            String postingDate = (String) hisObj.get("postingDate");
                            String prid = (String) hisObj.get("itemNo");
                            Object qtyObj = hisObj.get("salesAmount");
                            Object price = hisObj.get("salesPrice");

                            Object qtyObj2 = hisObj.get("quantity");
                            if (prid != null && qtyObj2 != null) {
                                double qty = Double.parseDouble(qtyObj2.toString());
                                double hind = Double.parseDouble(price.toString());
                                CustProd cp = new CustProd(cuid, prid, qty ,0 ,0,postingDate,hind,0,0," ");
                                cp.setAbiKood(shipmentNo);
                          	    res.add(cp); 
                    	     
                          	    // võtame maha vanast kui on juba müüdud
                          	    if(oldData != null && oldData.get(cuid + prid) != null) {
                          	    	oldData.remove(cuid + prid);
                          	    }
                          	    
                            }
                          }
                    	}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            
                if(oldData != null && oldData.size() > 0) {
                	for(CustProd cp : oldData.values()) {
                		res.add(cp);
                	}
                	System.out.println("oldata pärast " + oldData.size());
                }

        
    	return res; 
    }
    
    public Vector getPriceMatrixData() {

        Vector lines = new Vector();

        try {

            int part = 1;

            while (true) {

                File f = new File("data/priceList" + part + ".json");
                if (!f.exists()) break;

                System.out.println("Loading salesPrices file: " + f.getName());

                org.json.simple.JSONArray values =
                        (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(f.getPath());

                for (Object obj : values) {

                    org.json.simple.JSONObject cob =
                            (org.json.simple.JSONObject) obj;

                    MatrixLine ml = new MatrixLine();

                    // ===== CLEAN itemNo =====
                    String itemNo = clean((String) cob.get("itemNo"));
                    if (itemNo == null || itemNo.length() == 0) {
                        continue;
                    }

                    // ===== CLEAN priceGroupCode =====
                    String priceGroupCode = (String) cob.get("priceGroupCode");
                    String promotionCode = (String) cob.get("promotionCode");
                    String sDate = (String) cob.get("startingDate");
                    String eDate = (String) cob.get("endingDate");
                       
                    // ===== READ price =====
                    Object priceObj = cob.get("unitPrice");
                    Double price = 0.0;

                    if (priceObj != null) {
                        try {
                            price = Double.parseDouble(priceObj.toString());
                        } catch (Exception ignored) {}
                    }

                    // ===== SET MatrixLine =====
                   if(  priceGroupCode != null && priceGroupCode.trim().length() > 0) {
                     ml.setCliGroup(priceGroupCode);
                   }
                   if(  promotionCode != null && promotionCode.trim().length() > 0) {
                       ml.setStartDate(sDate);
                       ml.setEndDate(eDate);
                       ml.setLineinfo(promotionCode);
                   }
                    ml.setProdID(itemNo);
                    ml.setPrice(price);
                    lines.add(ml);

                }

                part++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
  System.out.println("lines " + lines.size());
        return lines;
    }

    private Vector debt = null;

    public Vector getDebt(String agentCode){
 			 return debt;
 	   }
    
    
    
    @Override public String getCustProdDiscounts(Agent agent) { return ""; }
    @Override public String getAssortmentStr(Agent agent) { return ""; }
    @Override public String getStartingTime() { return ""; }
    @Override public String getDealerCustomerCSV(String agentCode) { return ""; }
    @Override public String getDriverVersion() { return "BC365Valdo"; }
    @Override public String getSubjectFilesTo() { return ""; }
    @Override public double getSalary(String agentCode) { return 0.0; }
    @Override public void connectionUp() {}
    @Override public void connectionDown() {}
    @Override public String getPilotDayData(Agent pilot) { return ""; }
    @Override public String getDistribution() { return ""; }
    @Override public String getSaveStockInfo() { return ""; }
    @Override public String getSaveDebtInfo() { return ""; }
    
    @Override public TreeMap getProdGroups() throws SQLException { 
    	TreeMap tm = new TreeMap();
    	  int part = 1;
          while (true) {
              File itemFile = new File("data/items" + part + ".json");
              if (!itemFile.exists()) break;

              System.out.println("Processing product file: " + itemFile.getName());

              try {
                  org.json.simple.JSONArray values =
                          (org.json.simple.JSONArray) Converter.readJSONArrFileUTF(itemFile.getPath());

                  for (int i = 0; i < values.size(); i++) {
                      org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
                      String brand = (String) cob.get("brandDimensionCode");
                      if (brand != null && tm.get(brand) == null) {
                          tm.put(brand, brand);  
                      }
                  }

              } catch (Exception e) {
                  e.printStackTrace();
              }
              part++;
          }

    	
    	return tm; 
    	
    }



	@Override
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBulkSales(Map agents) {
		// TODO Auto-generated method stub
		
	}
}
