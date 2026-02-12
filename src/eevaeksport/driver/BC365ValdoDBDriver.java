package eevaeksport.driver;

import eevaeksport.Agent;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class BC365ValdoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter{

    public BC365ValdoDBDriver(EksportPanel parent) {
        super(parent);
        this.parent = parent;
        //getCustomers(null);
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
            "GetShipToAddress",
            "GetShipToAddressRoute"
    };

    /** Paging rule */
    private static final int MAX_OBJECTS_PER_FILE = 250;

    /* =========================
       Public API
       ========================= */

    public void runAllExports() {
        downloadData(null);
    }

    public void downloadData(String operation) {
        if (operation == null) {
            for (String op : operations) {
                downloadData(op);
            }
            return;
        }

        try {
            String json = callOperation(operation);

            // 🔴 CHANGED LINE (paging-aware save)
            saveFilePaged(operation, json, MAX_OBJECTS_PER_FILE);

        } catch (Exception e) {
            System.err.println("Failed to download data for operation: " + operation);
            e.printStackTrace();
        }
    }

    /* =========================
       PAGED FILE SAVE
       ========================= */


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

    /**
     * Splits a JSON array into individual object strings
     * No external libraries, brace-counting only
     */
    private List<String> splitJsonArray(String jsonArray) {
        List<String> result = new ArrayList<>();

        int braceLevel = 0;
        int start = -1;

        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);

            if (c == '{') {
                if (braceLevel == 0) start = i;
                braceLevel++;
            } else if (c == '}') {
                braceLevel--;
                if (braceLevel == 0 && start >= 0) {
                    result.add(jsonArray.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    /* =========================
       CORE NTLM SOAP CALL
       ========================= */

    public String callOperation(String operation) throws Exception {

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

    private String extractReturnValue(InputStream xmlStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);

        NodeList nodes = document.getElementsByTagName("return_value");

        if (nodes.getLength() == 0) {
            throw new RuntimeException("SOAP response does not contain <return_value>");
        }

        StringBuilder sb = new StringBuilder();
        NodeList children = nodes.item(0).getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeValue() != null) {
                sb.append(children.item(i).getNodeValue());
            }
        }
        return sb.toString().trim();
    }

    /* =========================
       ABSTRACT METHODS (stubs)
       ========================= */

    public TreeMap getAgents(){
		  TreeMap tm = new TreeMap();
	
			try {
			org.json.simple.JSONArray cc = 	Converter.readJSONArrFile("data//Agents1.json");
	
			
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
			
	  //System.out.println("Managersize " + tm.size());
	
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
           // downloadData(null);
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
                (org.json.simple.JSONArray) Converter.readJSONArrFile("data/customerEntries" + debtPart + ".json");

        int lenn1 = values1.size();
        debtPart++;
        try {

            for (int i = 0; i < lenn1; i++) {

                org.json.simple.JSONObject cob =
                        (org.json.simple.JSONObject) values1.get(i);

                // ===== NEW FIELD MAPPING =====

                String custid = clean((String) cob.get("customerShipToCode"));
                String payer = custid; // no separate payer in new JSON

                Double balDue = 0.0;
                Object amountObj = cob.get("remainingAmount");
                if (amountObj != null) {
                    try {
                        balDue = Double.parseDouble(amountObj.toString());
                    } catch (Exception ignored) {}
                }

                String postingDate = (String) cob.get("postingDate");
                String invnr = clean((String) cob.get("externalDocumentNo"));
                if (invnr.length() == 0) {
                	invnr = "No Number";
                }

                // ===== CREATE DEBT OBJECT =====

                Debt dd = new Debt();
                dd.setCustID(custid);
                dd.setPayerID(payer);
                dd.setDoknr(invnr);

                Date invDateObj = getDate(postingDate);
                dd.setDate(invDateObj);

                // No dueDate in new JSON → use postingDate as fallback
                dd.setDuedate(invDateObj);

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
                    (org.json.simple.JSONArray) Converter.readJSONArrFile(file.getPath());

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
                    (org.json.simple.JSONArray) Converter.readJSONArrFile(file.getPath());

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
                    (org.json.simple.JSONArray) Converter.readJSONArrFile(file.getPath());

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
                        cust.setAddress(clean(address));
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
                    if (priceGroup != null) cust.setGroup(priceGroup.trim());

                    String storeCat = (String) cob.get("storeCatDimensionCode");
                    if (storeCat != null && storeCat.trim().length() > 0) cust.setChain(storeCat.trim());

                    Object consignationDaysObj = cob.get("consignationDays");
                    if (consignationDaysObj != null) {
                        cust.setPaymTerm(String.valueOf(consignationDaysObj));
                    }

                    // ===== ASSIGN SALESPERSONS BASED ON customerShipToCode =====
                    List<String> salespersons = shipToMap.get(number);
                    System.out.println("Agent " + salespersons);
                    
                    
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

                    cust.addAgent(new Agent("0078"));
                    
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
                        (org.json.simple.JSONArray) Converter.readJSONArrFile(stockFile.getPath());

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
                        (org.json.simple.JSONArray) Converter.readJSONArrFile(itemFile.getPath());

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

                    String desc = (String) cob.get("searchDescription");
                    if (desc != null) prod.setName(clean(desc) + " " + number);

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

                    // Barcode
                    String barcode = (String) cob.get("barcode");
                    if (barcode != null && barcode.length() > 0) {
                        prod.setEAN(clean(barcode));
                    }

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

    
    public Vector getPriceMatrixData() {

        Vector lines = new Vector();

        try {

            int part = 1;

            while (true) {

                File f = new File("data/priceList" + part + ".json");
                if (!f.exists()) break;

                System.out.println("Loading salesPrices file: " + f.getName());

                org.json.simple.JSONArray values =
                        (org.json.simple.JSONArray) Converter.readJSONArrFile(f.getPath());

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
                    String priceGroupCode = clean((String) cob.get("priceGroupCode"));
                    if (priceGroupCode == null || priceGroupCode.length() == 0) {
                        continue;   // skip empty price groups (remove if not wanted)
                    }

                    // ===== READ price =====
                    Object priceObj = cob.get("unitPrice");
                    Double price = 0.0;

                    if (priceObj != null) {
                        try {
                            price = Double.parseDouble(priceObj.toString());
                        } catch (Exception ignored) {}
                    }

                    // ===== SET MatrixLine =====
                    ml.setCliGroup(priceGroupCode);
                    ml.setProdID(itemNo);
                    ml.setPrice(price);
                    lines.add(ml);

                }

                part++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lines;
    }

    private Vector debt = null;

    public Vector getDebt(String agentCode){
 			 return debt;
 	   }
    
    
    
    @Override public String getCustProdDiscounts(Agent agent) { return ""; }
    @Override public String getAssortmentStr(Agent agent) { return ""; }
    @Override public Vector getCustomerProductsList(String agentCode) { return new Vector(); }
    @Override public String getStartingTime() { return ""; }
    @Override public String getDealerCustomerCSV(String agentCode) { return ""; }
    @Override public String getDriverVersion() { return "BC365ValdoDBDriver"; }
    @Override public String getSubjectFilesTo() { return ""; }
    @Override public double getSalary(String agentCode) { return 0.0; }
    @Override public void connectionUp() {}
    @Override public void connectionDown() {}
    @Override public String getPilotDayData(Agent pilot) { return ""; }
    @Override public String getDistribution() { return ""; }
    @Override public String getSaveStockInfo() { return ""; }
    @Override public String getSaveDebtInfo() { return ""; }
    @Override public TreeMap getProdGroups() throws SQLException { return new TreeMap(); }



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
