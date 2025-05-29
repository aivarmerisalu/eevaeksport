package eevaeksport.util;



import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eevaeksport.Product;


public class ReportingService {
	
	private CloseableHttpClient client;
	
	
	
	public boolean uploadFile(String url,String fileName , String itemID, String module){
		boolean res = false;
		
		
		  
  		try {
  		  
            HttpPost post = new HttpPost( url);
            System.out.println("pildi saatmine 333" + fileName);
            File file = new File(fileName);
            HttpResponse response = null;
            if(file.exists()){
            	FileBody bin = new FileBody(file);

                HttpEntity  reqEntity=  null;
                if(itemID != null) {
                	reqEntity =MultipartEntityBuilder.create()
                    .addPart("file", bin)
                    .addPart("itemId", new StringBody(itemID, ContentType.TEXT_PLAIN))
                    .addPart("module", new StringBody(module, ContentType.TEXT_PLAIN))
                    .build();
                }else {
                	reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)
                 
                    .addPart("module", new StringBody(module, ContentType.TEXT_PLAIN))
                    .build();
                }
                
                
                post.addHeader("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\"");
                post.setEntity(reqEntity);
                System.out.println("executing request " + post.getRequestLine());
                  response = getClient().execute(post);  
                
            } 
            
            
            if(response!=null){
                HttpEntity responseEntity = response.getEntity();
                if(responseEntity!=null) {
                	String asoi = EntityUtils.toString(responseEntity);
                	 if(asoi.indexOf("Error") != -1){
                		 System.out.println("Faili Postimise tulemus vigane teade " + asoi);
                		 
                	 }else{
                		 System.out.println(" Falil Postimise tulemus OK " + asoi );
                		 res = true;
                	 }
               }
            }
  			

        } catch(Exception e) {
            System.out.println("Postimine t�itsa vigane ");
            e.printStackTrace();
        }
		
		
		
		
		return res ;
	}
	
	public JSONArray getContracts() throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/contract/list?showActive=true";
		if(https) {
			web = "https://api.mobitrek.eu/rest/contract/list?showActive=true";
		}
		
		HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
		//System.out.println("ahaaa Contracts " + aaa);
		JSONArray json = new JSONArray(aaa);
		return json;
	}

	public JSONArray getCustomers(String user) throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/customer/list";
		if(https) {
			web = "https://api.mobitrek.eu/rest/customer/list";
		}
		
		if(user != null){
			web = "http://merisoft.eu:8080/rest/customer/byManagerCode/" + user;
			if(https) {
				web = "https://api.mobitrek.eu/rest/customer/byManagerCode/" + user;
			}
		}
		
		
		HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
		//System.out.println("ahaaa Customers " + aaa);
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	
public JSONArray getSVdata(Date from , Date to , String type , String subTypes) throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/supervisor?from=" + from.getTime() + "&to=" + to.getTime() ;
		if(https) {
			web = "https://api.mobitrek.eu/rest/supervisor?from=" + from.getTime() + "&to=" + to.getTime() ;
		}
		if(type != null) {
			web = web + "&type=" + type;
		}
		
		if(subTypes != null) {
			StringTokenizer st = new StringTokenizer(subTypes , ",");
			while(st.hasMoreTokens()) {
				web = web + "&subType=" + st.nextToken();	
			}
		}
	
		HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
	//	System.out.println("ahaaa SV data " + aaa);
		JSONArray json = new JSONArray(aaa);
		return json;
		
	}


public JSONArray getQuestdata() throws Exception{
	
	String web = "http://merisoft.eu:8080/rest/questionnaire/list?active=true" ;
	if(https) {
		web = "https://api.mobitrek.eu/rest/questionnaire/list?active=true" ;
	}
	HttpClient cli = getClient();
	HttpGet aa = new HttpGet(web);
	//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
	HttpResponse resp = cli.execute(aa);
	String aaa = EntityUtils.toString(resp.getEntity());
//	System.out.println("ahaaa SV data " + aaa);
	JSONArray json = new JSONArray(aaa);
	return json;
	
}


public JSONArray getTaskdata(Date from , Date to ) throws Exception{
	
	String web = "http://merisoft.eu:8080/rest/task/list?allManagers=true&from=" + from.getTime() + "&to=" + to.getTime() ;
	if(https) {
		web = "https://api.mobitrek.eu/rest/task/list?allManagers=true&from=" + from.getTime() + "&to=" + to.getTime() ;
	}
	HttpClient cli = getClient();
	HttpGet aa = new HttpGet(web);
	//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
	HttpResponse resp = cli.execute(aa);
	String aaa = EntityUtils.toString(resp.getEntity());
//	System.out.println("ahaaa SV data " + aaa);
	JSONArray json = new JSONArray(aaa);
	return json;
	
}

	

public JSONArray getCustomersChains() throws Exception{
	String web = "http://merisoft.eu:8080/rest/customer/chained";
	if(https) {
		web = "https://api.mobitrek.eu/rest/customer/chained";
	}
	HttpClient cli = getClient();
    HttpResponse resp = cli.execute(getReq(web));
    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
	return json;
}

public JSONArray getUploadedFileList(Date algus, Date lopp) throws Exception{
	
	String web = "http://merisoft.eu:8080/rest/fileUpload/list";
	if(https) {
		web = "https://api.mobitrek.eu/rest/fileUpload/list";
	}
	if(algus != null) {
		web = web + "?from=" + algus.getTime();
	}
	
	if(lopp != null) {
		web = web + "to=" + lopp.getTime();
	}
	
	HttpClient cli = getClient();
    HttpResponse resp = cli.execute(getReq(web));
    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
	return json;
}

public JSONArray getVisitedList(String status) throws Exception{
	String web = "http://merisoft.eu:8080/rest/visitlinesBy?contactstatus=" + status + "&days=60"; // visitlinesBy?contactstatus=plan&days=30
	if(https) {
		web = "https://api.mobitrek.eu/rest/visitlinesBy?contactstatus=" + status + "&days=60";
	}
	HttpClient cli = getClient();
    HttpResponse resp = cli.execute(getReq(web));
  //  System.out.println(EntityUtils.toString(resp.getEntity()));
    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
	return json;
}

public JSONArray getCustomersLevel() throws Exception{
	String web = "http://merisoft.eu:8080/rest/customer/levelled";
	if(https) {
		web = "https://api.mobitrek.eu/rest/customer/levelled";
	}
	HttpClient cli = getClient();
	HttpResponse resp = cli.execute(getReq(web));
    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
	return json;
}
public JSONArray getCustomersTypes() throws Exception{
	String web = "http://merisoft.eu:8080/rest/customer/typed";
	if(https) {
		web = "https://api.mobitrek.eu/rest/customer/typed";
	}
	HttpClient cli = getClient();
	HttpResponse resp = cli.execute(getReq(web));
    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
	return json;
}

	public JSONArray getCustomersAll() throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/customer/allCustomers";
		if(https) {
			web = "https://api.mobitrek.eu/rest/customer/allCustomers";
		}
		HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
	//	System.out.println("ahaaa CustomersAll " + aaa.toString());
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	
	public JSONArray getDayReport(long timestampstart, long timestampend) throws Exception{
		String web = "http://merisoft.eu:8080/rest/dayreport?from=" + timestampstart + "&to=" + timestampend;
		if(https) {
			web = "https://api.mobitrek.eu/rest/pod/dayreport?from=" + timestampstart + "&to=" + timestampend;
		}
		HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
		//System.out.println("ahaaa Dayreport " + aaa.toString());
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	
	
	
	public Vector getCustomersCodes() throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/customer/allCodes";
		if(https) {
			web = "https://api.mobitrek.eu/rest/customer/allCodes";
		}
		
		HttpClient cli = getClient();
		
		HttpResponse resp = cli.execute(getReq(web));
	String aaa = EntityUtils.toString(resp.getEntity());
	  
	 aaa = aaa.substring(1,aaa.length()-1);
	
	Vector json = new Vector();
	      StringTokenizer st = new StringTokenizer(aaa,",");
	      while(st.hasMoreTokens()){
	    	 json.add(st.nextToken().replace('\"',' ').replace('\"',' ').trim()) ;
	      }
	    //  System.out.println("Customers " +	json.toString());
		return json;
	}
	
	
public Vector getPodsCodes() throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/pod/allCodes";
		if(https) {
			web = "https://api.mobitrek.eu/rest/pod/allCodes";
		}
		
		HttpClient cli = getClient();
		
		HttpResponse resp = cli.execute(getReq(web));
	String aaa = EntityUtils.toString(resp.getEntity());
	  
	 aaa = aaa.substring(1,aaa.length()-1);
	
	Vector json = new Vector();
	      StringTokenizer st = new StringTokenizer(aaa,",");
	      while(st.hasMoreTokens()){
	    	 json.add(st.nextToken().replace('\"',' ').replace('\"',' ').trim()) ;
	      }
	    //  System.out.println("Customers " +	json.toString());
		return json;
	}
	
	
	private HttpGet getReq(String url){
		 HttpGet ooo = new HttpGet(url);
		 ooo.addHeader("Merisoft-Company",company);
		 ooo.addHeader("Merisoft-ManagerCode",user);
		 return ooo;
	 }
	
	
	public JSONArray getAssets(String user) throws Exception{
		
		 String url = "http://merisoft.eu:8080/rest/countit/items";
		 if(https) {
				url = "https://api.mobitrek.eu/rest/countit/items";
			}
		HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpGet(url));
	    JSONArray json = new JSONArray(EntityUtils.toString(resp.getEntity()));
		return json;
	}
	
	public JSONArray getDocumentsShipit(long timestamp) throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/shipit/list";
		if(https) {
			web = "https://api.mobitrek.eu/rest/shipit/list";
		}
		if(timestamp > 0){
			web = "http://merisoft.eu:8080/rest/shipit/list?allManagers=true&date=" + timestamp;
			if(https) {
				web = "https://api.mobitrek.eu/rest/shipit/list?allManagers=true&date=" + timestamp;
			}
		}
		HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpGet(web));

		String aaa = EntityUtils.toString(resp.getEntity());
		System.out.println("Docs Shipit " + aaa + " \n" +web);
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	
	
public JSONObject getManager(String user) throws Exception{
		
		String web = "http://merisoft.eu:8080/rest/manager/list";
		if(https) {
			web = "https://api.mobitrek.eu/rest/manager/list";
		}
		if(user != null){
			web = "http://merisoft.eu:8080/rest/manager/byCode/" + user;
			if(https) {
				web = "https://api.mobitrek.eu/rest/manager/byCode" + user;
			}
		}
		
	
		HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpGet(web));
		String aaa = EntityUtils.toString(resp.getEntity());
		
		JSONObject json = null ;
	    try{
	    	 json =  new JSONObject(aaa);  	
	      }catch(Exception e){
	    	throw new Exception(aaa);
	      }
	  
		return json;
	}

public boolean https = false;

public JSONArray getManagerList() throws Exception{
	
	String web = "http://merisoft.eu:8080/rest/manager/list";
	
	if(https) {
		web = "https://api.mobitrek.eu/rest/manager/list";
	}

	HttpClient cli = getClient();
	HttpResponse resp = cli.execute(new HttpGet(web));
	String aaa = EntityUtils.toString(resp.getEntity());
	
	JSONArray json = null ;
    try{
    	 json =  new JSONArray(aaa);  	
      }catch(Exception e){
    	  e.printStackTrace();
    	throw new Exception(aaa);
      }
  
	return json;
}

	

public JSONArray getPackgages() throws Exception{
	
String web = "http://merisoft.eu:8080/rest/package/list";
if(https) {
	web = "https://api.mobitrek.eu/rest/package/list/";
}


	HttpClient cli = getClient();
	HttpResponse resp = cli.execute(new HttpGet(web));
	String aaa = EntityUtils.toString(resp.getEntity());
	//System.out.println("ahaaa pack " + aaa);
	JSONArray json = new JSONArray(aaa);
	return json;
}
	

    public JSONArray getProducts() throws Exception{
		
	String web = "http://merisoft.eu:8080/rest/product/list";
	if(https) {
    	web = "https://api.mobitrek.eu/rest/product/list/";
	}
	
		HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpGet(web));
		String aaa = EntityUtils.toString(resp.getEntity());
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	

 public String updatePods(String code,String tag ,String location) throws Exception{
	  
	    String add = "";
	    if(tag != null ){
	    	add = "tag=" + tag;
	    }
	    if(location != null){
	    	if(add.length() ==  0){
	    		add = "location="+ location;
	    	}else{
	    		add = add + "&location="+ location;
	    	}
	    }
	 
    	String web = "http://merisoft.eu:8080/rest/pod/updateFields/" + code + "?" + add; //tag=" + tag + "&location=" + location;
    
	 	if(https) {
	    	web = "https://api.mobitrek.eu/rest/pod/updateFields/" + code + "?" + add;
		}
		
    	HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpPost(web));
		String aaa = EntityUtils.toString(resp.getEntity());
		return aaa;
	}
 
 //Erply Driveris on getASI tehtud ..
 
 public String addNewShipDoc(String origid ,String route ,String completedMsisdn , Date completedDate ,
		 String completedComment, String docType , Vector lines , String podid , Date deliveryDate, 
		 String extDoc, String priority) throws Exception{
	 String add = null;
	    if(origid != null && !origid.equals("-1")){
	    	add =  "parent: {id: " + origid  + "} " ;
	    }
	    
	    if(podid != null){
	    	if(add != null){
	    		add = add + ", pod: {code: \""+ podid + "\"}";
	    	}else{
	    		add = " pod: {code: \""+ podid + "\"}";
	    	}
	    	
	    }
	    
	    if(priority != null){
	    	if(add != null){
	    		add = add + ", priority: \""+ priority + "\"";
	    	}else{
	    		add =  " priority: \""+ priority + "\"";	
	    	}
	    	
	    }
	    
	    
	    
	    if(completedDate != null){
	    	if(add != null){
	    		add = add + ", completedDate: " + completedDate.getTime() ;	
	    	}else{
	    		add = " completedDate: " + completedDate.getTime() ;
	    	}
	    	
	    }
	    
	    if(deliveryDate != null){
	    	add = add + ", deliveryDate: " + deliveryDate.getTime();
	    }
	    
	    if(route != null){
	    	add = add + ", cargoRoute: {code: \"" + route + "\"}";
	    }
	   
	    if(completedComment != null){
	    	add = add + ", completedComment: \""+ completedComment + "\"";
	    }
	    
	    if(extDoc != null){
	    	add = add + ", extDocNum: \""+ extDoc + "\"";
	    }
	   
	    if(docType != null){
	    	add = add + ", docType: \""+ docType + "\"";
	    }

	    if(completedMsisdn != null){
	    	add = add + ", completedMsisdn: \""+ completedMsisdn + "\"";
	    }
	    
	    
	     
	    for(int i = 0;i< lines.size(); i++){
	    	Product tt = (Product)  lines.elementAt(i);
	    	if(i != 0){
	    		add = add + ", "; 
	    	}else{
	    		  add = add + ", lines: [";
	    	}
	    	add = add + "{ pack: {code: \"" + tt.getID() + "\"} , amount: " + tt.getInCases() +"}";
	    	if(i == lines.size()-1){
	    		 add = add + " ]";
	    	}
	    }
	    
	    
	    add = "{" + add + "}";
	   
	    //System.out.println("add : " + add);
	 	HttpClient cli = getClient();
	 	
	 	String web = "http://merisoft.eu:8080/rest/shipit/insertDoc";
	 	if(https) {
	    	web = "https://api.mobitrek.eu/rest/shipit/insertDoc";
		}
		HttpResponse resp = restPostRequest(web, add, cli);
		String aaa = EntityUtils.toString(resp.getEntity());
		//System.out.println("Response " + aaa);
		return aaa;
	}


 static HttpResponse restPostRequest(String uri, String json, HttpClient client) throws Exception {


    	Converter.doTrustToCertificates();
    	Converter.trustAllHttpsURLConnection();
	 HttpPost httpPost = new HttpPost(uri);
	 if(json != null){
		 httpPost.setEntity(new StringEntity(json,"utf-8"));
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
	 }
        return client.execute(httpPost);

}
 

 public String postJson(String json,String url ) throws Exception{
	 	HttpClient cli = getClient();
		HttpResponse resp = restPostRequest(url, json, cli);
		String aaa = EntityUtils.toString(resp.getEntity());
		System.out.println("Response " + aaa);
		return aaa;
	}
 
 public String updateShipDocs(String id ,String completedMsisdn , Date completedDate ,
		 String completedComment) throws Exception{
	 
	    String add = "{";
	    if(id != null ){
	    	add = add + "docId: " + id ;
	    }
	    
	    if(completedDate != null){
	    	add = add + ", completedDate: " + completedDate.getTime();
	    }
	   
	    if(completedComment != null){
	    	add = add + ", completedComment: \""+ completedComment + "\"";
	    }
	    

	    if(completedMsisdn != null){
	    	add = add + ", completedMsisdn: \""+ completedMsisdn + "\"";
	    }
	    
	    add = add + "}";
	    
	 	HttpClient cli = getClient();
	 	String web = "http://merisoft.eu:8080/rest/shipit/complete";
	 	if(https) {
	    	web = "https://api.mobitrek.eu/rest/shipit/complete";
		}
		HttpResponse resp = restPostRequest(web, add, cli);
		String aaa = EntityUtils.toString(resp.getEntity());
	//	System.out.println("teen completet " +add + " " + aaa);
		return aaa;
	}


 


 
 public String updateCustomers(String code,String tag ,String location) throws Exception{
	  
	    String add = "";
	    if(tag != null ){
	    	add = "tag=" + tag;
	    }
	    if(location != null){
	    	if(add.length() ==  0){
	    		add = "location="+ location;
	    	}else{
	    		add = add + "&location="+ location;
	    	}
	    }
	 
 	String web = "http://merisoft.eu:8080/rest/customer/updateFields/" + code + "?" + add; //tag=" + tag + "&location=" + location;
 	if(https) {
    	web = "https://api.mobitrek.eu/rest/customer/updateFields/" + code + "?" + add;
	}
 	HttpClient cli = getClient();
		HttpResponse resp = cli.execute(new HttpPost(web));
		String aaa = EntityUtils.toString(resp.getEntity());
		return aaa;
	}

 
 
    
    public JSONArray getPods(String filter) throws Exception{
		
    	String web = "http://merisoft.eu:8080/rest/pod/list";
    	if(https) {
	    	web = "https://api.mobitrek.eu/rest/pod/list";
		}
		
    	if(filter != null){
    		web = "http://merisoft.eu:8080/rest/pod/byRouteCode/"+ filter;
    		if(https) {
    	    	web = "https://api.mobitrek.eu/rest/pod/byRouteCode/"+ filter;
    		}
    	}
	
    	HttpClient cli = getClient();
		HttpGet aa = new HttpGet(web);
		//aa.setHeader (HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		HttpResponse resp = cli.execute(aa);
		String aaa = EntityUtils.toString(resp.getEntity());
		//System.out.println("ahaaa PODS " + aaa);
		JSONArray json = new JSONArray(aaa);
		return json;
	}
	
    
	
	private HttpClient getClient() throws Exception{
		if (client == null){
			client =  HttpClients.createDefault();
			
			if(https) {
				client = TrustAllCertsApache.createHttpClient_AcceptsUntrustedCerts();
			}
			login();
		}
		
		return client;
	}
	
	public void closeClient(){
		if(client != null){
			
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    client = null;
		}		
	}
	
	private void login() throws Exception{

		String login = "{username: \"" + user + "\", password: \"" + pass + "\", company: \"" + company + "\"}";
		String url = "http://merisoft.eu:8080/rest/login";
		if(https) {
	    	url = "https://api.mobitrek.eu/rest/login";
		}
		HttpResponse resp = restPostRequest(url, login, client);
		if (resp.getStatusLine().getStatusCode() != 200) throw new Exception("Login failed! " + login);
		//System.out.println( EntityUtils.toString(resp.getEntity()));
	}
	
	
	
	public String user = null;
	public String pass = null;
	public String company = null;
	
	
	
}


