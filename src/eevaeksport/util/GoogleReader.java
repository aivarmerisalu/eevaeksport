package eevaeksport.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GoogleReader {

	public GoogleReader(String key,String query){
		this.key = key;
		this.query = query;
	}

	
	public String getData(){
		
	      StringBuffer inputLineBuf = new StringBuffer();
		try{
			String httpsURL = "https://spreadsheets.google.com/tq?tqx=out:c&tq=" + query + "&key=" + key;
			System.out.println("saadetud " + httpsURL);
	        URL myurl = new URL(httpsURL);
	        
	        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
	        InputStream ins = con.getInputStream();
	        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	        BufferedReader in = new BufferedReader(isr);

	        String inputLine;	  
	        while ((inputLine = in.readLine()) != null){	        
	        	inputLineBuf.append(inputLine);
	         }
	        in.close();
        
		}catch(Exception e){
			e.printStackTrace();
		}
				
		return inputLineBuf.toString();
	}


		private String key = null;
		private String query = null;
		private URL mFormUri = null;
	
}
