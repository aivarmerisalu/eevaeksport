/*
 * Created on 14.11.2005
 */
package eevaeksport.consolidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */


import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.EksportFrame;

public class Consolidator {

  private Properties props = null;
  static Category log = Category.getRoot();

  private Calendar cal = Calendar.getInstance();

  public Consolidator() throws IOException{
    props = new Properties();
    props.load(new FileInputStream(EksportFrame.INI_FILE));
        PropertyConfigurator.configure("log4j.properties");
  }

  private void execute() throws Exception{

  if(true){
//sisend failide asukoht
    String dirin = props.getProperty("consol.in");
    if (!(dirin.endsWith("/") || (dirin.endsWith("\\")))) dirin = dirin + "/";

//v�ljundfaili asukoht
    String dirout = props.getProperty("consol.out");
    if (!(dirout.endsWith("/") || (dirout.endsWith("\\")))) dirout = dirout + "/";
    
    
//renamemise directory
    String dirren = props.getProperty("consol.ren");
    if (dirren !=null && !(dirren.endsWith("/") || (dirren.endsWith("\\")))) dirren = dirren + "/";
    
    String [] names = new File(dirin).list();
	String lineSep = System.getProperty("line.separator");
	
	int cut = 0;
	String cuts = props.getProperty("consol.linecut");
	if(cuts != null){
		cut = Integer.parseInt(cuts);
	}
	
    for (int i = 0 ; i < names.length ; i++){
	
		if (names[i].endsWith(props.getProperty("consol.sec"))){

		    FileReader fr = new FileReader(dirin + names[i]);
		    BufferedReader reader = new BufferedReader(fr);
		    String line;
		    String input = "";
		    String add = "";
		    if( "true".equals(props.getProperty("consol.addon"))){
		    	add = cal.getTimeInMillis()+ "";
		    }
		    
		    File out = new File(dirout + add + props.getProperty("consol.file"));
		    System.out.println("out " + out.exists() + " " + dirin + names[i] + " " + cut);
		    if(out.exists()){   
		    for (int j = 0;j < cut ;j++){
		    	reader.readLine();
		     }
		    if("true".equals(props.getProperty("consol.nospaces"))){
		    	
		    }else{
		    	  input = input + lineSep ;	
				    input = input + lineSep ;
		    }
		  	
		    }
		    while((line = reader.readLine()) != null){
		    	if(line.trim().length()>0){
		    		input = input + line + lineSep ;	
		      }	 else {
		      	input = input + lineSep;
		      }
		    }
		    reader.close();
		    fr.close();
		    writeConsloStr(dirout + add + props.getProperty("consol.file"),input);
	    	File f = new File(dirin + names[i]);
		    if(dirren!=null){
		    	f.renameTo(new File(dirren + names[i]));
		    	if(f.exists()){
		    		f.delete();
		    	}
		    }else{
		        f.delete();	
		    }
		}	
	  }
    }  
  }
  
  
  
	public void writeConsloStr(String path,String e) {
		try {
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

}
  
	
   public void log(String s) {
     log.info(s);
   }


  public static void main(String[] args) throws Exception{
    Consolidator consol = new Consolidator();
    consol.execute();
  }
}
