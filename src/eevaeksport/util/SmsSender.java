package eevaeksport.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

public class SmsSender {


	public void send(String messageid,String number, String msg) throws Exception {

		Runtime run = Runtime.getRuntime();
		String[] cmds = new String[]{"C:/Program Files/nodejs/node.exe", "cert/zpienas/smssender.js",messageid, number, msg};

		ProcessBuilder pb = new ProcessBuilder(cmds);
		 pb.redirectErrorStream(true);
		//pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		Process p = pb.start();
		
		 BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String line;
		    String result = "\nEvent time "  + new Date().toLocaleString();
		    while (true) {
		        line = r.readLine();
		        if (line == null) {
		            break;
		        }
		        result = result + "\n  " + line;
		            System.out.println(line);
		        
		    }
		    
		    if(result.indexOf("TIMEOUT") != -1) {
              throw new Exception("timeout");		    	
		    }else {
			    writeConsloStr("log/smslog.txt",result);
		    }


	}

	public static void main(String[] args) throws Exception{
		SmsSender sender = new SmsSender();

		String messID = Calendar.getInstance().getTimeInMillis()+"";
	sender.send(messID ,"37069666307", "Hi from server. messID-" + messID);

	}
	
private void writeConsloStr(String path,String e) {
		
		try {
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
}
}
