/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ZipHandlerPass {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ZipHandlerPass(EksportPanel parent) {
	this.parent = parent;
    }

    public ZipHandlerPass(){
    }

    /**
     * Method declaration
     *
     *
     * @param fileName
     * @param content
     *
     * @see
     */
    public void addFile(String fileName, String content) {

	entries.put(fileName, content);
    }

    public void addFile(String fileName){
      try{
        entries.put(fileName, "file");
      } catch (Exception e){
        e.printStackTrace();
      }
    }
    public void addFolder(String folderName){
        try{
          entries.put(folderName, "folder");
        } catch (Exception e){
          e.printStackTrace();
        }
      }

    /**
     * Method declaration
     *
     *
     * @param fileName
     *
     * @see
     */
    public void writeZip(String fileName, String password) {
	try {
		
		
		
		//kui on Pass siis s�time parameetrid 
		
		File aa = new File(fileName);
		if(aa.exists()){
			aa.delete();
		}
		ZipFile zipFile = new ZipFile(fileName);
		
		if((parent.getProps().getProperty("files.secure")) != null){
			password = parent.getProps().getProperty("files.secure");
		}

       Enumeration      keys = entries.keys();
	    Enumeration      elems = entries.elements();

	    while (keys.hasMoreElements() && elems.hasMoreElements()) {
		String fname = (String) keys.nextElement();
		String   content = (String) elems.nextElement();
		
		
		if(!content.equals("file") && !content.equals("folder")){
			
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			if( password != null ){
			
				parameters.setEncryptFiles(true);
				parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
				parameters.setPassword(password);
					
			}
			
			parameters.setFileNameInZip(fname);
			parameters.setSourceExternalStream(true);
			if("true".equals(parent.getProps().getProperty("files.toUTF"))){
				InputStream in = IOUtils.toInputStream(content, "UTF-8");
				zipFile.addStream(in, parameters);
			}else{
				InputStream in = IOUtils.toInputStream(content);
				zipFile.addStream(in, parameters);
			}
				
			
		
			
		}else{
			
			
			String name = fname;
			File ff = new File(name);
			if(!ff.exists()){
				name = "settings/"+name;
			}
			ff =  new File(name);
			
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to store compression
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			if( password != null ){
				parameters.setEncryptFiles(true);
				parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
				parameters.setPassword(password);
			}
			try{
			//	System.out.println("ohoou " + ff);
				
				if(content.equals("folder")) {
					zipFile.addFolder(name, parameters);
				  } else {
					zipFile.addFile(ff, parameters);
				}
				
			}catch(Exception r){
				//System.out.println("ohoo " + ff);
				r.printStackTrace();
			}
				
		}

		
		
    		
	    }

	  
	} catch (IOException e) {
	    parent.showError(e);
	} catch (ZipException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }

    
    
 
    public void writeToPath(String path,String password){
        writeZip(path,password);
      }

    public void write(String agent,String password){
      writeZip(agent + ".zip", password);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    Hashtable    entries = new Hashtable();
    EksportPanel parent = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-25-2002 ---*/

