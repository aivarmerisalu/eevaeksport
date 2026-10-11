/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ZipHandler {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ZipHandler(EksportPanel parent) {
	this.parent = parent;
    }

    public ZipHandler(){
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
	ZipEntry entry = new ZipEntry(fileName);

	entries.put(entry, content);
    }

    public void addFile(String fileName){
      try{
        FileReader fis = new FileReader(fileName);
        BufferedReader in =  new BufferedReader(fis);
        StringBuffer buf = new StringBuffer();
        String line;
        while((line = in.readLine()) != null){
          buf.append(line + System.getProperty("line.separator"));
        }
        in.close();
        File file = new File(fileName);
        ZipEntry entry = new ZipEntry(file.getName());
        entries.put(entry, "file");
       // entries.put(entry, buf.toString());
      } catch (IOException e){
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
    public void writeZip(String fileName) {
	try {
	    FileOutputStream fos = new FileOutputStream(fileName);
	    ZipOutputStream  zos = new ZipOutputStream(fos);
	    Enumeration      keys = entries.keys();
	    Enumeration      elems = entries.elements();

	    while (keys.hasMoreElements() && elems.hasMoreElements()) {
		ZipEntry entry = (ZipEntry) keys.nextElement();
		String   content = (String) elems.nextElement();
		zos.putNextEntry(entry);
		
		if(!content.equals("file")){
			if("true".equals(parent.getProps().getProperty("files.toUTF"))){
				zos.write(content.getBytes(Charset.forName("UTF-8"))); 		
			}else{
				zos.write(content.getBytes());	
			}
			zos.closeEntry();
		}else{
			String name = entry.getName();
			File ff = new File(name);
			if(!ff.exists()){
				name = "settings/"+name;
			}
			try{
				FileInputStream fis = new FileInputStream(name);

		        byte[] buffer = new byte[4092];
		        int byteCount = 0;
		        while ((byteCount = fis.read(buffer)) != -1)
		        {
		            zos.write(buffer, 0, byteCount);
		           
		        }

		        fis.close();
		        zos.closeEntry();	
			}catch (Exception k){
				k.printStackTrace();
			}
			
		}

		
		
    		
	    }

	    zos.finish();
	    zos.close();
	} catch (IOException e) {
	    parent.showError(e);
	}
    }

    public void write(){
      writeZip(parent.getSelectedAgentCode() + ".zip");
    }
 
    public void writeToPath(String path){
        writeZip(path);
      }

    public void write(String agent){
      writeZip(agent + ".zip");
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

