
package eevaeksport.imp;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ee.mobilejava.earve.GenericDOM;
import ee.mobilejava.earve.OrderLine;
import ee.mobilejava.earve.driver.read.XMLDok;
import eevaeksport.Agent;
import eevaeksport.ConvertData;
import eevaeksport.EksportFrame;
import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.imp.driver.AbstractImportDriver;
import eevaeksport.imp.driver.KalevImportDriver;
import eevaeksport.imp.driver.MovexLatImportDriver;
import eevaeksport.imp.driver.SkaiteEugImportDriver;
import eevaeksport.imp.driver.VichiLITSQLImportDriver;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Cany:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ImportPanel extends JPanel implements ActionListener{

	
	 /*
	   * Tellimuste staatused 
	   * 1 - tagastus ftelema
	   * 2 - ymberhindlus
	   * 60 - XML Telema uus
	   * 59 - Kodupaber WEB tellimus  
	   * 80 - XML order Format EDISOFT 
	   *  79 - XML order Format EDISOFT 
	   * 81 - XLS Eiginita 
	   * 82 - multimaxima 
	   * 83 - assorti
	   * 84 - rtffyxls
	   * 77 - kalev xls
	   * 78 - kalev eanpda
	   * 80 - PDF Format NPFOODS lithuania
	   * 80 - PDF Format COLGATE poland
	   * 80 - PDF Format NESTLE lithuania
	   * 80 - TXT Format ALITA lithuania
	   * 85 - PDF Format HENKEL lithuania
	   * 95 - PRI Prisma Telema txt order
	   * 96 - XmlOrder telema vana formaat
	   * 98 - maxima SAP TXT tellimus
	   * 94 - maxima XLS tellimus
	   * 97 - LUKOIL XLS tellimus
	   * 70 - Logia Edi 
	   * 99 - loadHTMOrder 
	   * 99 - csv 
	   * 88 - CSV KPM
	   * 89 - Rimi tellimus pdf
	   * 90 - Drogas tellimus xml
	   * 93 - IKI tellimus txt
	   * 91 -  EIP rivile tellimus
	   * 92 - rivona PDF tellimus leedu
	   * 99- dominante arve 
	   *  99- via arve 
	   *  99- levis report
	   * 99- drogas 
	   * 99 - WEB Order
	   * 99 - XLS katpedele
	   * 99 - XLS prike
	   * */ 
	
  
	  public String make(String aa){
	    	if(aa != null && aa.trim().length()>0){
	    		aa = aa.replaceAll(";", " ").trim();
	    	}else{
	    		aa = " ";
	    	}
	    	return aa;
	    }
	
	private static final long serialVersionUID = 1L;
public ImportPanel(EksportFrame parent) throws ClassNotFoundException{
    this.parent = parent;
    importLogPanel = new ImportLogPanel(this);
    initDriver();

    
    PropertyConfigurator.configure("log4j.properties");
    importButton = new JButton(parent.getString("add"));
    this.setLayout(new BorderLayout());
    JPanel fileChoosePanel = new JPanel();
    fileChoosePanel.add(importFileField);
    JPanel importButtonPanel = new JPanel();
    importButtonPanel.add(importButton);
    importButton.addActionListener(this);
    add(importButtonPanel, BorderLayout.SOUTH);
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(fileChoosePanel, BorderLayout.NORTH);
    centerPanel.add(importLogPanel, BorderLayout.SOUTH);
    add(centerPanel, BorderLayout.CENTER);
    if (parent.isAutoImport()){
      System.out.println("isAutoimport");
  	   
      boolean teeme = EksportFrame.checkRunningCopy();
		if (teeme) {//
			 importOrders();
		}else{
			System.out.println("AutoImport - Quiting - no import done - previous session is open");
		}  
      System.exit(0);
    }
    
    if (parent.isAutoImportReq()){
    	boolean teeme = EksportFrame.checkRunningCopy();
    	if (teeme) {
    	
    		File f = new File ("impdata.txt");
        	if(f.exists()){
        		   f.delete();
        		   System.out.println("isAutoimportRequest");
        		      importOrders();
        		      System.exit(0);	
             
        	}
        	
    		}else{
    			System.out.println("ImportRequest - Quiting - no import done - previous session is open");
    			System.exit(0);
    		}
    	
    	
    	System.exit(0);
      }
    timer.start();
  }



  public void actionPerformed(ActionEvent e){
      Object source = e.getSource();
  

      if (source == importButton){
      doImport();    
      }

      if (source == timer){
        refreshFileList();
      }

  }

  public void doImport(){
    importButton.setEnabled(false);
    importOrders();
    parent.pack();
    importButton.setEnabled(true);
  }
public String currentFile = null;
public File currFile = null;


public String parseFirstEmail(String emails) {
    String		email = "";
    StringTokenizer st =
        new StringTokenizer(emails, String.valueOf(";"));

    if (st.hasMoreTokens()) {
        email = st.nextToken();
    }

    return email;
}


  private void importOrders(){
    File dir = new File(driver.getImportFile());
    
  //  System.out.println("vvv "+ driver.getImportFile());
    File[] files = dir.listFiles();
   
    
    for (int i = 0; i < files.length ; i++){
      File file = files[i];
      StringBuffer troubles = new StringBuffer();

      if (file.isFile() && !file.getName().contains("DS_Store")){
        String fileName = file.getAbsolutePath();
        currentFile = fileName;
        currFile = file;
        String agent = getAgentFromFileName(fileName);
         doknr = "";
        
        Agent agentOB = (Agent)parent.getEksportPanel().getAgentsHash().get(agent);
        if (agentOB == null){
        	agentOB = (Agent)parent.getEksportPanel().getAgentsHash().get(agent.toLowerCase());
        }
        if (agentOB == null){
        	agentOB = (Agent)parent.getEksportPanel().getAgentsHash().get(agent.toUpperCase());
        }
        //System.out.println("agndi kood failist " + agent + " " + agentOB.getID());
        
        if (agentOB == null){
          String agentID = parent.getProps().getProperty("defaultagent");
          if (agentID != null){
            agentOB = new Agent(agentID);
          } else {
            showError(new Exception(parent.getString("filename")));
            importButton.setEnabled(true);
          }
        }

        boolean isPilot = agentOB.isIspilot();
        System.out.println("onPillot "+isPilot  + "  Nimi  " + agentOB.getID()+ file.getName());
        Orders orders = new Orders(agentOB.getID(), isPilot);
     
        
        if(fileName.toLowerCase().endsWith(".zip")){
           file = unZip(file);	
           fileName = file.getAbsolutePath();
        }
        

        if(fileName.toLowerCase().endsWith(".pgp")){
       file = unPgp(file);	
       log.info("PGP unpacked filename:" + file.getAbsolutePath());
       fileName = file.getAbsolutePath();
        }
        
       // System.out.println(file.getName().toLowerCase().endsWith(".WEB") + " " + file.getName().startsWith("IN.") + " " + file.getName().endsWith(".TXT"));
        try {
        	//Suuniviisiline ja vabatahtlik
        	if(parent.getProps().getProperty("convertversion")!= null){
        	String format = parent.getProps().getProperty("convertversion");
        	
        	if(format.equals("drogas")){
        		 System.out.println("xls tellimus DrogasLIT formaat");
                 loadDrogasOrder(orders, file,troubles);
                System.out.println(file.getAbsolutePath());
                // file.delete();
        	}

         	if(format.equals("idaxls")){
       		 System.out.println("xls tellimus IdaAri formaat");
       		 loadIdaXlsArve(orders, file,troubles);
            System.out.println(file.getAbsolutePath());
        }

        	if(format.equals("levis")){
        		 System.out.println("xls aruanne LEViS formaat");
                 loadLevisReport(orders, file,troubles);
                System.out.println(file.getAbsolutePath());
                // file.delete();
        	}
        	if(format.equals("via")){
       		 System.out.println("via arve tagasilugemine formaat");
                loadViaBills(orders, file,troubles);
               System.out.println(file.getAbsolutePath());
               // file.delete();
       	    }
        	if(format.equals("csv")){
        		loadCsvOmaBills(orders, file,troubles);
                System.out.println("CSV Arve tagasilugemine");
            }
        	
        	
        	if(format.equals("xml")){
          		 System.out.println("xml vana arve tagasilugemine formaat");
          		loadXMLArve(orders, file,troubles);
                  System.out.println(file.getAbsolutePath());
                  // file.delete();
          	    }
           	
        	if(format.equals("xmluus")){
         		 System.out.println("xml UUS arve tagasilugemine formaat");
         		loadXMLUusArve(orders, file,troubles);
                 System.out.println(file.getAbsolutePath());
                }

        	if(format.equals("xmluusvia")){
         		 System.out.println("xml UUS VIA arve tagasilugemine formaat");
         		loadXMLViaUusArve(orders, file,troubles);
                 System.out.println(file.getAbsolutePath());
                }
        	
        	if(format.equals("xmlost")){
        		 System.out.println("xml ERply arve tagasilugemine formaat");
        		 loadXMLerplArve(orders, file,troubles);
                System.out.println(file.getAbsolutePath());
                }
        	
        	if(format.equals("dom")){
          		 System.out.println("dominante arve tagasilugemine formaat");
                   loadDomBill(orders, file,troubles);
                  System.out.println(file.getAbsolutePath());
                }

        	if(format.equals("nord")){
          		 System.out.println("nordneti arve tagasilugemine formaat");
                   loadNordBill(orders, file,troubles);
                  System.out.println(file.getAbsolutePath());
                }
        	
        	if(format.equals("logia")){
          		 System.out.println("Logia arve tagasilugemine formaat");
          		loadCSVBill(orders, file,troubles);
                  System.out.println(file.getAbsolutePath());
                }
        	
        	
        	if(format.equals("logius")){
         		 System.out.println("Logius arve tagasilugemine formaat");
         		loadLogiusBill(orders, file,troubles);
                 System.out.println(file.getAbsolutePath());
                 // file.delete();
         	    }
        	
        	
        	if(format.equals("selver")){
                this.multimode = true;
                
            	this.loadXLSSelverFileOrder(orders, files,troubles);
                System.out.println("XLS tellimus MULTIORDER3 MultiFile");
            }
          	if(format.equals("prikeprint")){
        		 System.out.println("Prike Lehe trykkimine formaat");
        			 loadPrikePrint(orders, file,troubles);
        		
        		
                 System.out.println(file.getAbsolutePath());
                // file.delete();
        	    }
 
       
        		
        	}else if(parent.getProps().getProperty("purchage")!= null){
            	String format = parent.getProps().getProperty("purchage");
            	
            	if(format.equals("eugesta") || format.equals("true")){
            		String type =  identifyPurFile(file);
            		if(type.equals("PDF")){
            	     loadPDFPurOrder(orders, file,troubles);
               		 System.out.println("pdf ostutellimus ");
            		}
                    
            		if(type.equals("TXT")){
               	     loadTXTPurOrder(orders, file,troubles);
                  		 System.out.println("txt ostutellimus ");
               		}
            		
            		if(type.equals("CSV")){
            		 

                		 System.out.println("csv ostuarve ");
            			loadCSVBills(orders, file,troubles);
                         System.out.println(file.getAbsolutePath());
                     
                  		}
            		
            		if(type.equals("XML")){
                  	     loadXMLPurOrder(orders, file,troubles);
                     		 System.out.println("XML Telema ostutellimus ");
                  		}
            		if(type.equals("ediinv")){
                 	     loadXMLEdiPurOrder(orders, file,troubles);
                    		 System.out.println("XML Edisoftostuarve ");
                 		}
                    System.out.println(type + " " + file.getAbsolutePath());
                  
            	}

            
                if (file.exists()){
                  	if("true".equals(parent.getProps().getProperty("savetohis"))){

              			System.out.println("Siin oleme1");
                  		if (parent.getProps().getProperty("imp.read")== null ){

                  			System.out.println("Siin oleme2");
                  			file.renameTo(new File("C:/Mobile Tellimused/history" + File.separator  + file.getName()));                		
                  		}else{
                  			System.out.println("Siin oleme");
                  			if (parent.getProps().getProperty("imp.read2")== null ){
                  				File fff = new File(parent.getProps().getProperty("imp.read") + "done");
                  				if(!fff.exists()){
                  					fff.mkdir();
                  				}
                  				
                  				File ff = new File(parent.getProps().getProperty("imp.read") + "done" +File.separator  + file.getName());
                      			if(ff.exists()){
                      				ff.delete();
                      			}
                      			file.renameTo(new File(parent.getProps().getProperty("imp.read") + "done" +File.separator  + file.getName()));	
                  			}else{
                  				File fff = new File(parent.getProps().getProperty("imp.read2") + "done");
                  				if(!fff.exists()){
                  					fff.mkdir();
                  				}
                  				
                  				File ff = new File(parent.getProps().getProperty("imp.read2") + "done" +File.separator  + file.getName());
                      			if(ff.exists()){
                      				ff.delete();
                      			}
                      			file.renameTo(new File(parent.getProps().getProperty("imp.read2") + "done" +File.separator  + file.getName()));
                  			}
                  		
                  		}
                  		}else{

                        file.delete();	
                  	}
                  }
            		
            	}else{
        		
            		boolean sap = true;
            		if(parent.getProps().getProperty("sap") != null){
            			sap = false;
            		}
            		System.out.println("NAME   " + fileName + " " + sap + " " + file.getName().length());
          if(fileName.toLowerCase().endsWith(".txt") &&( file.getName().startsWith("tellimus") || file.getName().startsWith("t-"))&& !file.getName().startsWith("tellimus_")){
          	 System.out.println("txt tellimus TelemaTXT formaat");
             loadTelTXTOrder(orders, file,troubles);
          }else if(fileName.toLowerCase().endsWith(".txt") && file.getName().startsWith("aruanne")){
         	 System.out.println("txt tellimus Rautakesko TXT formaat");
             loadRautaKeskoTXTOrder(orders, file,troubles);
          }else if (sap && file.getName().toLowerCase().endsWith(".txt") && 
        		  ((file.getName().startsWith("S") && file.getName().length() == 14 ) || (file.getName().startsWith("45") && file.getName().indexOf("45.") == -1) ||
        		  (file.getName().startsWith("S00")) || (file.getName().startsWith("S01")) || (file.getName().startsWith("S02")) ||
        		  (file.getName().startsWith("47") && (file.getName().indexOf("47.") == -1 || file.getName().indexOf("47.") > 2  )) ||
        		  (file.getName().startsWith("Z") && !file.getName().startsWith("ZEM") && !file.getName().startsWith("ZI") && !file.getName().startsWith("Z-2") &&  !file.getName().startsWith("ZO."))
          		|| ((file.getName().indexOf("L00")>-1 || file.getName().indexOf("L01")>-1 ) && file.getName().indexOf("VIL0") == -1
          				&& file.getName().indexOf("KL0") == -1 && file.getName().indexOf("TEL00") == -1) || file.getName().indexOf("PO_4")>-1) ||
        		  file.getName().startsWith("IP") ){
                System.out.println("txt tellimus SAPOUTPUT formaat");
                convCodes = getConversionCodes();
                
                
                loadSAPOrder(orders, file,troubles);
           } else if (file.getName().toLowerCase().endsWith(".rtf")){
               System.out.println("RTF tellimus DEPO formaat");
              
               this.loadRTFOrder(orders, file,troubles);
       }else if (file.getName().toLowerCase().endsWith(".WEB") || (file.getName().startsWith("IN.") && file.getName().endsWith(".TXT"))){
                    System.out.println("TXT tellimus WEB formaat");
                    loadWEBOrder(orders, file,troubles);
            } else if (fileName.toLowerCase().endsWith(".txt") || fileName.toLowerCase().endsWith(".zip")
            		|| fileName.toLowerCase().endsWith(".edi") || fileName.toLowerCase().indexOf(".orders") != -1
            		 || fileName.toLowerCase().endsWith(".eip")){
            System.out.println("txt v�i edi tellimus formaat");
            if(fileName.toLowerCase().endsWith(".zip")){
            	loadABCOrder(orders, file,troubles);
            }else{
            	String format = identifyTXT(file);
                if(format.equals("ABC")){
                	System.out.println("txt tellimus  ABC formaat");
                    loadABCOrder(orders, file,troubles);
                }
                
                if(format.equals("SAP")){
                	System.out.println("txt tellimus  ABC formaat");
                	loadSAPOrder(orders, file,troubles);
                }
                
                if(format.equals("IKI")){
                	System.out.println("txt tellimus  IKI formaat");
                	 convCodes = getConversionCodes();
                    loadIKIOrder(orders, file,troubles);
                }
                
                if(format.equals("PRI")){
                	System.out.println("txt tellimus  PRI formaat");
                	 convCodes = getConversionCodes();
                    loadPRIOrder(orders, file,troubles);
                }
                
                if(format.equals("EDI")){
                	System.out.println("EDI tellimus  LogiaPluss formaat");
                	 convCodes = getConversionCodes();
                     loadLogiaEdiOrder(orders, file,troubles);
                }
                
                if(format.equals("EIP")){
                	System.out.println("EIP tellimus Rivile formaat");
                	 convCodes = getConversionCodes();
                     loadRivileEIPOrder(orders, file,troubles);
                }
            }
            
    
           } else if (fileName.toLowerCase().endsWith(".csv")){
             
              convCodes = getConversionCodes();
              String csv = identifyCSV(file);
              if(csv.equals("KPM")){
            	  System.out.println("CSV tellimus KPM formaat");
            	  loadCSVKMPOrder(orders, file,troubles);  
              }
              if(csv.equals("CSV")){
            	  System.out.println("CSV tellimus CSV formaat");
            	  loadCSVOrder(orders, file,troubles);  
              }
              if(csv.equals("CSVENG")){
            	  System.out.println("CSVENG tellimus CSVENG formaat");
            	  
            	  loadCSVOrder(orders, file,troubles);  
              }
              if(csv.equals("KAPP")){
            	  System.out.println("CSV tellimus KAPP formaat");
            	  loadCSVKappOrder(orders, file,troubles);  
              }
          } else if (fileName.toLowerCase().endsWith(".xml") && (fileName.indexOf("ORDER_") != -1 ||
        		  fileName.indexOf("Order_") != -1 || fileName.indexOf("Tellimus_") != -1  
        		  || fileName.indexOf("development") != -1)){
             System.out.println("XML tellimus EDISOFT formaat "  + fileName );
             
             convCodes = getConversionCodes();
            loadXMLEDISOFTOrder(orders, file,troubles);
         
           } else if (fileName.endsWith("Order.xml")){
              System.out.println("XML tellimus DROGAS formaat");
              convCodes = getConversionCodes();
             loadXMLDrogasOrder(orders, file,troubles);
           }else if (fileName.toLowerCase().endsWith(".htm")){
            
            convCodes = getConversionCodes();
            String format = identifyHTM(file);
            if(format.equals("telema")){
            	 System.out.println("HTM tellimus Telema formaat");
                 this.loadHTMOrder(orders, file,troubles);
            }
            
            if(format.equals("RXL")){
           	 System.out.println("HTM tellimus RXlogist formaat");
                this.loadHTMRXOrder(orders, file,troubles);
           }
           
          }else if (file.getName().toLowerCase().indexOf("order") != -1 && fileName.toLowerCase().endsWith(".xls") 
        		  && file.getName().toLowerCase().indexOf("forma") == -1 && file.getName().toLowerCase().indexOf("_order") == -1){
              System.out.println("XLS tellimus Maxima Leedu formaat");
             // System.out.println("asi " + fileName.toLowerCase().indexOf("order") + " " + fileName.toLowerCase());
              convCodes = getConversionCodes();
             this.loadXLSMaxOrder(orders, file,troubles);
          }else if (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx")
        		  || fileName.toLowerCase().endsWith(".xlsb")){
                System.out.println("XLS tellimus formaat");
                convCodes = getConversionCodes();
                String format = identifyXLS(file);
                System.out.println("XLS tellimus formaat "  + format);
               try{
            	   if(format.equals("eiginta")){
                       this.loadXLSEigintaOrder(orders, file,troubles);
                       System.out.println("XLS tellimus eiginta");
            	   }
            	   
            	   if(format.equals("rivonax")){
                       this.loadXLSRivonaXOrder(orders, file,troubles);
                       System.out.println("XLS tellimus rivona");
            	   }
            	   
            	   if(format.equals("statoillit")){
                       this.loadXLSStatoilLITOrder(orders, file,troubles);
                       System.out.println("XLS tellimus statoilLitEug");
            	   }
            	   
            	   
              
                       if(format.equals("assorti")){
                           this.loadXLSAssortiOrder(orders, file,troubles);
                           System.out.println("XLS tellimus assorti");
                       }
                       
                       if(format.equals("stockman")){
                           this.loadXLSStockmanOrder(orders, file,troubles);
                           System.out.println("XLS tellimus assorti");
                       }
                       
                       if(format.equals("lukoil")){
                    	    System.out.println("XLS tellimus LUKOIL");
                           this.loadXLSLukoilOrder(orders, file,troubles);
                       
                       }
                       
                       if(format.equals("altia")){
                   	    System.out.println("XLS tellimus Altia");
                          this.loadXLSAltiaOrder(orders, file,troubles);
                      
                      }
                       
                       if(format.equals("orkla")){
                      	    System.out.println("XLS tellimus rkls");
                             this.loadXLSOrklaOrder(orders, file,troubles);
                         
                         }
                       if(format.equals("kalev")){
                     	    System.out.println("XLS tellimus kalev");
                            this.loadXLSKalevOrder(orders, file,troubles);
                        
                        }
                       if(format.equals("kaleveanpda")){
                    	    System.out.println("XLS tellimus kalev");
                           this.loadXLSKalevPDAEANOrder(orders, file,troubles);
                       
                       }
                       if(format.equals("statoil")){
                           this.loadXLSStatoilOrder(orders, file,troubles);
                           System.out.println("XLS tellimus statoil");
                       }
                       if(format.equals("MULTIORDER")){
                           this.multimode = true;
                       	this.loadXLSMULTIOrder(orders, file,troubles);
                           System.out.println("XLS tellimus MULTIORDER");
                       }
                       if(format.equals("MULTIORDER2")){
                           this.multimode = true;
                       	this.loadXLSMULTISHEETOrder(orders, file,troubles);
                           System.out.println("XLS tellimus MULTIORDER2 MultiSheet");
                       }
                       
                       if(format.equals("sanitex")){
                           this.loadXLSSanitexOrder(orders, file,troubles);
                           System.out.println("XLS tellimus sanitex");
                       }
                      
                       if(format.equals("sanitexuus")){
                           this.loadXLSXSanitexOrder(orders, file,troubles);
                           System.out.println("XLS tellimus sanitex");
                       }
                       
                       if(format.equals("maxima")){
                           this.loadXLSMaxOrder(orders, file,troubles);
                           System.out.println("XLS tellimus maxima");
                       }
                       if(format.equals("eststatoil")){
                       	  System.out.println("XLS tellimus eststatoil");
                           this.loadXLSPrikeOrder(orders, file,troubles);	
                       }
                       if(format.equals("katpedele")){
                       	System.out.println("asi katpedele");
                       	//System.exit(0);
                       	 this.loadXLSKatpedeleOrder(orders, file,troubles);	
                       }
                  
                       if(format.equals("maximalat")){
                          	System.out.println("asi maximalat");
                          	//System.exit(0);
                          	 this.loadXLSMaxLatOrder(orders, file,troubles);	
                          }
                       
                       
                       if(format.equals("prike")){
                         	System.out.println("asi prike");
                         	//System.exit(0);
                         	 this.loadXLSPrikeOmaOrder(orders, file,troubles);	
                         }
                     
               }catch(Exception e){
            	   e.printStackTrace();
            	   troubles.append("XLS File not readable in JAVA Sorry .....");
            	   throw new Exception("XLS File not readable", e);            	
               }
                   
          }else if (fileName.toLowerCase().endsWith(".xml") ){
           
            String format = identifyXML(file,troubles);
            System.out.println("XML tellimus "  +format + " formaat");
            convCodes = getConversionCodes();
            if(format.equals("telemauus")){
               loadXMLTelemaUusOrder(orders, file ,troubles);	
            }
            if(format.equals("laimaaxa")){
                loadXMLLaimaOrder(orders, file ,troubles);	
             }
            
            
            if(format.equals("telemauusdesorder")){
                loadXMLTelemaUusDesOrder(orders, file ,troubles);	
             }
            if(format.equals("telemavana")){
                loadXMLOrder(orders, file ,troubles);	
            }
            
            if(format.equals("edisoft")){
               loadXMLEDISOFTOrder(orders, file,troubles);
            }

            if(format.equals("gruste")){
            	loadXMLGrusteOrder(orders, file,troubles);
            }
            
            if(format.equals("edirec")){
            	  System.out.println("XML recadv EDISOFT formaat");
                  loadXMLEDISOFTRecadv(orders, file,troubles);                	
            }
            
            
         }else if (fileName.toLowerCase().endsWith(".pdf") ){
            System.out.println("PDF tellimus PDF formaat");
            convCodes = getConversionCodes();
            String format = identifyPDF(file);
            	  
            	   if(format.equals("telema")){
            		   loadPDFTelemaOrder(orders, file,troubles);
                    }
            	   
            	   if(format.equals("rimi")){
            		   loadPDFOrder(orders, file,troubles);
                    }
            	   
            	   if(format.equals("epost")){
            		   System.out.println("Epost PDF");
            		   loadPDFPostOrder(orders, file,troubles);
                    }
            	   
             	   if(format.equals("rautakesko")){
            		   System.out.println("Rautakesko PDF");
            		   loadPDFRautaOrder(orders, file,troubles);
                    }
            	   
            	   if(format.equals("stockmann")){
            		   loadPDFStockmannOrder(orders, file,troubles);
                    }
           
             System.out.println("PDF " + format);
        
         }
          
      	
        	}
        	
        	if(!multimode){
        		importOrder(orders,troubles);  
        		multimode = false; 
        	}
          

          System.out.println("DONE...." + file.exists() + " " + file.getAbsolutePath());
          if(troubles.length()>5){
        	  String nnn = file.getAbsolutePath();
        	  if(parent.getProps().getProperty("infopath") != null){
        		  nnn =  parent.getProps().getProperty("infopath") + file.getName()   ;
        	  }
        	  this.sendTroubleEmail(troubles , nnn);
        	  System.out.println("Sending Trobles " + file.exists() + " " + nnn);
          }
         
          
          if (file.exists()){

          	if("true".equals(parent.getProps().getProperty("savetohis"))){

          		if (parent.getProps().getProperty("imp.read")== null ){
                  
          			file.renameTo(new File("C:/Mobile Tellimused/history" + File.separator  + file.getName()));                		
          		 }else{
          			if (parent.getProps().getProperty("imp.read2")== null ){
          				System.out.println("Siin oleme 01 ");
          				File fff = new File(parent.getProps().getProperty("imp.read") + "done");
          				if(!fff.exists()){
          					fff.mkdir();
          				}
          				
              			File ff = new File(parent.getProps().getProperty("imp.read") + "done" + File.separator  + file.getName());
              			
              			if(ff.exists()){
              				ff.delete();
              			}	
          			}else{
          				System.out.println("Siin oleme 01 ");
          				File fff = new File(parent.getProps().getProperty("imp.read2") + "done");
          				if(!fff.exists()){
          					fff.mkdir();
          				}
          				
              			File ff = new File(parent.getProps().getProperty("imp.read2") + "done" + File.separator  + file.getName());
              			
              			if(ff.exists()){
              				ff.delete();
              			}	
          			}
          		   File ff1 = new File(parent.getProps().getProperty("imp.read") + "error" + File.separator  + file.getName());
          			if (parent.getProps().getProperty("imp.read2")!= null ){
          				ff1 = new File(parent.getProps().getProperty("imp.read2") + "error" + File.separator  + file.getName());
          			}
           
          			
          			if(ff1.exists()){
          				ff1.delete();
          			}
          			 if(troubles.length()>5 && troubles.indexOf("CREDITPROBLEM") != -1){
          				if (parent.getProps().getProperty("imp.read2")== null ){
          					file.renameTo(new File(parent.getProps().getProperty("imp.read") + "karantiin" + File.separator  + file.getName()));            					
          				}else{
          					file.renameTo(new File(parent.getProps().getProperty("imp.read2") + "karantiin" + File.separator  + file.getName()));              				
          				}
               			 
          			 }else if(troubles.length()>5 ){
          				if (parent.getProps().getProperty("imp.read2")== null ){
          					file.renameTo(new File(parent.getProps().getProperty("imp.read") + "error" + File.separator  + file.getName()));            					
          				}else{
          					file.renameTo(new File(parent.getProps().getProperty("imp.read2") + "error" + File.separator  + file.getName()));              				
          				}
               			 
          			 }{
               			if (parent.getProps().getProperty("imp.read2")== null ){
               				File fff = new File(parent.getProps().getProperty("imp.read") + "done");
              				if(!fff.exists()){
              					fff.mkdir();
              				}
              				
          					file.renameTo(new File(parent.getProps().getProperty("imp.read") + "done" + File.separator  + file.getName()));         					
          				}else{
          					File fff = new File(parent.getProps().getProperty("imp.read2") + "done");
              				if(!fff.exists()){
              					fff.mkdir();
              				}
              				
          					file.renameTo(new File(parent.getProps().getProperty("imp.read2") + "done" + File.separator  + file.getName()));            				
          				} 
          			 }

              		System.out.println("Rename fail to  " + file.getAbsolutePath());
          		  }
          		System.out.println("Deleting fail " + file.getAbsolutePath());
          		file.delete();
          		}else{
                file.delete();
                System.out.println("deleted successfully");
          	}
          }
          
        } catch (Exception e){
        	e.printStackTrace();
            if (file.exists()){

              	if("true".equals(parent.getProps().getProperty("savetohis"))){

              		if (parent.getProps().getProperty("imp.read")== null ){

              			file.renameTo(new File("C:/Mobile Tellimused/history" + File.separator  + file.getName()));                		
              		}else{
              			File ff = new File(parent.getProps().getProperty("imp.read") + "error" +File.separator  + file.getName());
              			if(ff.exists()){
              				ff.delete();
              			}
              			if(driver.getTrouble() != null){
              				file.renameTo(new File(parent.getProps().getProperty("imp.read") + "karantiin" +File.separator  + file.getName()));
                      				
              			}else{
              				file.renameTo(new File(parent.getProps().getProperty("imp.read") + "error" +File.separator  + file.getName()));
                      		
              			}
              			
              		}
              		}else{
                    file.delete();	
              	   }
              }
          	 
            
            this.sendExceptionEmail(e.toString(), file.getAbsolutePath());
            
            System.out.println("Sending EXCEPTIONS " + file.exists() + " " + file.getAbsolutePath());
            System.out.println("DONE...." + file.exists() + " " + file.getAbsolutePath());
            if(troubles.length()>5){ String nnn = file.getAbsolutePath();
      	  if(parent.getProps().getProperty("infopath") != null){
    		  nnn = parent.getProps().getProperty("infopath") + file.getName() ;
    	      }
    	     this.sendTroubleEmail(troubles , nnn);
          	System.out.println("Sending Troubles1 " + file.exists() + " " + nnn);
            }
            
            if (file.exists()){
            	System.out.println("Kustutamisele  -1");
              	if("true".equals(parent.getProps().getProperty("savetohis"))){
              		System.out.println("Kustutamisele  0");
              		if (parent.getProps().getProperty("imp.read")== null ){
              			System.out.println("Kustutamisele  1");
              			file.renameTo(new File("C:/Mobile Tellimused/history" + File.separator  + file.getName()));                		
              		 }else{
              			System.out.println("Siin oleme 01 ");
              			File ff = new File(parent.getProps().getProperty("imp.read") + "done" + File.separator  + file.getName());
              			
              			if(ff.exists()){
              				ff.delete();
              			}
              			
                        File ff1 = new File(parent.getProps().getProperty("imp.read") + "error" + File.separator  + file.getName());
              			
              			if(ff1.exists()){
              				ff1.delete();
              			}
              			 if(troubles.length()>5 && troubles.indexOf("CREDITPROBLEM") != -1){
                   			file.renameTo(new File(parent.getProps().getProperty("imp.read") + "karantiin" + File.separator  + file.getName()));           				 
              			 }else if(troubles.length()>5){
                   			file.renameTo(new File(parent.getProps().getProperty("imp.read") + "error" + File.separator  + file.getName()));
              			 }else{
              				File fff = new File(parent.getProps().getProperty("imp.read") + "done");
              				if(!fff.exists()){
              					fff.mkdir();
              				}
              				
                   			file.renameTo(new File(parent.getProps().getProperty("imp.read") + "done" + File.separator  + file.getName()));             				 
              			 }

                  		System.out.println("Rename fail to  " + file.getAbsolutePath());
              		  }
              		System.out.println("Deleting fail " + file.getAbsolutePath());
              		file.delete();
              		}else{
                    file.delete();
                    System.out.println("deleted successfully");
              	}
              }
            
          showError(e);
        }
      }
    }

  }

  private void loadXMLPurOrder(Orders orders, File f,StringBuffer troubles) {
		
	 
	    
	    String date = null;
	    String paydate = null;
	    String cred = "";
	    String comment = "";
	    double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	    String valuta = "EUR";
	  
	    company = " PRIKE ";
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "E-Document");		  	    
      Node root2 = getNode(root, "Document");
    
  
	//    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	 //   String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
      
      
	    String dokNr  = getNode(getNode(root2, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();		    
	    kogusumma = Double.parseDouble(getNode(getNode(root2, "DocumentSumGroup"), "DocumentSum").getFirstChild().getNodeValue());
	    valuta = getNode(getNode(root2, "DocumentSumGroup"), "Currency").getFirstChild().getNodeValue();
	    
	

	    try{
		    date = getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"InvoiceDate").getFirstChild().getNodeValue();	
		       if(!date.equals("")){
		      		date = getCalLit(date).getTime() + "";
		        }
		    long  pay = getCalLit(getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"DueDate").getFirstChild().getNodeValue()).getTime();	
		
		       paydays = this.getPaydays(Long.parseLong(date), pay);
	   }catch(Exception ee ){			   
	   }
	   	
	
	 
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	firma =  getNode(getNode(getNode(root2, "DocumentParties"), "BuyerParty"),"PartyCode").getFirstChild().getNodeValue();
	    	firma =  driver.identifyCustomer(firma,"prike");
	    }catch (Exception e){    
	    }
	 //   firma = "prike";
	         	
			  if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + dokNr + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED PRIKE XML format \n" + "Dok Nr: " + doknr + " " + dokNr+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }		    	
	    	
			  Node read = getNode(root2, "DocumentItem");
		    	
			    
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	      
	        	  System.out.println("ref "+ dokNr +  " " + firma +
	        			  " " + date  + " " + kogusumma);		   		
	              
	           	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName ="";
	          	
	          	 
	          	prodName = getNode(rida, "ItemDescription").getFirstChild().getNodeValue();
	          	
				    double  kogus = Double.parseDouble(getNode(rida, "AmountInvoiced").getFirstChild().getNodeValue()) ;
	         
				 try{
	          	    ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();
	          	}catch(Exception ss){
	          	ss.printStackTrace()	;
	          	}
	         
	          	 
	            try{
	            	
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED PRIKE XML format : \n" + "Dok Nr: " + doknr + " " + dokNr+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tInvoiceNr:" + dokNr +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }
	                  String batch = "";
	   	           try{
	   	        	batch =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
	   	        	batch = batch + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
	   		       			"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	   	           }catch(Exception e){
	   	        	   
	   	           }
	 

	               double price = Double.parseDouble(getNode(rida, "ItemPrice").getFirstChild().getNodeValue());
	             
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	        
	               try{
		   	        	unit  =  getNode(getNode(getNode(rida, "ItemReserve"), "ItemReserveUnit"),"ItemUnit").getFirstChild().getNodeValue();
		   	        	
		   	           }catch(Exception e){
		   	        	   
		   	           }
	               
	               System.out.println("UNIT " + unit );
	               if (unit.startsWith("KG")){
	             	  unit = "KG";
	               }else{
	             	  unit = "vnt."; 
	               }
	               
	               if(getNode(rida, "excise") != null){
		           //    batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
	               }
	               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch );
	               
	             	
	            	   orders.add(firma, prod, prodName ,kogus, dokNr, valuta, km, 0,date,"0",price,0,false,f.lastModified()+ "",
	            			   80,unit,"XML format Prike estonia","","",f.getName(),true,null,null);
	  	             orders.setInfo(dokNr);  	            
	  	             orders.setKm(km);
	  	             counter++;
	               
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }

  private void loadXMLEdiPurOrder(Orders orders, File f,StringBuffer troubles) {
		
		 
	    
	    String date = null;
	    double kogusumma = 0;
	    int paydays = 0;
	    String valuta = "EUR";
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "Document-Invoice");		  	    
    Node root2 = getNode(root, "Invoice-Header");
  

	//    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	 //   String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
    
    
	    String dokNr  = getNode(root2 ,"InvoiceNumber").getFirstChild().getNodeValue();		    
	    kogusumma = Double.parseDouble(getNode(getNode(root, "Invoice-Summary"), "TotalGrossAmount").getFirstChild().getNodeValue());
	    valuta = "EUR";
	    
	    String ladu = "";

	    
	    try{
		    ladu = Converter.cleanLitLatEst(getNode(getNode(root2, "Delivery"), "CodeByBuyer").getFirstChild().getNodeValue());	
		   
	   }catch(Exception ee ){			   
	   }
	    System.out.println("ladu " + ladu);
	    
	    try{
		    date = getNode(root2, "InvoiceDate").getFirstChild().getNodeValue();	
		       if(!date.equals("")){
		      		date = getCalLit(date).getTime() + "";
		        }
		       
		    long  pay = getCalLit(getNode(root2, "InvoicePaymentDueDate").getFirstChild().getNodeValue()).getTime();	
		
		       paydays = this.getPaydays(Long.parseLong(date), pay);
	   }catch(Exception ee ){			   
	   }
	   	
	
	 
	    String fileName = f.getName();
	   
	    String firma = null;   

	    String iln = null;   
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	//v�tame regnumbri ja ILN ja otsime sellega 
	    	iln =  getNode(getNode(getNode(root, "Invoice-Parties"), "Seller" ), "ILN" ).getFirstChild().getNodeValue();
	    	regnr =  getNode(getNode(getNode(root, "Invoice-Parties"), "Seller" ), "UtilizationRegisterNumber" ).getFirstChild().getNodeValue();
	    	firma =  driver.identifyCustomer(iln,regnr);
	    }catch (Exception e){    
	    }
	 //   firma = "prike";
	         	
			  if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + dokNr + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED EDISOFT INVOICE XML format \n" + "Dok Nr: " + doknr + " " + dokNr+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }	
			  
			  System.out.println("ref "+ dokNr +  " " + firma +
        			  " " + date  + " " + kogusumma + " " + paydays + " " + iln);	
	   	
			  Node read = getNode(root, "Invoice-Lines");
			  
			    
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida0 = read.getChildNodes().item(i);
	          if (rida0.getNodeName().equalsIgnoreCase("Line")){
	      
	        	  System.out.println("ref "+ dokNr +  " " + firma + " " + iln +
	        			  " " + date  + " " + kogusumma);		   		
	        	  Node rida = getNode(rida0, "Line-Item");
	           	toode = getNode(rida, "SupplierItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName = getNode(rida, "ItemDescription").getFirstChild().getNodeValue();
	          	
				    double  kogus = Double.parseDouble(getNode(rida, "InvoiceQuantity").getFirstChild().getNodeValue()) ;
	         
				 try{
	          	    ean = getNode(rida, "EAN").getFirstChild().getNodeValue();
	          	}catch(Exception ss){
	          	ss.printStackTrace()	;
	          	}
	         
	          	 
	            try{
	            	
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED EDISOFT INVOICE XML format : \n" + "Dok Nr: " + doknr + " " + dokNr+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tInvoiceNr:" + dokNr + " Ean: " + ean + " Code:" + toode + " " + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }
	                  String batch = "";
	   	           try{
	   	        	//batch =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
	   	        	//batch = batch + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
	   		       		//	"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	   	           }catch(Exception e){
	   	        	   
	   	           }
	 

	               double price = Double.parseDouble(getNode(rida, "InvoiceUnitNetPrice").getFirstChild().getNodeValue());
	             
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	        
	               try{
		   	        	unit  =  getNode(rida, "UnitOfMeasure").getFirstChild().getNodeValue();
		   	        	
		   	           }catch(Exception e){
		   	        	   
		   	           }
	               
	               System.out.println("UNIT " + unit );
	               if (unit.startsWith("KG")){
	             	  unit = "KG";
	               }else{
	             	  unit = "vnt."; 
	               }
	               
	             //  if(getNode(rida, "excise") != null){
		           //    batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
	              // }
	               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch );
	              	
	            	   orders.add(firma, prod, prodName ,kogus, dokNr, valuta, km, 0,date,"0",price,0,false,f.lastModified()+ "",
	            			   66,unit,"XML Edisoft INVOICE format","","",f.getName(),true,null,null);
	  	             orders.setInfo(dokNr);  	            
	  	             orders.setKm(km);
	  	             orders.setShipMode(ladu);
	  	             counter++;
	               
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }

  
  private String identifyPDF(File f){
	  	 
	  	 File fil = new File ("C:/x/pdftemp.txt");
	  try{
		  
	 
	     PDDocument document = null;
	     try
	     {
	         document = PDDocument.load( f.getAbsolutePath() );
	         if( document.isEncrypted() )
	         {
	             try
	             {
	                 document.decrypt( "" );
	             }
	             catch( Exception e )
	             {
	            	
	                 System.err.println( "Error: Document is encrypted with a password." );
	                 System.exit( 1 );
	             }
	         }
	         
	         PDFTextStripper ss = new PDFTextStripper();
	        
	  
	         // System.out.println(aa);
	             if(fil.exists()){
		             	fil.delete();
		             }else{
		            	 File dir = new File("C:/x/");
		            	 if(!dir.exists()){
		            		 dir.mkdir();
		            	 }
		            	 }
	             String aa =  ss.getText(document);
	             
	       	  
	             FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
	            fos.write(aa.getBytes());
	            fos.close();
	          

	        
	     } finally{
	         if( document != null ) {
	             document.close();
	         }
	     }
	     
	    

	     FileReader frch = new FileReader(fil);
	     BufferedReader brch = new BufferedReader(frch);
	     
        String check = null;
	    
	     while ((check = brch.readLine()) != null){
	    	 
	    	 System.out.println("Check line " + check);
	    	 
	    	 if(check.toLowerCase().indexOf("tellimus�nr:") != -1 || 
	    			 (check.toLowerCase().indexOf("tellimus") != -1 && check.toLowerCase().indexOf("nr") != -1) ){
	    		return "telema";
	    	 } 
	    	 
	    	 if(check.toLowerCase().indexOf("order nr:") != -1 || check.toLowerCase().indexOf("order no:") != -1 ||
	    			( check.indexOf("Order") != -1 && check.indexOf("no:") != -1)  ){
	 	    		return "telema";
		    	 } 
	

	    	 if(check.toLowerCase().indexOf("rautakesko") != -1){
	 	    		return "rautakesko";
		    	 } 
	    	 if(check.toLowerCase().indexOf("aubaladu") != -1){
		    		return "epost";
		    	 } 
	    	 if(check.toLowerCase().indexOf("stockmann") != -1){
		    		return "stockmann";
		    	 } 
	    	
	     }
 }catch(Exception e){
	e.printStackTrace();	  
	  }
	    return "rimi";	 
  }
  
  private void refreshFileList(){
    importLogPanel.renewFileArea();
    File dir = new File(driver.getImportFile());
    if (dir.isDirectory()){
      File[] files = dir.listFiles();
      importFileList = new Vector();
      for (int i = 0; i < files.length ; i++){
        if (files[i].isFile()){
          String name = files[i].getName();
          importLogPanel.addFile(name);
          importFileList.addElement(name);
        }
      }
    }
  }

  private void initDriver() throws ClassNotFoundException{
   
    driver = parent.getImportDriver(this);
    importFileField.setText(driver.getImportFile());
  }

  public Properties getProps(){
    return parent.getProps();
  }

  private void setEmailBody(Orders orders, String fileName) throws IOException{
    File f = new File(fileName);
    File bodyFile = new File("C:/Mobile Tellimused" + File.separator + DIR_BODY + File.separator + f.getName());
    System.out.println(fileName);
    if (bodyFile.exists()){
        System.out.println("File OK bodyFile: " + bodyFile.getAbsolutePath());
      StringBuffer sb = new StringBuffer();
      FileReader fr = new FileReader(bodyFile);
      int i;
      while ((i = fr.read()) != -1){
        sb.append((char)i);
      }
      fr.close();
      orders.setEmailBody(sb.toString());
    }
  }
  
  private void loadXMLUusArve(Orders orders, File f,StringBuffer troubles) {
	
		  
		    
		    String date = null;
		    String paydate = null;
		    String cred = "";
		    String comment = "";
		    double kogusumma = 0;
		    double kmsumma = 0;
		    double ymmardus = 0;
		    int paydays = 0;
		  
		  
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
			
			System.out.println(f.getAbsolutePath());
			
			FileInputStream in = getCleanInputStream(f);
			String handler = null;

		    Document doc = db.parse(in,handler);
		    
		    Node root = getNode(doc, "E-Document");		  	    
	        Node root2 = getNode(root, "Document");
	      
	    
		//    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
		 //   String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
		   
	        
		    String dokNr  = getNode(getNode(root2, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();		    
		    kogusumma = Double.parseDouble(getNode(getNode(root2, "DocumentSumGroup"), "DocumentSum").getFirstChild().getNodeValue());
		    
		    String reference =  getNode(getNode(getNode(getNode(root2, "DocumentInfo"), "RefInfo"),"SourceDocument")
		    		,"SourceDocumentNum").getFirstChild().getNodeValue();
	
		    try{
			    date = getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"InvoiceDate").getFirstChild().getNodeValue();	
			       if(!date.equals("")){
			      		date = getCalLit(date).getTime() + "";
			        }
			    long  pay = getCalLit(getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"DueDate").getFirstChild().getNodeValue()).getTime();	
			
			       paydays = this.getPaydays(Long.parseLong(date), pay);
		   }catch(Exception ee ){			   
		   }
		   	
		
		 
		    String fileName = f.getName();
		   
		    String firma = null;   
		     
		    String prod = null;
		    String ean = null;
		    String toode = null;
		    String regnr = null;
		    int counter = 0;
		    try{
		    	firma =  getNode(getNode(getNode(root2, "DocumentParties"), "BuyerParty"),"PartyCode").getFirstChild().getNodeValue();
		    	firma =  driver.identifyCustomer(firma,regnr);
		    }catch (Exception e){    
		    }
		         	
				  if (firma == null || firma.trim().length() == 0){
		              addLog(parent.getString("nocompid") + " " + reference + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
		              troubles.append("Customer CANNOT BE RESOLVED DESPACH XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
		       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
		            }		    	
		    	
				  Node read = getNode(root2, "DocumentItem");
			    	
				    
		        for (int i = 0; i < read.getChildNodes().getLength(); i++){
		          Node rida = read.getChildNodes().item(i);
		          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
		      
		        	  System.out.println("ref "+ dokNr +  " " + firma +
		        			  " " + date + " " + reference + " " + kogusumma);		   		
		              
		           	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
		           
		          	String prodName ="";
		          	
		          	 
		         
		          	
					    double  kogus = Double.parseDouble(getNode(rida, "AmountInvoiced").getFirstChild().getNodeValue()) ;
		         
					 try{
		          	    ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();
		          	}catch(Exception ss){
		          	ss.printStackTrace()	;
		          	}
		         
		          	 
		            try{
		            	
		            prod = driver.identifyProduct(ean,toode);
		                  } catch (SQLException e) {}
		                  if (prod == null){
		                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
		                   // break;
		                	prod = " ";
		                	if(prod.trim().length() == 0 ){
		                		troubles.append("PRODUCT CANNOT BE RESOLVED DOMINANTE XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
		                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
		                   }
		                  }
		                  String batch = "";
		   	           try{
		   	        	batch =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
		   	        	batch = batch + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
		   		       			"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
		   	           }catch(Exception e){
		   	        	   
		   	           }
		   	           
		                  

		               double price = Double.parseDouble(getNode(rida, "ItemPrice").getFirstChild().getNodeValue());
		             
		               double km = 0;
		               double disc = 0;
		               String unit = "";
		        
		               if(getNode(rida, "excise") != null){
			           //    batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
		               }
		               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch );
		               if(!firma.equals("")){
		             	
		               orders.setInfo(reference);   
		               orders.setSumma(kogusumma);
		               orders.setKmsumma(kmsumma);
		               orders.setShipMode(cred);
		               orders.setKm(km);
		               orders.setYm(ymmardus);
		               orders.setEmailBody(comment);       
		               orders.add(firma, prod, prodName ,kogus, dokNr, null, 0, disc,date,"0",price,paydays,false,
		            		   "",99,unit,cred,batch,ean,null,true,null,null);         
		               counter++;
		               }
		            
		            prod = null;
		          
		       //  System.exit(0);
		          }
		         }
			} catch (Exception e1) {
			
				e1.printStackTrace();
			}
		       }


  private void loadXMLViaUusArve(Orders orders, File f,StringBuffer troubles) {
		
	  
	    
	    String date = null;
	    String paydate = null;
	    String cred = "";
	    String comment = "";
	    double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "E-Document");		  	    
      Node root2 = getNode(root, "Document");
     
      
	    String dokNr  = getNode(getNode(root2, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();		    
	    kogusumma = Double.parseDouble(getNode(getNode(root2, "DocumentSumGroup"), "DocumentSum").getFirstChild().getNodeValue());
	    
	    String reference = "";
	    try{
	    	 reference = getNode(getNode(getNode(getNode(root2, "DocumentInfo"), "RefInfo"),"SourceDocument")
	    			 ,"SourceDocumentNum").getFirstChild().getNodeValue();
	    }catch(Exception k){
	   	}
	    
	    
	    
	    String original = "";	    
	    try{
	    	Node rida = getNode(getNode(root2, "DocumentInfo"), "RefInfo");
	    	int asju =  rida.getChildNodes().getLength();
	   
	    	for (int u = 0; u< asju ; u++){
	        
	    		Node nn = rida.getChildNodes().item(u);
	        		  

	        		  if(nn.getNodeName().equals("SourceDocument") && nn.getAttributes().getNamedItem("type").getNodeValue().equals("order")){
	        			  original = getNode(nn , "SourceDocumentNum").getFirstChild().getNodeValue();  
	        			  System.out.println("nname 2 origx "  + original);
	  		        	
	        		  }
	        		  
	        		  if(nn.getNodeName().equals("SourceDocument") && nn.getAttributes().getNamedItem("type").getNodeValue().equals("desadv")){
	        			  original = getNode(nn , "SourceDocumentNum").getFirstChild().getNodeValue();  
	        			  System.out.println("nname 3 origx "  + original);
	  		        	
	        		  }
	        		  
	        		  if(nn.getNodeName().equals("SourceDocument") && nn.getAttributes().getNamedItem("type").getNodeValue().equals("Mmforder")){
	        			  reference = getNode(nn , "SourceDocumentNum").getFirstChild().getNodeValue();  
	        			  System.out.println("nname 1 refx "  + reference);
	  		        	
	        		  }
	        			
	        	  }
	        	  
	    	
	    }catch(Exception k){
	    		k.printStackTrace();
	    	}
	    
	    /*
	     * <SourceDocument type="order">
<SourceDocumentNum>4698321</SourceDocumentNum>
</SourceDocument>
<SourceDocument type="Mmforder">
<SourceDocumentNum>MPM150460</SourceDocumentNum>
</SourceDocument>
<SourceDocument type="desadv">
<SourceDocumentNum>AL000846</SourceDocumentNum>
</SourceDocument>*/
	    
	    
	    
	    try{
		    date = getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"InvoiceDate").getFirstChild().getNodeValue();	
		       if(!date.equals("")){
		      		date = getCalLit(date).getTime() + "";
		        }
		    long  pay = getCalLit(getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"DueDate").getFirstChild().getNodeValue()).getTime();	
		
		       paydays = this.getPaydays(Long.parseLong(date), pay);
	   }catch(Exception ee ){			   
	   }
	   	
	
	 
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	firma =  getNode(getNode(getNode(root2, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	System.out.println("Firma " + firma);
	    	firma =  driver.identifyCustomer(firma,regnr);
	    }catch (Exception e){ 
	    	e.printStackTrace();
	    }
	         	
			  if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED VIA3L XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }		    	
	    	
			  Node read = getNode(root2, "DocumentItem");
		    	
			    
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	      
	           	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName ="";
	          	


	          	
				    double  kogus = Double.parseDouble(getNode(rida, "AmountInvoiced").getFirstChild().getNodeValue()) ;
	         
				 try{
	          	    ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();
	          	}catch(Exception ss){
	          		ss.printStackTrace()	;
	          	}
	          
	          	
	         
	          	 
	            try{
	            	
	             prod = toode.substring(0,toode.length()-2);
	                  } catch (Exception e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED VIA3L XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }

		         
	                  String batch = "";
	   	           try{
	   	        	   //Siia peab tegem al�bi itereerimise 
	   	        	int asju =  rida.getChildNodes().getLength();
	   	        	  
	   	        	  for (int u = 0; u< asju ; u++){
	   	        		  Node nn = rida.getChildNodes().item(u);
	   	        		// System.out.println("nname " + nn.getNodeName());
	   	        		  if(nn.getNodeName().equals("ItemReserve")){
	   	        			  if(batch.equals("")){
	   	        				  batch = getNode(nn, "LotNum").getFirstChild().getNodeValue(); 
	   	        			  }else{
	   	        				  if(batch.indexOf(getNode(nn, "LotNum").getFirstChild().getNodeValue()) == -1){
	   	        					 batch = batch + "|" + getNode(nn, "LotNum").getFirstChild().getNodeValue(); 
	 	   		   	        	 }
	   	        			  }
	   	        		  }
	   	        	  }
	   	        	  
	   	        	  System.out.println("batch " + batch);
	   	        	   
	   	        //	batch =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
	   	        	//batch = batch + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
	   		       		//	"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	   	           }catch(Exception e){
	   	        	   
	   	           }
	   	           
	   	  	  
               double discount = 0;
               try{
            	   discount = Double.parseDouble(getNode(getNode(rida, "Addition"), "AddRate").getFirstChild().getNodeValue());
               }catch(Exception rr){
            	   
               }
               
               System.out.println("ref "+ dokNr +  " " + firma +
         			  " " + date + " " + reference + " " + kogusumma + " " + prod + " " + batch + " " + discount);		   		
                   	   

	               double price = Double.parseDouble(getNode(rida, "ItemPrice").getFirstChild().getNodeValue());
	             
	               double km = 0;
	               String unit = "";
	        
	               
	               if(!firma.equals("")){
	             	
	               orders.setInfo(reference);   
	               orders.setSumma(kogusumma);
	               orders.setKmsumma(kmsumma);
	               orders.setShipMode(cred);
	               orders.setKm(km);
	               orders.setYm(ymmardus);
	               orders.setEmailBody(comment); 
	               orders.setTeostaja(original);
	               orders.add(firma, prod, prodName ,kogus, dokNr, null, 0, discount,date,"0",price,paydays,
	            		   false,"",99,unit,cred,batch,ean,null,true,null,null);         
	               counter++;
	               }
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }


  
  
  
  
  
  private void loadXMLerplArve(Orders orders, File f,StringBuffer troubles) {
		
	  
	    
	    String date = null;
	    String paydate = null;
	    String cred = "";
	    String comment = "";
	    double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "E-Document");		  	    
      Node root2 = getNode(root, "Document");
    
  
	//    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	 //   String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
	   
      
	    String dokNr  = getNode(getNode(root2, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();		    
	    kogusumma = Double.parseDouble(getNode(getNode(root2, "DocumentSumGroup"), "DocumentSum").getFirstChild().getNodeValue());
	    
	    String reference = ""; 
	    try{
	    	Node nn = getNode(getNode(root2, "DocumentInfo"), "RefInfo");
	    	for(int i = 0 ; i < nn.getChildNodes().getLength() ; i++){
	    	
	    		if(nn.getChildNodes().item(i).getNodeName().equals("SourceDocument")) {
	    			try{
	    				reference = getNode(nn.getChildNodes().item(i),"SourceDocumentNum").getFirstChild().getNodeValue();
	    			}catch(Exception r){
	    			}
	    		}
	    		
	    		
	    	}
	
	    }catch(Exception ee ){
	    	ee.printStackTrace();
		   }
	  
	    
	    try{
		    date = getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"InvoiceDate").getFirstChild().getNodeValue();	
		       if(!date.equals("")){
		      		date = getCalLit(date).getTime() + "";
		        }
		    long  pay = getCalLit(getNode(getNode(getNode(root2, "DocumentInfo"), "DateInfo"),"DueDate").getFirstChild().getNodeValue()).getTime();	
		
		       paydays = this.getPaydays(Long.parseLong(date), pay);
	   }catch(Exception ee ){			   
	   }
	   	
	   try{
	    cred = getNode(getNode(getNode(root2, "DocumentParties"), "OrderParty"),"PartyCode").getFirstChild().getNodeValue();
	   }catch(Exception ee ){
		   ee.printStackTrace();
	   }
	   
	 
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	// mm�ja
	    	firma =  getNode(getNode(getNode(root2, "DocumentParties"), "SellerParty"),"RegNum").getFirstChild().getNodeValue();
	    	firma =  driver.identifyCustomer(firma,firma);
	    }catch (Exception e){    
	    }
	         	
			  if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED ERPLYTELEMA XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }		    	
	    	
			  Node read = getNode(root2, "DocumentItem");
		    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	      
	        	  System.out.println("ref "+ dokNr +  " " + firma +
	        			  " " + date + " " + reference + " " + kogusumma);		   		
	              
	           	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName =getNode(rida, "ItemDescription").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	          	
	          	 
	         
	          	
				    double  kogus = Double.parseDouble(getNode(rida, "AmountInvoiced").getFirstChild().getNodeValue()) ;
	         
				 try{
					 if(  getNode(rida, "GTIN") != null){
						    ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();	 
					 }else{
						 ean = null;
					 }
	          	}catch(Exception ss){
	          		
	          	ss.printStackTrace()	;
	          	}
	         
	          	 
	            try{
	            	
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED ERPLY TELEMA XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " " +  ean + " amount: " + kogus + "\n\n");
	                   }
	                  }
	                  String batch = "";
	   	           try{
	   	        	batch =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
	   	        	batch = batch + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
	   		       			"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	   	           }catch(Exception e){
	   	        	   
	   	           }
	   	           
	                  

	               double price = Double.parseDouble(getNode(rida, "ItemPrice").getFirstChild().getNodeValue());
	             
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	        
	               if(getNode(rida, "excise") != null){
		           //    batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
	               }
	               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch );
	               if(!firma.equals("")){
	             	
	               orders.setInfo(reference);   
	               orders.setSumma(kogusumma);
	               orders.setKmsumma(kmsumma);
	               orders.setShipMode(cred);
	               orders.setKm(km);
	               orders.setYm(ymmardus);
	               orders.setEmailBody(comment);       
	               orders.add(firma, prod, prodName ,kogus, dokNr, null, 0, disc,date,"0",price,paydays,false,"",99,unit,cred,batch,ean,
	            		   null,true,null,null);         
	               counter++;
	               }
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }



  
  public void sendExceptionEmail(String buf , String filename){
	  String address = getProps().getProperty("exceptionemail");
	  if(address != null ){
		  Email email = new Email(parent.getString("importerror"));
         
          email.setFrom(getProps().getProperty("from"));
          email.setSMTP(getProps().getProperty("smtphost"));
          email.setUser(getProps().getProperty("smtpuser"));
          email.setPass(getProps().getProperty("smtppass"));
          email.setPort(getProps().getProperty("smtpport"));
          email.setSubjectFilesTo(parent.getString("importerror") + " NR:" + doknr + " File:"+ filename );
          if(getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBody(buf.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              email.send();
              }
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  
	  }
  }
  
  public void sendTroubleEmail(StringBuffer buf , String filename){
	  sendTroubleEmail(buf.toString() , filename);
  }
  
  
  public void sendTroubleEmail(String buf , String filename){
	  String address = getProps().getProperty("troubleemail");
	  String subjTX = getProps().getProperty("subjtx");
	  String invoice = "";
	  if(getProps().getProperty("invoice") != null){
		  invoice = " " + getProps().getProperty("invoice") + " " + company;
	  }
	  
	  if(address != null ){
		  Email email = new Email(parent.getString("importerror"));
         
          email.setFrom(getProps().getProperty("from"));
          email.setSMTP(getProps().getProperty("smtphost"));
          email.setUser(getProps().getProperty("smtpuser"));
          email.setPass(getProps().getProperty("smtppass"));
          email.setPort(getProps().getProperty("smtpport"));
          email.setType("text/html");
          String doknn = "";
          if(doknr != null){
        	  doknn = " NR: " + doknr;
          }
          String format = "";
          String bb = buf.toString();
          String shopCode = "";
          String sub = parent.getString("importerror");
          try{
        	  if(bb.indexOf("RESOLVED") != -1){
            	  format = bb.substring(bb.indexOf("RESOLVED") + 8 , bb.toLowerCase().indexOf("format") );
              }
              
       
              if(bb.indexOf("SHOPCODE") != -1){
            	  format = bb.substring(bb.indexOf("SHOPCODE") + 8 , bb.toLowerCase().indexOf("by buyer") );
              }
           
              if(subjTX != null){
            	  sub = subjTX;
              }
            	  
          }catch(Exception e ){
        	  
          }
           String subj = sub + shopCode + format + invoice + doknn + " File:  \"" + filename + "\"";
          System.out.println(subj);
          email.setSubjectFilesTo(subj); //vahele invoice 
          if(getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          //email.setEncoding("iso-8859-1");
          try {
        	  
        		String subject = email.getSubjectFilesTo();
        		StringBuffer sb = new StringBuffer();
        		sb.append("<HTML>\n");
        		sb.append("<HEAD>\n");
        		sb.append("<TITLE>\n");
        		sb.append(subject + "\n");
        		sb.append("</TITLE>\n");
        		sb.append("</HEAD>\n");

        		sb.append("<BODY>\n");
        		sb.append("<H1>File:  <A href=\"" + filename + "\">" + filename +"</A>" + "</H1><BR>");
        		//<A href="../images/forest.gif">map of the enchanted forest.</A>
        		//<A href=" + filename + ">" + filename +"</A>
        
        		    sb.append(buf.toString().replaceAll("\n", "<BR>"));
        		    sb.append("<BR>");

        		sb.append("</BODY>\n");
        		sb.append("</HTML>\n")    ;    	  
        	  
        	email.setBody( sb.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              System.out.println("Setting address " + aadr);
              email.send();
              }
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  
	  }
  }

  private void sendReportEmail(StringBuffer buf,String filen){
	  String address = getProps().getProperty("reportemail");
	  
	  String invoice = "";
	  if(getProps().getProperty("invoice") != null){
		  invoice = " " + getProps().getProperty("invoice") + " " + company;
	  }
	  
	  if(address != null ){
		  Email email = new Email("Saadetud tellimuste loend");
         
          email.setFrom(getProps().getProperty("from"));
          email.setSMTP(getProps().getProperty("smtphost"));
          email.setUser(getProps().getProperty("smtpuser"));
          email.setPass(getProps().getProperty("smtppass"));
          email.setPort(getProps().getProperty("smtpport"));
          email.setType("text/html");
          String doknn = "";
          if(doknr != null){
        	  doknn = " NR: " + doknr;
          }
          String format = "";
          
          String shopCode = "";
          String sub = "Smartenisse edastatud tellimuste nimistu " + filen;
          
           String subj = sub  ;
          System.out.println(subj);
          email.setSubjectFilesTo(subj); //vahele invoice 
          if(getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          //email.setEncoding("iso-8859-1");
          try {
        	  
        		String subject = email.getSubjectFilesTo();
        		StringBuffer sb = new StringBuffer();
        		sb.append("<HTML>\n");
        		sb.append("<HEAD>\n");
        		sb.append("<TITLE>\n");
        		sb.append(subject + "\n");
        		sb.append("</TITLE>\n");
        		sb.append("</HEAD>\n");

        		sb.append("<BODY>\n");
        		//sb.append("<H1>File:  <A href=\"" + filename + "\">" + filename +"</A>" + "</H1><BR>");
        		//<A href="../images/forest.gif">map of the enchanted forest.</A>
        		//<A href=" + filename + ">" + filename +"</A>
        
        		    sb.append(buf.toString().replaceAll("\n", "<BR>"));
        		    sb.append("<BR>");

        		sb.append("</BODY>\n");
        		sb.append("</HTML>\n")    ;    	  
        	  
        	email.setBody( sb.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              System.out.println("Setting address " + aadr);
              email.send();
              }
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  
	  }
  }
  private void importOrder(Orders orders,StringBuffer troubles) throws SQLException{

    Enumeration enume = orders.getOrders().elements();
    int count = 0;
    double summa = 0;
    while(enume.hasMoreElements()){
      count++;
      Order order = (Order)enume.nextElement();
      order.setCompName(orders.getCompName());
      driver.setNumAddedLines(0);
      int linecount = order.getOrderLines().size();
     // System.out.println(order.toString());
      if(troubles.length() > 10){

          order.setChanged(true);  
      }
      driver.makeOrder(order);
      
      summa = order.getSum() * 1.20d;
     System.out.println("pLJU " + driver.getNumAddedLines() +  " " + linecount);
      if (driver.getNumAddedLines() != linecount && !parent.isAutoImport()){
        String mes = parent.getString("countlines");
        showError(new Exception(mes));
        String comp = parent.getString("company");
        String orderLines = parent.getString("orderlines");
        String addedLines = parent.getString("addedlines");
        addLog(comp + " " + firma + " " + orderLines + " " +
               linecount + ",\n" + addedLines + " " + driver.getNumAddedLines() + "\n");
        troubles.append("Error: " + comp + " " + firma + " " + orderLines + " " +
                linecount + ",\n" + addedLines + " " + driver.getNumAddedLines() + "\n");
      }
      if(driver.getTrouble() != null &&driver.getTrouble().trim().length()>5 ){
    	  troubles.append(driver.getTrouble());
      }
      System.out.println("Probleemide trykk" + troubles);
      
    }

    
    
    String added = parent.getString("added");

    if (count == 1){
      if (summa > 0){
        addLog(added + " " + count + " " + parent.getString("insum") + " " +
             getEksportFrame().getEksportPanel().getNF().format(summa) + "\n");
      } else {
        addLog(added + " " + count + " " + parent.getString("order") + "\n");
      }
    } else {
      if (summa > 0){
        addLog(added + " " + count + " " + parent.getString("orderinsum") + " " +
               getEksportFrame().getEksportPanel().getNF().format(summa) + "\n");
      } else {
        addLog(added + " " + count + " " + parent.getString("orders") + "\n");
      }
    }
  }

  private static String getAgentFromFileName(String path){
    String agentName = null;
    String fileName = null;
    try{
      File file = new File(path);
      fileName = file.getCanonicalFile().getName();
    } catch (IOException e){}
    int index = fileName.indexOf(".");
    if (index > 0){
      agentName = fileName.substring(0, index);
    }
    index = agentName.indexOf("(");
    if (index > 0){
      agentName = agentName.substring(0, index);
    }
    return agentName;
  }
  
  private void loadXMLArve(Orders orders, File f,StringBuffer troubles) throws Exception{
	    XMLDok xml = new XMLDok(f,null);
	    String fileName = f.getName();
	    Vector conCod = null;
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    int counter = 0;
	    String regnr = null;
	    String datelong = "";
	    String reference = "";
	    String doknr = "";
	    String markused = null;
	    String tarnekell = null;
	    String kood2 = "";
	    if(xml.getTarneKuupaev()!= null){
	    	datelong = xml.getTarneKuupaev().getTime()+"";	
	    }
	  try{
	    if(xml.getTellijaRegnr()!= null){
	    	regnr = xml.getTellijaRegnr();	
	    }
	  }catch (Exception e){
	  	//e.printStackTrace();
	  }
	  String ladu  = xml.getFiliaal();
      String tarnijaKood = xml.getTellijaKood();
	  
	  try{
	   kood2 = xml.getTarnijaKood();
	  }catch (Exception e){
		  	e.printStackTrace();
		  }
	  try{
	      firma = driver.identifyCustomer(tarnijaKood,regnr);
	      conCod = getCustConvCodes(firma);
	      System.out.println("firmakood " +firma + "|" + tarnijaKood + "|" + regnr);
	    } catch (SQLException e) {	
	    }
	    System.out.println("firmakood " +firma + "|" + tarnijaKood + "|" + regnr);
	    if(firma == null || firma.trim().length()==0){
	    	  try{
	    	      firma = driver.identifyCustomer(kood2,null);
	    	      conCod = getCustConvCodes(firma);
	    	      
	    	      System.out.println("firmakood " +firma + "|" + tarnijaKood + "|" + regnr);
	    	    } catch (SQLException e) {
	    	    //e.printStackTrace();	
	    	    }
	    }
	    System.out.println("kood2 " +firma + "|" + tarnijaKood + "|" + regnr);
	    if (firma == null || firma.trim().length() == 0){
	      addLog(parent.getString("nocompid") + " " + tarnijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	      troubles.append("Customer CANNOT BE RESOLVED  XML ARVE format \n" + "Dok Nr: " + doknr +  " " + reference+ "\n"
					 + "\t\tRegNr:" + regnr + " Code:" + tarnijaKood + "\n\n");
	     
	    }
	    
	    
	    if(reference == null || reference.trim().length() == 0){
	    	reference = " ";
	    }
	    try{
	 	   markused = xml.getMarkused();
	    }catch(Exception e){
	 	   e.printStackTrace();
	    }
	    try{
		 	   doknr = xml.getDokNr();
		    }catch(Exception e){
		 	   e.printStackTrace();
		    }
	    
	    for (Iterator iter = xml.getOrderLines().iterator(); iter.hasNext(); ){
	       ee.mobilejava.earve.OrderLine ol = (OrderLine)iter.next();
	        name = ol.getProdName();
	      
             double price = ol.getPrice(); 
	     toode = ol.getPartnerProdID();
	     ean = ol.getEAN();
	      try{
	      prod = driver.identifyProduct(ean,toode);
	    
	            } catch (SQLException e) {}
	            if (prod == null){
	              addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	             // break;
	          	prod = " ";
	          	if(prod.trim().length() == 0 ){
	        		 troubles.append("PRODUCT CANNOT BE RESOLVED XML ARVE format : \n" + "Dok Nr:  " + doknr +  " " + reference+ "\n"
	        				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " Amount:" + ol.getAmount() + "\n\n");
	           }
	            }
	            double convert = 1 ;
	            boolean up = false;
	         //   System.out.println("conCod.size()" + conCod.size());
	            if(conCod != null){
	            	Enumeration enume = conCod.elements();
	            	while(enume.hasMoreElements()){
	            		ConvertData dd= (ConvertData)enume.nextElement();
	            		System.out.println("toode" + toode + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+ " " + dd.getConvert());
	            		if(dd.getProdOwn().equals(prod)){
	            			convert = dd.getConvert();
	            			up = dd.isRoundup();
	            			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
	              				prod = dd.getProdOrder();
	              			}
	            		}
	            	}
	            	
	            }
	          double  kogus = ol.getAmount() * convert;
	            
	          if(up){
	          	System.out.println("kogus" + kogus);
	          	kogus = Math.round(kogus + 0.49);
	          	System.out.println("kogusrr" + kogus);
	          }
	          String nameCu = "";
	          if(firma == null){
	        	  nameCu = tarnijaKood;
	          }
	        	if(reference != null && reference.indexOf("-") != -1 ){
	        		reference = reference.substring(reference.indexOf("-")+1);
	        	}
	          
	      orders.add(firma, prod, name , kogus ,doknr, null, 0, 0,datelong,"0",price,0,false,"",96," ",ladu,"",ean,tarnekell,true,null,null);
	     if (markused != null){
	    	 orders.setInfo(reference + " " + markused); 
	     }else{
	    	 orders.setInfo(reference); 
	     }
	      System.out.println("Getinfo " + orders.getInfo());
	      counter++;
	      orders.setCompName(nameCu);
	      System.out.println("taenekell " + tarnekell);
	      orders.setShipTime(tarnekell);
	      System.out.println("taenekell " + orders.getShipTime());
	      prod = null;
	    }
	  }
  

  private void loadXMLOrder(Orders orders, File f,StringBuffer troubles) throws Exception{
    XMLDok xml = new XMLDok(f,null);
    String fileName = f.getName();
    String tellijaKood = xml.getFiliaal();
    if(tellijaKood.equals("-") || tellijaKood.equals("00") ){
    	tellijaKood = xml.getTellijaKood();
    }
   
    
    Vector conCod = null;
   
    String name = " ";
    firma = "";
    String prod = null;
    String ean = null;
    String toode = null;
    int counter = 0;
    String regnr = null;
    String datelong = "";
    String reference = "";
    String markused = null;
    String tarnekell = null;
    String kood2 = "";
    double tagastus = 1;
    
    if(xml.getTarneKuupaev()!= null){
    	datelong = xml.getTarneKuupaev().getTime()+"";	
    }
  try{
    if(xml.getTellijaRegnr()!= null){
    	regnr = xml.getTellijaRegnr();	
    }
  }catch (Exception e){
  	//e.printStackTrace();
  }
  
  try{
	    if(regnr != null){
	    	tarnekell = xml.getTarneAeg();	
	    }
	  }catch (Exception e){
	  	e.printStackTrace();
	  }
  
  if(xml.getRoot().getNodeName() != null && xml.getRoot().getNodeName().equals("tagastus")) {
	  tagastus = -1;
  }
  
  try{
   kood2 = xml.getTellijaKood();
  }catch (Exception e){
	  	e.printStackTrace();
	  }
  try{
      firma = driver.identifyCustomer(tellijaKood,regnr);
      conCod = getCustConvCodes(firma);
      
      System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
    } catch (SQLException e) {
    //e.printStackTrace();	
    }
    System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
    if(firma == null || firma.trim().length()==0){
    	  try{
    	      firma = driver.identifyCustomer(kood2,null);
    	      conCod = getCustConvCodes(firma);
    	      
    	      System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
    	    } catch (SQLException e) {
    	    //e.printStackTrace();	
    	    }
    }
    System.out.println("kood2 " +firma + "|" + tellijaKood + "|" + regnr);
    if (firma == null || firma.trim().length() == 0){
      addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
      troubles.append("Customer CANNOT BE RESOLVED  XML format \n" + "Dok Nr: " + doknr +  " " + reference+ "\n"
				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
     
     // break;
    }
   try{
	   reference = xml.getDokNr();
   }catch(Exception e){
	   
   }
   
    if(reference == null || reference.trim().length() == 0){
    	reference = " ";
    }
    try{
 	   markused = xml.getMarkused();
    }catch(Exception e){
 	   e.printStackTrace();
    }
    System.out.println("Markused " + markused);
    for (Iterator iter = xml.getOrderLines().iterator(); iter.hasNext(); ){
       ee.mobilejava.earve.OrderLine ol = (OrderLine)iter.next();
        name = ol.getProdName();
      

    // System.out.println(xml.getTarneKuup�ev().toLocaleString() + "   ref:" + reference);	
     toode = ol.getPartnerProdID();
     System.out.println("id '" + ol.getPartnerProdID() + "'" + ol.getProdID());
     ean = ol.getEAN();

      try{
      prod = driver.identifyProduct(ean,toode);
      
       } catch (SQLException e) {
    	   e.printStackTrace();
       }
            if (prod == null){
              addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
             // break;
          	prod = " ";
          	if(prod.trim().length() == 0 ){
        		 troubles.append("PRODUCT CANNOT BE RESOLVED XML format : \n" + "Dok Nr:  " + doknr +  " " + reference+ "\n"
        				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " Amount:" + ol.getAmount() + "\n\n");
           }
            }
            double convert = 1 ;
            boolean up = false;
         //   System.out.println("conCod.size()" + conCod.size());
            if(conCod != null){
            	Enumeration enume = conCod.elements();
            	while(enume.hasMoreElements()){
            		ConvertData dd= (ConvertData)enume.nextElement();
            		System.out.println("toode" + toode + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+ " " + dd.getConvert());
            		if(dd.getProdOwn().equals(prod)){
            			convert = dd.getConvert();
            			up = dd.isRoundup();
            			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
              				prod = dd.getProdOrder();
              			}
            		}
            	}
            	
            }
          double  kogus = ol.getAmount() * convert;
            
          if(up){
          	System.out.println("kogus" + kogus);
          	kogus = Math.round(kogus + 0.49);
          	System.out.println("kogusrr" + kogus);
          }
          String nameCu = "";
          if(firma == null){
        	  nameCu = tellijaKood;
          }
        	if(reference != null && reference.indexOf("-") != -1 ){
        		reference = reference.substring(reference.indexOf("-")+1);
        	}
          
        	double disc = 0;
        	if(ol.getDiscount() > 0){
        		disc = ol.getDiscount();
        	}
        	
      orders.add(firma, prod, name , kogus * tagastus ,reference, null, 0, disc,datelong,"0",0,0,false,"",96," ",null,"",ean,tarnekell,true,null,null);
     if (markused != null){
    	 orders.setInfo(reference + " " + markused); 
     }else{
    	 orders.setInfo(reference); 
     }
      System.out.println("Getinfo " + orders.getInfo());
      counter++;
      orders.setCompName(nameCu);
      System.out.println("taenekell " + tarnekell);
      orders.setShipTime(tarnekell);
      System.out.println("taenekell " + orders.getShipTime());
      prod = null;
    }
  }

  

  
  private void loadHTMOrder(Orders orders, File f,StringBuffer troubles) throws Exception{
  	 String fileName = f.getName();
     FileReader fr = new FileReader(f);
     BufferedReader br = new BufferedReader(fr);
     String line = null;

     String tellijaKood = null;
      firma = "";
      String datelong = "";
     String tellijaRegnr = null;
     String kood = null;
     int counter = 0;
     String reference = null;
   /*  while( (line = br.readLine()) != null){
     //  System.out.println(line + " " + counter);
          
            if (line.equals("</SCRIPT>")){
            //  break;
            }
     }*/
     
     
     while( (line = br.readLine()) != null){
       line = line.trim();
    	//Tarne kuup.:
   
       if (line.indexOf("Tarne kuup.:") != -1){
           line = br.readLine();
           datelong = line.substring(line.indexOf(">")+1);
           datelong = datelong.substring(0,datelong.indexOf("<"));        
           System.out.println(datelong);
           datelong = this.getCalHTM(datelong);
         }
   	
       
       	if (line.indexOf("Tellimuse nr.:") != -1){
            line = br.readLine();
            reference = line.substring(line.indexOf(">")+1);
            reference = reference.substring(0,reference.indexOf("<"));
            
            System.out.println(reference);
          }
      
       	
  
       
       if (line.indexOf("nr.:1") > -1){
         tellijaRegnr = line.substring(line.indexOf(":")+1);
         tellijaRegnr = tellijaRegnr.substring(0,tellijaRegnr.indexOf("<"));
         System.out.println(tellijaRegnr); 
        
       }
       
        if (line.indexOf("tellija filiaali nimi:</") > -1){
         
          	 line = br.readLine();
        	tellijaKood = line.substring(line.indexOf(">")+1);
        	tellijaKood = tellijaKood.substring(0,tellijaKood.indexOf("<"));
 
            
         try{
           if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
           kood = tellijaRegnr ; } else {kood = tellijaKood;}
           // System.out.println("Kood: "+ kood);
           firma = driver.identifyCustomer(kood,tellijaRegnr);
           if(firma == null && kood.length() >  6){
        	   firma = driver.identifyCustomer(kood.substring(0,6),tellijaRegnr);
           }
          
           System.out.println("'"+firma+"'");
         } catch (Exception e) {
         e.printStackTrace();	
         }
         
         if (firma.trim().length() == 0){
           addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
           troubles.append("Customer CANNOT BE RESOLVED HTML format \n"+ "Dok Nr: " + doknr  + " "+ reference+ "\n" 
     				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
          
           //break;
         }
       
       } 
        if(line.indexOf("<TH ALIGN=\"right\">Summa</TH>") > -1){
        	line = br.readLine();
        	line = br.readLine();
        	if(true){
        	System.out.println("'J�udsime ridade juurde'");
        	String ol = null;
         String prod = " ";
         while( (ol = br.readLine()) != null){
          ol = br.readLine();
       //  System.out.println(ol);
           
         String toode  = ol.substring(ol.indexOf(">")+1);
     
          toode = toode.substring(0,toode.indexOf("<"));
         // System.out.println("toode"+ toode);
          
         ol = br.readLine();
         String ean  = ol.substring(ol.indexOf(">")+1);
         	ean = ean.substring(0,ean.indexOf("<")); 
           // System.out.println("ean rida"+ ol);
         
         	if(toode.equals("&nbsp;")){
         		toode = ean;
         	}
         	
           ol = br.readLine();
          // System.out.println("name rida"+ ol);
           String name  = ol.substring(ol.indexOf(">")+1);
           try{
           	name = name.substring(0,name.indexOf("<"));
           }catch (Exception e){
           	ol = br.readLine();
           }
           
          
           
           ol = br.readLine();
           String unit  = ol.substring(ol.indexOf(">")+1);
           unit = unit.substring(0,unit.indexOf("<"));
                 
          
       
           try{
           prod = driver.identifyProduct(toode.trim(),toode.trim());
           System.out.println("tuvastamisele " + toode +"  tagasi tuvastamiselt "+ prod);
        } catch (Exception e) {
        e.printStackTrace();	
        }
        String kog = "0";
        while( (ol = br.readLine()) != null){
        	//System.out.println("KOG" + ol);
        	if(ol.indexOf("name=\"QNT")!= -1){
        		kog = ol.substring(ol.indexOf("value=")+7);
        		
        		kog = kog.substring(0,kog.indexOf(" ")-1);
        		
        	}
        	if(ol.indexOf("<TR class=\"main_backgrounded\">")!= -1){
        		break;
        	}
        }
        
        if (prod == null){
          addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
          //break;
          prod = " ";
          if(prod.trim().length() == 0 ){
      		 troubles.append("PRODUCT CANNOT BE RESOLVED HTML format : \n" + "Dok Nr: " + "Dok Nr: " + doknr + " " + reference + "\n"
      				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " Amount:" + kog + "\n\n");
         }
        }
        
       
        
        
           double kogus = Double.parseDouble(kog);
          // System.out.println(firma+"'"+prod+"'"+kogus);
           System.out.println(toode +"  "+ean + "  " + name + "  "+ unit + "  " + kog);
           if(!datelong.equals("")){
           		datelong = getCalXML(datelong);
           }
           
           orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",99," ",null,"",ean,null,true,null,null);
           counter++;
           orders.setInfo(reference);
           
         }
       }
     }
    	
 	}
     br.close();
     fr.close();	
  }
  

  private void loadHTMRXOrder(Orders orders, File f,StringBuffer troubles) throws Exception{
	  	 String fileName = f.getName();
	     FileReader fr = new FileReader(f);
	     BufferedReader br = new BufferedReader(fr);
	     String line = null;
	     boolean first = true;
	     boolean second = false;

	     String tellijaKood = null;
	      firma = "";
	      String datelong = "";
	     String tellijaRegnr = null;
	     String kood = null;
	     int counter = 0;
	     String reference = null;
	     while( (line = br.readLine()) != null){
	     	//  System.out.println(line + " " + counter);
	            if (line.indexOf("\">Pas") != -1){
	              break;
	            }
	     }
	     
	     reference = line.substring(line.indexOf(">Pas")+12);
         reference = reference.substring(0,reference.indexOf("<"));
         System.out.println("reference " + reference);
	     
         String firmaident = "LV40003673764";

         
	     while( (line = br.readLine()) != null){
	       line = line.trim();
	
	  
	       
	       if (line.indexOf("nr.:1") > -1){

	        // line = br.readLine();
	         tellijaRegnr = line.substring(line.indexOf(":")+1);
	         tellijaRegnr = tellijaRegnr.substring(0,tellijaRegnr.indexOf("<"));
	         System.out.println(tellijaRegnr); 
	        
	       }
	       
	       if(line.indexOf("Summa<BR></") > -1){
	            
	         try{
	           if(tellijaRegnr != null && tellijaKood == null){
	           kood = tellijaRegnr ; } else {kood = tellijaKood;}
	           // System.out.println("Kood: "+ kood);
	           firma = driver.identifyCustomer(kood,firmaident);
	           System.out.println("'"+firma+"'");
	         } catch (SQLException e) {
	        //   e.printStackTrace();	
	         }
	         
	         /*if (firma.trim().length() == 0){
	           addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	           troubles.append("Customer CANNOT BE RESOLVED HTML Format \n" 
	     				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
	          
	           //break;
	         }*/
	       
	        
	        
	       
	        	line = br.readLine();
	            while((line = br.readLine()).equals("<TR>")){
	            	
	            }
	            line = br.readLine();
                while((line = br.readLine()).equals("<TR>")){
	            	
	            }
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
	        	if(true){
	        		System.out.println("'J�udsime ridade juurde'");
	        	String ol = null;
	         String prod = " ";
	         while( !(ol = br.readLine()).equals("</TABLE>") && (first || second)){
	        	 
	        	  
	          ol = br.readLine();
	          ol = br.readLine();
	          ol = br.readLine();
	          ol = br.readLine();
	          ol = br.readLine();
	       
	      //   System.out.println( "ol " + ol);
	           
	         String toode  = ol.substring(ol.indexOf("a\">")+3);
	     try{
	    	 toode = toode.substring(0,toode.indexOf("<"));
	     }catch(Exception e){
	    	 break;
	     }
	         
	         // System.out.println("toode"+ toode);
	          
	         ol = br.readLine();
	         ol = br.readLine();
	         String ean  = ol.substring(ol.indexOf("a\">")+3);
	         	ean = ean.substring(0,ean.indexOf("<")); 
	        //    System.out.println("ean rida"+ ol);
	         
	           ol = br.readLine();
	           ol = br.readLine();
	          // System.out.println("name rida"+ ol);
	           String name  = ol.substring(ol.indexOf("a\">")+3);
	           try{
	           	name = name.substring(0,name.indexOf("<"));
	           }catch (Exception e){
	           //	ol = br.readLine();
	           }
	           
	           
	           ol = br.readLine();
	           ol = br.readLine();
	           String kog  = ol.substring(ol.indexOf("a\">")+3);
		        		kog = kog.substring(0,kog.indexOf("<"));
		        		
		        	
		        	
		        	
		            
			           ol = br.readLine();
			           ol = br.readLine();
			           String unit  = ol.substring(ol.indexOf("a\">")+3);
			           unit = unit.substring(0,unit.indexOf("<"));
			                 
			           	
		        	
		        	while ((ol = br.readLine()) != null){
		        		if(ol.indexOf("</TR>")!= -1){
			        		break;
			        	}	
		        	}
		        	
		        
	     
	       
	           try{
	           prod = driver.identifyProduct(ean,toode);
	           System.out.println(toode +"  "+ prod);
	        } catch (SQLException e) {
	       // e.printStackTrace();	
	        }
	     
	        
	        if (prod == null){
	          addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	          //break;
	          prod = " ";
	          if(prod.trim().length() == 0 ){
	      		 troubles.append("PRODUCT CANNOT BE RESOLVED HTLM format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	      				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " Amount:" + kog + "\n\n");
	         }
	        }
	        
	       
	        
	        
	           double kogus = Double.parseDouble(kog);
	          // System.out.println(firma+"'"+prod+"'"+kogus);
	           System.out.println(toode +"  "+ean + "  " + name + "  "+ unit + "  " + kog);
	           if(!datelong.equals("")){
	           		datelong = getCalXML(datelong);
	           }
	           
	           orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",99," ",null,"",ean,null,true,null,null);
	           counter++;
	           orders.setInfo(reference);
	           
	         }
	         if(first){
	        	 System.out.println("Esimene lehekylg on l�bi");
	        	 System.out.println("Suurus " + counter);
	         }else{
	        	 System.out.println("Teine lehekylg on l�bi");
	        	 System.out.println("Suurus " + counter);
	         }
	         first = false;
	         second = true;	        
	         
	       }

	     }
	    	
	 	}
	     br.close();
	     fr.close();	
	  }
  
  
  


  private void loadPDFPurOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
	
  	 
	  File dir = new File("C:/x/");
 	 if(!dir.exists()){
 		 dir.mkdir();
 	 }
	  
  	 File fil = new File ("C:/x/pdftemp.txt");
  
     PDDocument document = null;
     try
     {
         document = PDDocument.load( f.getAbsolutePath() );
         if( document.isEncrypted() )
         {
             try
             {
                 document.decrypt( "" );
             }
             catch( InvalidPasswordException e )
             {
            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
                 System.err.println( "Error: Document is encrypted with a password." );
                 System.exit( 1 );
             }
         }
         
         PDFTextStripper ss = new PDFTextStripper();
        
             String aa =  ss.getText(document);
             
  
         // System.out.println(aa);
            
             if(fil.exists()){
             	fil.delete();
             }
            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
            fos.write(aa.getBytes());
            fos.close();
          

        
     } finally{
         if( document != null ) {
             document.close();
         }
     }
     
     //System.exit(0);
     
     FileReader fr = new FileReader(fil);
     BufferedReader br = new BufferedReader(fr);
     FileReader frhea = new FileReader(fil);
     BufferedReader brhea = new BufferedReader(frhea);

     FileReader frch = new FileReader(fil);
     BufferedReader brch = new BufferedReader(frch);
     
     String line = null;
     int counter = 0;
      firma = "";
      String datelong = "";
     String reference = null;
     String check = null;
     boolean nestle = false;
     boolean henkel = false;
     boolean colgate = false;
     boolean npfoods = false;
     boolean felix = false;
     while ((check = brch.readLine()) != null){
    	 if(check.indexOf("UAB \"Nestle Baltics\"")!=-1){
    		 nestle = true;
    		 System.out.println("Nestle variant");
    		 company = " NESTLE ";
    		// return;
    	 }
    	 if(check.indexOf("ORKLA FOODS")!=-1 || check.indexOf("497044060003242217")!=-1){
    		 felix = true;
    		 System.out.println("FELIX variant");
    		 company = " FELIX ";
    		// return;
    	 }
    	
    	 if(check.indexOf("NP Foods Lita")!=-1){
    		 npfoods = true;
    		 System.out.println("NPFoods variant");
    		 company = " NP Foods Lita ";
    		// return;
    	 }
    	 
    	 if((check.indexOf("SIA \"Henkel Latvia\"") !=-1 || check.indexOf("SIA HENKEL LATVIA") !=-1) && !henkel){
    		 henkel = true;
    		System.out.println("Henkel variant");
    		 company = " HENKEL ";
    		// return;
    	 }
    	 
    	 if((check.indexOf("PL1180006759")!=-1 || check.indexOf("PL97103015080000000500044187") != -1 ) && !colgate){
    		 colgate = true;
    		System.out.println("Colgate variant");
    		 company = " COLGATE ";
    		// return;
    	 }
     }
     brch.close();
     frch.close();
    
     // kui on felix siis on allj�rgnev
     if(felix){
    	  
   	   firma =  driver.identifyCustomer(null, "felix");
 
   	   while( (line = br.readLine()) != null ){        
           if( line.trim().equals("Serija SFP")){
        	   break;
           }
           if(line.startsWith("Nr. ")){
        	   reference = line.trim().substring(line.indexOf("Nr. ") + 4); 
           }
           if(line.indexOf("Terminas:") != -1){
        	   String termin = line.substring(0,8);
        	   line = line.substring(line.indexOf("Terminas:")+9);
        	   int term  = Integer.parseInt(line.substring(0,line.indexOf(" ")));
        	   datelong =  getCalFel(termin,term).getTime() + "";
           }  
  	    } 	
      long aa = Long.parseLong(datelong);   
      if(reference == null){
    	  reference = "FEL00099";
      }
      System.out.println(firma + " " + reference + " " + aa +  " " + new Date(aa).toGMTString());
       String valuta = "EUR";//dealer i koha peal on valuuta 
       
  
       while( (line = br.readLine()) != null){
          		 System.out.println("line " + line );
       	
             
 
       		if(line.length() > 60){
       		 String unit = "VNT";
       			int vnt = line.indexOf("VNT");
       			if(vnt == -1){
       				vnt =  line.indexOf(" KG");
       			 unit = "KG"; 
       			}else{
       				unit = "vnt.";
       			}
       		    
       			int vat = 21;
       		   		
    		if(vnt != -1 ){
    		
       			String name = line.substring(0,vnt).trim();
       		      		
       		    String line2 = line.substring(vnt + 3);
       		     String partii = line2.substring(line2.length()-10).trim();
       		             line2 = line2.substring(0,line2.length()-10).trim();
       		             line2 = line2.substring(0,line2.lastIndexOf(" "));
       		
       		     String ean = line2.substring(line2.lastIndexOf(" ")).trim(); 
       		             line2 = line2.substring(0,line2.lastIndexOf(" "));
       		      
       		         String koguskood = line2.substring(0,line2.indexOf(" "));
       		      System.out.println("koguskood " + koguskood);
       		  
       		   koguskood = koguskood.replaceAll(",", "");
       		 System.out.println("koguskood " + koguskood);
       		         int len = koguskood.length();
       		         String resu = ""; 
       		         for( int uu = 0;uu<len;uu++){
       		        	if(isNumeric(koguskood.charAt(uu))){
       		        		if(koguskood.charAt(uu) != '.'){
       		        			resu = resu + koguskood.charAt(uu);	
       		        		}      		        		
       		        	}else{
       		        		break;
       		        	}
       		         }
       		      System.out.println(resu);
       		           	 double  kogus = Double.parseDouble(resu);  
       		     
       		          line2 = line2.substring(line2.indexOf(" ")).trim();
       		           	 
       		           String resu2 = line2.substring(line2.lastIndexOf(" ")).trim();
       		        resu2 = resu2.replaceAll(",", "").trim();
       		     
       		           resu2 = resu2.substring(0,resu2.length()-1);
       		        String summa = resu2; 
       		        int coun = 100;
      		         for( int uu = 0; uu<resu2.length(); uu++){
      		        	if(isNumeric(resu2.charAt(uu))){
      		        		if(resu2.charAt(uu) != '.'){
      		        			//summa = summa + resu2.charAt(uu);	
      		        		}  
      		        		if(resu2.charAt(uu) == ','){
      		        			//summa = summa + '.';	
      		        			coun = 0;
      		        		}  
      		        		if(coun == 2){
      		        			break;
      		        		}
      		        		coun++;
      		        	}else{
      		        		break;
      		        	}
      		         }
       		           System.out.println("summa " + summa + " " + resu2);
       		             double hind = Double.parseDouble(summa)/kogus;     
       		       
       	 	     System.out.println("Lines starting " + unit + " " +  name + " " + ean + " " + vat + " " + kogus  + " " + hind );
       	 		
              String prod = driver.identifyProduct(ean, null );
              if(prod== null ){
        			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF FELIX format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
        					+ "\tLineNum: "  +  (counter+1) + " " + name +" Code:" + ean + " Amount:" + kogus + "\n\n");
        			 prod = " ";
                 }
              orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,
            		  false,f.lastModified()+ "",80,unit,"PDF format FELIX lithuania","",ean,f.getName(),true,null,null);
               orders.setInfo(reference);
               orders.setKm(vat);
               
               
               counter++;
           	
    		}   
       		 }
       	 }
    }
     
     
     //kui on NP Foods siis on allj�rgnev 
     
     if(npfoods){
 	  
 	   firma =  driver.identifyCustomer(null, "npfoods");
 	  line =  br.readLine().trim();
     reference = line.trim().substring(line.lastIndexOf(" "));
     br.readLine().trim();
     br.readLine().trim();
     br.readLine().trim();
   line =   br.readLine().trim();
     datelong =  getCalLit(line.trim()).getTime() + "";
     System.out.println(firma + " " + reference + " " + datelong);
     String valuta = "EUR";//dealer i koha peal on valuuta 
     
     
     while( (line = br.readLine()) != null){        
         if (line.trim().equals("Partijos Nr.")){
           break;
         }
	    }

  
   
     while( (line = br.readLine()) != null){
     	
    	 if(line.startsWith("PVM S")){
    		  while( (line = br.readLine()) != null){        
    		         if (line.trim().equals("Partijos Nr.")){
    		        	 line = br.readLine();
    		           break;
    		         }
    			    } 
    	         }
    	 
     		 System.out.println("line " + line );
     		 if(line.indexOf("VISO BE PVM:") == -1 && line.indexOf("Partijos Nr.") == -1 &&  !isNumeric(line.trim().substring(line.trim().length()-6))){
     			  line = line + br.readLine();
     			 System.out.println("!! " + line);
     		 }
     		
     		 if(line.indexOf("VISO BE PVM:") == -1 && line.indexOf("Partijos Nr.") == -1  && !isNumeric(line.trim().substring(line.trim().length()-6))){
    			  line = line + br.readLine();
    			 System.out.println("!! " + line);
    		 }
     
     		 if(line.indexOf("SUMA APM")!= -1){
     			 System.out.println("l�pp lool");
     			 break;
     		 }
     		 
     		 if(line.length() > 60){
     			
     		
     			 
        	 String ean = line.substring(0,13);       	 
             String rsult = line.trim().substring(0,line.lastIndexOf(" "));     	
       	 
             String unit = line.substring(line.lastIndexOf(" ")).trim();
             String unit2 = rsult.trim().substring(rsult.trim().lastIndexOf(" ")).trim();
           System.out.println("UNIT " + unit + " " + unit2 );
              if (unit.startsWith("KG") || unit2.startsWith("KG")){
            	  unit = "KG";
              }else{
            	  unit = "vnt."; 
              }
              
              
              
              String juhuks = rsult;
              rsult = rsult.substring(0,rsult.lastIndexOf(" "));
         	 String asi  = rsult.substring(rsult.lastIndexOf(" ")).trim();
         	 if(asi.equals("")){
         		 asi = juhuks.substring(juhuks.lastIndexOf(" ")).trim();
         	 }
         	 System.out.println("asi kkk " + asi);
         	 double  kogus = Double.parseDouble(asi.substring(asi.indexOf(".")+3));       
             int vat = 21;
             double hind = Double.parseDouble(asi.substring(0,asi.indexOf(".")+3))/kogus;     
             rsult = rsult.substring(0,rsult.lastIndexOf(" ")).trim();
             rsult = rsult.substring(0,rsult.lastIndexOf(" ")).trim();
       	     String name = rsult.substring(14);       
       	     System.out.println("Lines starting " + unit + " " +  name + " " + ean + " " + vat + " " + kogus  + " " + hind );
          //  System.exit(0); 
            String prod = driver.identifyProduct(ean, null );
            if(prod== null ){
      			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF NPFOODS format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
      					+ "\tLineNum: "  +  (counter+1) + " " + name +" Code:" + ean + " Amount:" + kogus + "\n\n");
      			 prod = " ";
               }
            orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,false,
            		f.lastModified()+ "",80,unit,"PDF format NPFOODS lithuania","",ean,f.getName(),true,null,null);
             orders.setInfo(reference);
             orders.setKm(vat);
             
             
             counter++;
             
     		 }
     	 }
  }
     
   
    //kui on colgate siis allj�rgnev

     if(colgate){
    	
    	 String valuta = "EUR";//dealer i koha peal on valuuta 
    	 
    	    while( (line = brhea.readLine()) != null){        
              if (line.indexOf("Nr faktury / Invoice No:") != -1){
               
        reference = line.substring(line.indexOf("Nr faktury / Invoice No:")+25).trim();
        line =  brhea.readLine().trim();
        datelong =  getCalHTM(line.substring(line.indexOf("Data faktury / Inv.Date:") + 25).trim());
       
                }
    	    }
  
    	brhea.close();
        frhea.close();
  //siin on headeri asjade lugemise l�pp

        firma =  driver.identifyCustomer(null, "colgate");
        System.out.println(firma + " " + reference + " " + datelong);
           
        
        while( (line = br.readLine()) != null){
        	    line = line.trim();
        	    System.out.println( "!!!!" + line );
        	 if(line.indexOf("_______")== -1){
        		 System.out.println(line );
        		 line = line.substring(line.indexOf(" ")).trim();
        		 String prodid = line.substring(0,line.indexOf(" ")).trim();
        		 String prod = driver.identifyProduct(null, prodid + "|27956" );
        		 
        		 
        		 line = line.substring(line.indexOf(" ")).trim();
        		 
        		 String jaak = line;
        		 
        		 int vv = jaak.lastIndexOf(" ");
        		 jaak = jaak.substring(0,vv).trim();
        		
        		 vv = jaak.lastIndexOf(" ");
        		 jaak = jaak.substring(0,vv).trim();
        		 
        		 vv = jaak.lastIndexOf(" ");
        		  String hin = jaak.substring(vv);
        		  jaak = jaak.substring(0,vv).trim();
         		 vv = jaak.lastIndexOf(" ");
        		  double  kogus = Double.parseDouble(jaak.substring(vv).replace(',', ' ').replace('.',' ').replaceAll(" ", ""));
        		 
        		  String name = jaak.substring(0,vv);
        		  name = name.substring(name.indexOf(" ")).trim();
        		  name = name.substring(name.indexOf(" ")).trim();
              
              
                hin = hin.substring(hin.lastIndexOf(" ")).trim();
                hin = hin.replace('.', ' ').replaceAll(" ", "");
                hin = hin.replace(',', '.');
           	    double hind = Double.parseDouble(hin);
           	    
           	
           	    

       		 if(prod== null ){
       			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF Colgate format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
       					+ "\tLineNum: "  +  (counter+1) + " "  + name +" Code:" + prodid + " Amount:" + kogus + "\n\n");
       			 prod = " ";
                }
           	   String unit = "CASE";
               System.out.println("Lines starting " +  name + " " + " " + kogus  + " " + hind );
              
               orders.add(firma, prod, name ,kogus, reference, valuta, 0, 0,datelong,"0",hind,0,false,f.lastModified()+ "",
            		   80,unit,"PDF format COLGATE poland","","",f.getName(),true,null,null);
                orders.setInfo(reference);
              
                counter++;
        	 }else{
        		 System.out.print("jama " + line);
        		 while((line = br.readLine()) != null){
        			// System.out.println("jama 2 " + line);
        			 if(line.indexOf("Balance brought") != -1){
        				 break;
        			 }
        			//
        		 }
        	 }
        }    
     }
     
     
     
     
     // Kui on nestle siis toimub all olev
     
     
     
     if(nestle){
    	    while( (line = br.readLine()) != null){        
                if (line.indexOf("Invoice No.") != -1){
                  break;
                }
    	    }
    	   firma =  driver.identifyCustomer(null, "nestle");
        reference = br.readLine().trim();
        line =  br.readLine().trim();
        datelong =  getCalLit(line.substring(line.indexOf("Data/Date:") + 10).trim()).getTime() + "";
        System.out.println(firma + " " + reference + " " + datelong);
        String valuta = "EUR";//dealer i koha peal on valuuta 
        
        while( (line = br.readLine()) != null){        
            if (line.trim().equals("%")){
              break;
            }
	    }
      
        String lineold= "";
        while( (line = br.readLine()) != null){
        	
        	 if (line.indexOf("PAL") != -1){
                 break;
               } 

        	 if (line.trim().length() == 1 || line.trim().length() == 2){
                 line =  br.readLine();
                 System.out.println("MUUTSIN " + line);
               } 
        	 if(line.indexOf("ninis")!= -1 && line.length() > 45){
        		 System.out.println(line );
        		 String prodid = line.substring(0,8).trim();
            	 String name = line.substring(8,line.indexOf("ninis")-4);
           	  String ean = line.substring(line.indexOf("EAN Code:") + 9);
                 if(ean.trim().length() == 0){
                 	ean = br.readLine().trim();
                 } 
               String prod = driver.identifyProduct(ean, prodid + "|111684" );
                 line = br.readLine();
                double  kogus = Double.parseDouble(line.substring(0, line.indexOf(" ")).replace('.', ' ').replaceAll(" ", "").replace(',', '.'));
                int vat = Integer.parseInt(line.trim().substring(line.trim().lastIndexOf(" ")+1));
                String hin = line.substring(0,line.length()-((vat+"").length()+1)).trim();
                hin = hin.substring(hin.lastIndexOf(" ")).trim();
                hin = hin.replace('.', ' ').replaceAll(" ", "").replace(',', '.');
           	    double hind = Double.parseDouble(hin)/kogus;
           	    
           	   String unit = "CASE";
               System.out.println("Lines starting " +  name + " " + ean + " " + vat + " " + kogus  + " " + hind );  
               if(prod== null ){
         			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF NESTLE format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
               				 + "\tLineNum: "  +  (counter+1) + " " + name +" Code:" + prodid + " Amount:" + kogus + "\n\n");
         			 prod = " ";
                  }
               orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,false,
            		   f.lastModified()+ "",80,unit,"PDF format NESTLE lithuania","",ean,f.getName(),true,null,null);
                orders.setInfo(reference);
                orders.setKm(vat);
             
                
                counter++;
        	 }else if (line.indexOf("ninis")!= -1 && line.length() < 45 && line.trim().length() > 5){
        		
        		 System.out.println(lineold + "'''" + line );
        		 try{
        			
        		 String prodid = lineold.substring(0,8).trim();
            	 String name = lineold.substring(8);
           	  String ean = line.substring(line.indexOf("EAN Code:") + 9);
                 if(ean.trim().length() == 0){
                 	ean = br.readLine().trim();
                 } 
               String prod = driver.identifyProduct(ean, prodid + "|111684");
                 line = br.readLine();
                 double  kogus = Double.parseDouble(line.substring(0, line.indexOf(" ")).replace('.', ' ').replaceAll(" ", "").replace(',', '.'));
                 int vat = Integer.parseInt(line.trim().substring(line.trim().lastIndexOf(" ")+1));
                 String hin = line.substring(0,line.length()-((vat+"").length()+1)).trim();
                 hin = hin.substring(hin.lastIndexOf(" ")).trim();
                 hin = hin.replace('.', ' ').replaceAll(" ", "").replace(',', '.');
            	    double hind = Double.parseDouble(hin)/kogus;
            	    String unit = "CASE";
                  System.out.println("Lines starting " +  name + " " + ean + " " + vat + " " + kogus  + " " + hind );  
                  if(prod== null ){
            			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF Nestle format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
            					 + "\tLineNum: "  + (counter+1)+ " " + name +" Code:" + prodid + " Amount:" + kogus + "\n\n");
            			 prod = " ";
                     }
                  
                  orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,false,
                		  f.lastModified()+ "",80,unit,"PDF format NESTLE lithuania","",ean,f.getName(),true,null,null);
                   orders.setInfo(reference);
                   orders.setKm(vat);
                   counter++;
                   
        		 }catch(Exception e){
        			 
        		 }
        		 
        	 }
        	  lineold = line;
        }    
     }
     
     
     
     // kui on Henkel toimub allolev 
     
     if(henkel){
 	    while( (line = brhea.readLine()) != null){        
             if (line.startsWith("Invoice")){
               break;
             }
 	    }
 	  firma =  driver.identifyCustomer(null, "henkel");
 	    System.out.println("line 1" + line);
 	    line =  brhea.readLine().trim();
 	 reference =brhea.readLine().trim();
    
     line =  brhea.readLine().trim();
     datelong =  this.getCalHTM( line =  brhea.readLine().trim()) + "";
     System.out.println(firma + " " + reference + " " + datelong);
     String valuta = "EUR";//dealer i koha peal on valuuta 
     double discount = 0;
	 while( (line = brhea.readLine()) != null){
		// System.out.println(line);
  		 if(line.indexOf("KA Discounts Prepayment")!= -1  ){
  			 String disc = line.substring(23).trim();
  			
  			 disc = disc.substring(0,disc.indexOf("-")).trim();
  			 disc = disc.substring(0,disc.length()).replaceAll(",", ".");
  			 System.out.println("globaldisc2 " + disc);
  			 discount = Double.parseDouble(disc);
  			 break;       			 
  	      } 
	 }
	//   firma =  driver.identifyCustomer(null, "henkel");
	    

     brhea.close();
     frhea.close();
     
   
    
     
     //siin l�peb headeri info otsimine
     
     
     
     
     while( (line = br.readLine()) != null){
    	
     	 if (line.indexOf("Total Number of pieces") != -1){
     		 
          	 while( (line = br.readLine()) != null){
          		 
          		   if(line.indexOf("Page") != -1 || line.indexOf("No. ") != -1){
          			 while( (line = br.readLine()) != null){
          				 if(line.indexOf("Deliv. quantity") != -1){
          					line = br.readLine();
          					 break;
          				 }
          			 }
          		 }

          		   if(line == null || line.indexOf("TOTAL ")!= -1 
          				 || line.indexOf("I N V O I C E  S U M")!= -1  ){    //|| line.indexOf("________________________________________") != -1    			 
          			 break;       			 
          	      }
          		
          		
     		 try{
          		
          		
          		  if(line.indexOf("1 CON   =  1 CON") != -1 ){
          			  br.reset();
          			  line = br.readLine();
          		  }
          		
          		if(line.startsWith("KA Discounts")) {
          			line = br.readLine();
          		}
          		
          		
          		
          		 if(line.indexOf("Page") != -1 || line.indexOf("No. ") != -1){
          			 while( (line = br.readLine()) != null){
          				 if(line.indexOf("Deliv. quantity") != -1){
          					line = br.readLine();
          					 break;
          				 }
          			 }
          		 }
          	//	System.out.println("aaa " +line);
              	
          		String name = line.substring(0,line.lastIndexOf(" "));
          		 String ean = line.substring(line.lastIndexOf(" ")).trim();
             		// String name = line.trim().substring(line.trim().indexOf(" "),line.trim().indexOf("      "));
          	     br.mark(500);
          	
          		double  kogus = 0;
          		String hin = null;
          		double hind = 0;
          		String prodid = null;
          		try{
          	
          			line = br.readLine().trim();
              		 prodid = line.substring(0,line.indexOf(" ")).trim();
              		line = br.readLine();
              		if(line.startsWith("1 CON   =  1 CON")) {
              			line = br.readLine();
              		}
              	
              				line = line .trim().replaceAll(" ", "|");
              //		 System.out.println("ccc0 " + line );
                   	StringTokenizer st = new StringTokenizer(line,"|");
              		String  kog = st.nextToken().trim();
          			kogus = Double.parseDouble(kog.replace('.', ' ').replaceAll(" ", "").replace(',', '.'));
                	 st.nextToken();
                	  hin = st.nextToken().trim();
                	   hin = hin.replace('.', ' ').replaceAll(" ", "").replace(',', '.');
                	
                	   double dd = 1;
                     if(discount != 0 ){
                  	   dd = (100-discount)/100;
                     }
                	 	
                	    hind = Double.parseDouble(hin) * dd;
                		 br.mark(500);
                	    line = br.readLine();
          			
          		}catch(Exception y ){
          			//y.printStackTrace();
          			br.reset();
          			
          			line = br.readLine().trim();
          		//	System.out.println("ccc0 " + line );
              		// prodid = line.substring(0,line.indexOf(" ")).trim();
              		line = br.readLine().trim().replaceAll(" ", "|");
              		 System.out.println("ccc1 " + line );
                   	StringTokenizer st = new StringTokenizer(line,"|");
              		String  kog = st.nextToken().trim();
              		if(!kog.startsWith("TOTAL")) {
              			
              	
          			kogus = Double.parseDouble(kog.replace('.', ' ').replaceAll(" ", "").replace(',', '.'));
                	 st.nextToken();
                	  hin = st.nextToken().trim();
                	   hin = hin.replace('.', ' ').replaceAll(" ", "").replace(',', '.');
                	
                	   double dd = 1;
                     if(discount != 0 ){
                  	   dd = (100-discount)/100;
                     }
                	    hind = Double.parseDouble(hin) * dd;
                	    line = br.readLine();
              		}
                	  // System.out.println("ccc2 " + line );
          		}
          		
          		
          		          	 
          	  
          		
          	//  System.out.println("cccxxx " + counter + " " + name + " " + ean + " " + kogus + " " + hin  + " " + discount + " " + prodid + " " + hind );
          		//if(counter == 2)
         		// System.exit(0);    

            	
      //      String prod =  driver.identifyProduct(ean.trim(), prodid + "|165362" );
                int vat = 0;
          String prod = "1029928";
             
                if(prod== null ){
          			 troubles.append("PRODUCT CANNOT BE RESOLVED PDF Henkel format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
          					+ "\tLineNum: "  +  (counter+1) + " " + name +" Code:" + prodid + " Amount:" + kogus + "\n\n");
          			 prod= " ";
                   }
           	   String unit = "vnt.";
                System.out.println("Lines starting " +  name + " " + ean + " " + vat + " " + kogus  + " " + hind );  
                orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,false,f.lastModified()+ ""
                		,85,unit,"PDF format HENKEL lithuania","",ean,f.getName(),true,null,null);
                orders.setInfo(reference);
                orders.setKm(vat);
                  
                if(line.startsWith("TOTAL")){
            		  break;
            	  }
                
                  counter++;
}catch(Exception r){
      r.printStackTrace();    			
          		}
          		
          		}
         // 	System.exit(0);
          		
     	 
     	 } 
     	
     	
     }
 	    
 	    
  }
     
  }

  
  
  private void loadTXTPurOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
		
	  	 
		     
	     FileReader fr = new FileReader(f);
	     BufferedReader br = new BufferedReader(fr);
	     FileReader frhea = new FileReader(f);
	     BufferedReader brhea = new BufferedReader(frhea);

	     
	     String line = null;
	     int counter = 0;
	      firma = "";
	      String datelong = "";
	     String reference = null;
	     String check = null;
	     boolean alita = false;
	     while ((check = brhea.readLine()) != null){
	    	 if(check.indexOf("ALITA")!=-1){
	    		 alita = true;
	    		 System.out.println("ALITA txt variant");
	    		 company = " ALITA ";
	    	 }
	     }
	     brhea.close();
	     frhea.close();
	     
	     
	     //kui on ALITA siis on allj�rgnev 
	     
	     if(alita){
	 	  
	 	  firma =  driver.identifyCustomer(null, "alita");
	 	  line =  br.readLine().trim();
	     reference = line.trim().substring(line.lastIndexOf(" "));
	     br.readLine().trim();
	   line =   br.readLine().trim();
	     datelong =  getCalLit(line.trim().substring(0,10)).getTime() + "";
	     System.out.println(firma + " " + reference + " " + datelong);
	     String valuta = "EUR";//dealer i koha peal on valuuta 
	     
	     
	     while( (line = br.readLine()) != null){        
	         if (line.trim().startsWith("Prek")){
	           break;
	         }
		    }

	  
	   
	     while( (line = br.readLine()) != null){
	     	
	    	 if(line.indexOf(" viso") != -1){
	    		 break;
	    	 }
	     		 System.out.println("line " + line );
	    		 
	     		 if(line.length() > 60){
	     	line = line.replaceAll("\t", "| ");
	     	StringTokenizer st = new StringTokenizer(line , "|");
	     	String prodid = st.nextToken().trim();
	        	      
	        	      String name = st.nextToken().trim();     
	        	      st.nextToken().trim();   
	        	      st.nextToken().trim();   
String token = st.nextToken().trim().replace('.', ' ');
System.out.println("token" + token);
	 	         double  kogus = Double.parseDouble(token.replaceAll(",", ".").replaceAll(" ", ""));     	 
	 	             st.nextToken().trim();   
      	             st.nextToken().trim(); 
      	           token = st.nextToken().trim().replace('.', ' ');
	             double hind = Double.parseDouble(token.trim().replaceAll(",", ".").replaceAll(" ", ""))/kogus;    
	             
	             String unit = "vnt."; 
     	          int vat = 21;
	             System.out.println("Lines starting " + unit + " " +  name + " " + prodid + " " + vat + " " + kogus  + " " + hind );
	          //  System.exit(0); 
	            String prod = driver.identifyProduct(null,  prodid + "|405592" );
	            if(prod== null ){
	      			 troubles.append("PRODUCT CANNOT BE RESOLVED TXT ALITA format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	      					+ "\tLineNum: "  +  (counter+1) + " " + name +" Code:" + prodid + " Amount:" + kogus + "\n\n");
	      			 prod = " ";
	               }
	            orders.add(firma, prod, name ,kogus, reference, valuta, vat, 0,datelong,"0",hind,0,false,
	            		f.lastModified()+ "",80,unit,"TXT format ALITA lithuania","","",f.getName(),true,null,null);
	             orders.setInfo(reference);
	            
	             orders.setKm(vat);
	             counter++;
	     		 }
	     	 }
	    }
	  }
  private void loadPDFPostOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
	  	 
	  	String fileName = f.getAbsolutePath();
	  	 File fil = new File ("C:/x/pdftemp.txt");
	  	
	     PDDocument document = null;
	     try
	     {
	         document = PDDocument.load( f.getAbsolutePath() );
	         if( document.isEncrypted() )
	         {
	             try
	             {
	                 document.decrypt( "" );
	             }
	             catch( InvalidPasswordException e )
	             {
	            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
	                 System.err.println( "Error: Document is encrypted with a password." );
	                 System.exit( 1 );
	             }
	         }
	         
	         PDFTextStripper ss = new PDFTextStripper();
	        
	             String aa =  ss.getText(document);
	             
	  
	         // System.out.println(aa);
	            
	             if(fil.exists()){
	             	fil.delete();
	             }else{
	            	 File dir = new File("C:/x/");
	            	 if(!dir.exists()){
	            		 dir.mkdir();
	            	 }
	            	 }
	            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
	            fos.write(aa.getBytes());
	            fos.close();
	          

	        
	     } finally{
	         if( document != null ) {
	             document.close();
	         }
	     }
	     
	     FileReader fr = new FileReader(fil);
	     BufferedReader br = new BufferedReader(fr);
	     
	     
	     String line = null;
	     int counter = 0;
	     String tellijaKood = null;
	      firma = "";
	      String datelong = "";
	     String tellijaRegnr = null;
	     String kood = null;
	     br.readLine();
	     br.readLine();
	     br.readLine();
	     br.readLine();
	     br.readLine();
	     br.readLine();
	     
	     String reference = br.readLine();
	     reference = reference.substring(18).trim();
	     
	 	 String datum = br.readLine().substring(36).trim();
	 	 System.out.println("datum " + datum);
	 	 
	    	 datelong = getCalHTM(datum);  
	    	 br.readLine();
		     try{
		    		kood = br.readLine().substring(33).trim().replaceAll(" ", ""); 
		    		 
			 }catch(Exception e ){
		    	 e.printStackTrace();
		     }
	    	 
	    	 System.out.println("Dates1 " + datum + " " + datelong + " "  + reference + " " + firma + " " + line);
	
	    	 datelong = Calendar.getInstance().getTimeInMillis() + "";
	 try{

	        firma = driver.identifyCustomer(kood,null);
	       //  System.out.println("'"+firma+"'");
	      } catch (Exception e) {
	      // e.printStackTrace();	
	       } 
       
	       if (firma == null || firma.trim().length() == 0){
	         addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	         troubles.append("Customer CANNOT BE RESOLVED PDF EESTI POST format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
	  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
	         doknr=reference;
	       }
	     

		     while( (line = br.readLine()) != null ){ 
		    	 if (line.indexOf("Kaubakood") != -1){
		    		 break;
		    	 }
		     }
	    
	 
	     while( (line = br.readLine()) != null){  
	    	    
	    	 String ean  = line;
	    	 
	    		if(line.indexOf("Tarnija")!= -1){
        			
        			//System.out.println("Sees " + line);
        			 while( (line = br.readLine()) != null ){ 
        		    	 if (line.indexOf("Kaubakood") != -1){
        		    		 break;
        		    	 }
        		     }
        			 
        	    	 
        			 line = br.readLine() ;
        			 ean  = line;
        			// System.out.println("Sees1 " + line);
        		}
	    	 
	    	 
	    	// line = br.readLine();
	        if(line != null){
	        
	        	try{
	        		
	        	
	        		
	        		 System.out.println("liin1 " +line);
	        		line = line.replaceAll(" ", " |");
		        	line = line.replaceAll("  ", " ");
	        		
	        		 if(line.indexOf("Baashind") == -1 ){
	        		
	        		 try{
	        			 line = line.substring(0,line.indexOf("|� |� |� |� |"));
	        		 }catch(Exception e){
	        			// line = line.substring(0,line.indexOf("|  |  |"));    �/// |8410702015646 |� |PENASOL |TINTO |6 |tk
	        		 }
	        		 
	        	
	        	StringTokenizer st = new StringTokenizer(line,"|");
	        	
	        	String prod = null;
	         	   
	        	    line = line.substring(0,line.lastIndexOf("|"));
	        	    line = line.substring(0,line.lastIndexOf("|"));
	        	    
	        	    
	        	    int ind = line.lastIndexOf("|"); 
	        	   String unit = line.substring(ind+1);
	        	   line = line.substring(0,line.indexOf(unit)-1);
	        	   ind = line.lastIndexOf("|"); 
	        	     String kog = line.substring(ind+1);
	     	        
	         	// System.out.println("ean " + ean + " " + unit + " " + kog);
	         	
	             String name = line.substring(0,ind).replace('|', ' ').replace('|', ' ').replace('|', ' ').trim();
	             name = name.replaceAll("  ", " ");
	             if(ean.length() > 13){
	            	 ean = ean.substring(0,13);
	             }
	             System.out.println(counter + ". ean : " + ean + " * " + prod + " * " + name + " * " + kog ); 
	            
	             try{
	            	  prod = driver.identifyProduct(ean,null);
	              }catch (Exception e){
	            	 
	             }
      
	       
	        	 
	             
	             
	             if ( prod == null ){
	                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                 //break;
	                 prod = " ";
	            		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF Eesti Post format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + kog + "\n\n");
	            		 doknr=reference;
	               
	               }
	               
	               
	                  double kogus =  0;
	                  try{
	               	   kogus =  Double.parseDouble(kog.replace(',', '.'));
	                  }catch (Exception e){
	               	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
	               	  // System.out.println("ffffff" + kog);
	               	   kogus =  Double.parseDouble(kog);   
	                  }


	                  
	                  orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",92," ",null,"",ean,null,true,null,null);
	                
	                  orders.setInfo(reference);
	                  counter++;
	             	 
	        		 }
	        	}catch(Exception e){
	        		//e.printStackTrace();
	        	}

     	}
	        	} 
	   
	     addLog("Total Order Lines added:" + counter);
	     
	     br.close();
	     fr.close();
	  }


  
  private void loadPDFRautaOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
	  	 
	  	String fileName = f.getAbsolutePath();
	  	 File fil = new File ("C:/x/pdftemp.txt");
	  	
	     PDDocument document = null;
	     try
	     {
	         document = PDDocument.load( f.getAbsolutePath() );
	         if( document.isEncrypted() )
	         {
	             try
	             {
	                 document.decrypt( "" );
	             }
	             catch( InvalidPasswordException e )
	             {
	            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
	                 System.err.println( "Error: Document is encrypted with a password." );
	                 System.exit( 1 );
	             }
	         }
	         
	         PDFTextStripper ss = new PDFTextStripper();
	        
	             String aa =  ss.getText(document);
	             
	  
	         // System.out.println(aa);
	            
	             if(fil.exists()){
	             	fil.delete();
	             }else{
	            	 File dir = new File("C:/x/");
	            	 if(!dir.exists()){
	            		 dir.mkdir();
	            	 }
	            	 }
	            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
	            fos.write(aa.getBytes());
	            fos.close();
	          

	        
	     } finally{
	         if( document != null ) {
	             document.close();
	         }
	     }
	     
	     FileReader fr = new FileReader(fil);
	     BufferedReader br = new BufferedReader(fr);
	     
	     
	     String line = null;
	     int counter = 0;
	     String tellijaKood = null;
	      firma = "";
	      String datelong = "";
	     String tellijaRegnr = null;
	     String kood = null;
	    
	     while( (line = br.readLine()) != null ){ 
	    	 if (line.indexOf("Pealelaadimiskoht:") != -1){
	    		 break;
	    	 }
	     }
	     
	   
	     try{
	    		kood = br.readLine().trim().replaceAll(" ", ""); 
		 }catch(Exception e ){
	    	 e.printStackTrace();
	     }
	     
	     while( (line = br.readLine()) != null ){ 
	    	 if (line.indexOf("Tagastustellimus") != -1){
	    		 break;
	    	 }
	     }
	  
	     String reference = line.substring(line.lastIndexOf(" ")).trim();
	  	 String datum = br.readLine().substring(8,19).trim();
	     
	    	 datelong = getCalHTM(datum);  
	    	 System.out.println("Dates1 " + datum + " " + datelong + " "  + reference + " " + kood + " "  + firma + " " + line);
	    	 datelong = Calendar.getInstance().getTimeInMillis() + "";
	        
	      try{

	        firma = driver.identifyCustomer(kood,null);
	       //  System.out.println("'"+firma+"'");
	         } catch (Exception e) {
	      // e.printStackTrace();	
	       } 
	 //seeon Horticomi rautakeskokood 
          firma = "4963";
	       if (firma == null || firma.trim().length() == 0){
	         addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	         troubles.append("Customer CANNOT BE RESOLVED PDF EESTI POST format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
	  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
	         doknr=reference;
	       }
	     

	      	       
		     while( (line = br.readLine()) != null ){ 
		    	 if (line.indexOf("Artikli kirjeldus") != -1){
		    		 break;
		    	 }
		     }
	    
		     int linn = 0;
	     while( (line = br.readLine()) != null){  
	    	    
	    	 //siin on esimene rida mida sisselugeda - reanumbri ja koodid ja kogus janimi 
	    	 
	    	 try{
	    		// br.mark(500);	
	    		 String linenum = line.substring(0,line.indexOf(" "));
	    			 String left = line.substring(linenum.length()+ 1).trim();
	    			 String tkood = left.substring(0,left.indexOf(" "));
	    			 left = left.substring(tkood.length()+ 1).trim();
	    			 String ean = left.substring(0,left.indexOf(" "));
	    			 String unit = left.substring(left.lastIndexOf(" ")).trim();
	    			 left = left.substring(0,left.indexOf(unit));
	    			  int linnew = Integer.parseInt(linenum);
	    			 
	    			 String prod = null;
	         	     String kog = left.trim().substring(left.trim().lastIndexOf(" ")).trim().replace(',', '.');
	         	
	             String name = br.readLine().trim();
	   
	             
	             
	          /*   while( (line = br.readLine()) != null ){ 
		       	    	if (line.indexOf("Artikli kirjeldus") != -1){
				    		 break;
				    	 }
					    	 if (line.indexOf("Sissetuleku") != -1){
					    		 break;
					    	 }
					    	
					    	 if((linn+10) == linnew){
					    		 br.reset();
					    		 break; 
					    	 }
					    	 
					     }*/
	             try{
	            	 linn = Integer.parseInt(linenum);
	 	         }catch(Exception t ){
	             }
	             
	        	 name = name.replaceAll("  ", " ");
	             if(ean.length() > 13){
	            	 ean = ean.substring(0,13);
	             }
	             
	             try{
	            	  prod = driver.identifyProduct(ean,null);
	              }catch (Exception e){
	             }
	           
	             System.out.println((counter+1) + ". ean : " + ean + " * " + linenum + " * " + prod + " * " + name + " * " + kog  + " * " + unit ); 
		         
	            
	       	     if(ean.trim().length() < 6){
	       	    	System.out.println("Ei Loe seda sisse ean : " + ean + " * " + linenum + " * " + prod + " * " + name + " * " + kog  + " * " + unit ); 
			         
	       	     }else{
	       	  
	       	     
			      
	             
	             if ( prod == null ){
	                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                 //break;
	                 prod = " ";
	            		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF RAUTAKESKO format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + kog + "\n\n");
	            		 doknr=reference;
	               
	               }
	               
	               
	                  double kogus =  0;
	                  try{
	               	   kogus =  Double.parseDouble(kog.replace(',', '.'));
	                  }catch (Exception e){
	               	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
	               	   System.out.println("ffffff" + kog);
	               	   kogus =  Double.parseDouble(kog);   
	                  }

	               
	                  
	                  orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",77," ",null,"",ean,null,true,null,null);
	                  orders.setInfo(reference);
	                  counter++;
	             	 
	                  if(counter == 30){
	               // 	  System.exit(0);
	                  }
	                  
	       	     } 
	        	}catch(Exception d){
	        		//e.printStackTrace();
	        	}

   	}
	        	
	   
	     addLog("Total Order Lines added:" + counter);
	     
	     br.close();
	     fr.close();
	  }

  
  
  public static int countMatches(String str, String sub) {
	    if (isEmpty(str) || isEmpty(sub)) {
	        return 0;
	    }
	    int count = 0;
	    int idx = 0;
	    while ((idx = str.indexOf(sub, idx)) != -1) {
	        count++;
	        idx += sub.length();
	    }
	    return count;
	}
  
  private static boolean isEmpty(String str) {
	if (str == null || str.length() == 0){
		return true;
	}
	return false;
}



private void loadPDFStockmannOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
	  	 
	  	String fileName = f.getAbsolutePath();
	  	 File fil = new File ("C:/x/pdftemp.txt");
	  	
	     PDDocument document = null;
	     try
	     {
	         document = PDDocument.load( f.getAbsolutePath() );
	         if( document.isEncrypted() )
	         {
	             try
	             {
	                 document.decrypt( "" );
	             }
	             catch( InvalidPasswordException e )
	             {
	            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
	                 System.err.println( "Error: Document is encrypted with a password." );
	                 System.exit( 1 );
	             }
	         }
	         
	         PDFTextStripper ss = new PDFTextStripper();
	        
	             String aa =  ss.getText(document);
	             
	  
	         // System.out.println(aa);
	            
	             if(fil.exists()){
	             	fil.delete();
	             }else{
	            	 File dir = new File("C:/x/");
	            	 if(!dir.exists()){
	            		 dir.mkdir();
	            	 }
	            	 }
	            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
	            fos.write(aa.getBytes());
	            fos.close();
	          

	        
	     } finally{
	         if( document != null ) {
	             document.close();
	         }
	     }
	     
	     FileReader fr = new FileReader(fil);
	     BufferedReader br = new BufferedReader(fr);
	     
	     
	     String line = null;
	     int counter = 0;
	     String tellijaKood = null;
	      firma = "";
	      String datelong = "";
	     String tellijaRegnr = null;
	     String kood = null;
	     String reference = null;
	     System.out.println("stock ");
	     while((line = br.readLine()) != null){
	    	 if(line.indexOf("ORDER NR:") != -1){
	    		 int ind = line.indexOf("DATE");
	    		
		    	  reference = line.substring(10,ind).trim();
		    	 // System.out.println("stock " + ind);
		 	  	 String datum = line.substring(ind + 5).trim();
		    	 datelong = getCalHTM(datum); 
		    
		    	break;	 
	    	 }
	     }

		     try{
		    		kood = "stockmann";		    		 
			 }catch(Exception e ){
		    	 e.printStackTrace();
		     }
	    	 
	    	 System.out.println("Dates1 "  + datelong + " "  + reference + " " + firma + " " + line);
	
	    
	 try{
	        firma = driver.identifyCustomer(kood,null);
	       //  System.out.println("'"+firma+"'");
	      } catch (SQLException e) {
	      // e.printStackTrace();	
	       } 
     
	       if (firma == null || firma.trim().length() == 0){
	         addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	         troubles.append("Customer CANNOT BE RESOLVED PDF STOCKMANN format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
	  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
	         doknr=reference;
	       }
	     

		     while( (line = br.readLine()) != null ){ 
		    	 if (line.indexOf("Discount") != -1){
		    		 break;
		    	 }
		     }
		     Calendar cc = Calendar.getInstance();
		    String year = (cc.get(Calendar.YEAR)+ "").substring(0,3);
	 
	     while( (line = br.readLine()) != null){  
	    	    
	    	 String ean  = line;
	    	System.out.println("line Zero " + line);
	    		if(line.trim().length() == 10 && line.indexOf(year) != -1){
      		 	 while( (line = br.readLine()) != null ){ 
      		    	 if (line.startsWith("Discount")){
      		    		 break;
      		    	 }
      		     }
      			  line = br.readLine();
      			  ean  = line;
      		   // System.out.println("Sees1 " + line);
      		}
	    		if(line == null){
	    			break;
	    		}
	    		
	    		try{
	    			ean = line.substring(0,line.indexOf(" "));
		    		if(ean.length() >13){
		    			ean = ean.substring(0,13);
		    		}
		    		
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
	    		
	    		
			    
	
	        	try{
	                if(line.trim().length() == 0){
	                	line = br.readLine();
	                }
	        		line = line.replaceAll(" ", " |");
		        	line = line.replaceAll("  ", " ");
		       
		        	  int count = countMatches(line ,"|" );
		        	//  System.out.println("Alla 10 " + count + " " + line);
		        	    if  (count < 10){
		        	    	
		        	    	
		        	    	if(line.trim().length() == 1){
		        //	    		line = br.readLine();
		        	    	}
		        	    	String line1 =  br.readLine();
		        	    	String line2 = br.readLine();
		        	    	line = line +" |"+ (line1+ " " +line2).replaceAll(" ", " |").replaceAll("  ", " ");
		        	    	br.mark(500);
		        	    	String line3 = br.readLine();
		        	    	
		        	    	if(line3.length() > 2){
		        	    		line = line + " |" + line3.replaceAll(" ", " |").replaceAll("  ", " ");
		        	    		br.mark(500);
		        	    	} else {
		        	    		br.reset();
		        	    	}
		        	    	
		        	    	String line4 = br.readLine();
		        	    	if(line4.length() > 2){
	        	    			line = line + " |" + line4.replaceAll(" ", " |").replaceAll("  ", " ");
	        	    		}else{
	        	    			br.reset();
	        	    		}
		        	    	String line5 = br.readLine();
		        	    	if(line5.length() > 2){
	        	    			line = line + " |" + line5.replaceAll(" ", " |").replaceAll("  ", " ");
	        	    		}else{
	        	    			br.reset();
	        	    		}
		        	    }
		        	    
		       	 System.out.println("liin1 " +line);
		       	 	br.mark(500);
		       		String line3 = br.readLine();
        	    	if(line3.trim().length() != 0){
        	    		br.reset();
        	    	}
		       		 
	        	String prod = null;
	        	  
	        	    int ind = line.lastIndexOf("|"); 
	        	    line = line.substring(0,ind);
	        	    ind = line.lastIndexOf("|"); 
	        	    line = line.substring(0,ind);
	        	    ind = line.lastIndexOf("|");
	        	    String kog = line.substring(ind+1); 
                	  System.out.println("kogus " + kog);
	        	    line = line.substring(0,ind);
	        	    ind = line.lastIndexOf("|"); 
	        	   String unit = line.substring(ind+1);
	        	   line = line.substring(0,line.lastIndexOf(unit)-1);
                  	   ind = line.lastIndexOf("|");
                  	   
	        	   line = line.substring(0,ind+1);
	        	   ind = line.lastIndexOf("|"); 
	        	     String kastis = line.substring(ind+1);
	        	     System.out.println("kastis " + kastis);
	        	     line = line.substring(0,line.lastIndexOf(kastis)-1);
	     		
	             String name = line.substring(ean.length() + 1,ind).replace('|', ' ').replace('|', ' ').replace('|', ' ').trim();
	             name = name.replaceAll("  ", " ");
	             
	             if(ean.length() >13){
	            	 ean = ean.substring(0,13);
	             }
	             System.out.println(counter + ". ean : " + ean + " * " + prod + " * " + name + " * " + kog  + " " + kastis); 
	             System.out.println(counter + ". ean : " + ean );
	         	
	             try{
	            	  prod = driver.identifyProduct(ean,null);
	              }catch (Exception e){
	            	 
	             }
                 
	             if ( prod == null ){
	                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                 //break;
	                 prod = " ";
	            		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF STOCKMANN format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + kog + "\n\n");
	            		 doknr=reference;
	               
	               }
	               
	               
	                  double kogus =  0;
	                  double kasti = 1; 
	                  try{
	               	   kogus =  Double.parseDouble(kog.replace(',', '.'));
	                  }catch (Exception e){
	               	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
	               	  // System.out.println("ffffff" + kog);
	               	   kogus =  Double.parseDouble(kog);   
	                  }

	              /*    try{
	                	  kasti =  Double.parseDouble(kastis.replace(',', '.'));
		                  }catch (Exception e){
		                	  kastis = kastis.substring(0,kastis.indexOf(".")) +kastis.substring(kastis.indexOf(".")+1) ;
		               	   System.out.println("ffffff" + kog);
		                	  kasti =  Double.parseDouble(kastis);   
		                  }
*/
	                  
	                  orders.add(firma, prod, name ,kogus * kasti, reference, null, 0, 0,datelong,"0",0,0,false,"",92," ",null,"",ean,null,true,null,null);
	                
	                  orders.setInfo(reference);
	                  counter++;
	             	 
	        		
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}

   	
	        	} 
	   
	     addLog("Total Order Lines added:" + counter);
	     
	     br.close();
	     fr.close();
	  }
	  

  

  private void loadPDFTelemaOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
	  	 
	  	String fileName = f.getAbsolutePath();
	  	 File fil = new File ("C:/x/pdftemp.txt");
	  	
	     PDDocument document = null;
	     try
	     {
	         document = PDDocument.load( f.getAbsolutePath() );
	         if( document.isEncrypted() )
	         {
	             try
	             {
	                 document.decrypt( "" );
	             }
	             catch( InvalidPasswordException e )
	             {
	            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
	                 System.err.println( "Error: Document is encrypted with a password." );
	                 System.exit( 1 );
	             }
	         }
	         
	         PDFTextStripper ss = new PDFTextStripper();
	        
	             String aa =  ss.getText(document);
	             
	  
	         // System.out.println(aa);
	            
	             if(fil.exists()){
	             	fil.delete();
	             }else{
	            	 File dir = new File("C:/x/");
	            	 if(!dir.exists()){
	            		 dir.mkdir();
	            	 }
	            	 }
	            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
	            fos.write(aa.getBytes());
	            fos.close();
	          

	        
	     } finally{
	         if( document != null ) {
	             document.close();
	         }
	     }
	     
	     FileReader fr = new FileReader(fil);
	     BufferedReader br = new BufferedReader(fr);
	     
	     
	     String line = null;
	     int counter = 0;
	     String tellijaKood = null;
	      firma = "";
	      String datelong = "";
	     String tellijaRegnr = null;
	     String kood = null;
	     
	     String reference = br.readLine();
	     if(reference.startsWith("Order")) {
	    	 reference = reference.substring(10).trim();
	     }else {
	    	 reference = reference.substring(13).trim();
	     }
	    
	     System.out.println("ref " + reference);

	  
	     
	     while( (line = br.readLine()) != null ){ 
	    	 if (line.indexOf("Tellitud") != -1 || line.indexOf("ordered") != -1 ){
	    		 break;
	    	 }
	     System.out.println(line);
	     if(line.startsWith("Tellimuse") || line.startsWith("Order date")){
	    	 int dd = 0;
	    	 if(line.startsWith("Order date")) {
	    		 dd = -7;
	    	 }
	    	 String datum = line.substring(20 + dd).trim();
	    	 datelong = getCalHTM(datum);  
	    	 System.out.println("Dates1 " + datum + " " + datelong + " "  + reference + " " + firma + " " + line);
	     }
	     try{
	    	 if(line.startsWith("Tarnek") || line.startsWith("Delivery loc") ){
	    	int dd = 0 ;
	    	if(line.startsWith("Delivery")) {
	    		dd = 8;
	    	}
	    		 
	    		kood = line.substring(11 + dd).trim().replaceAll(" ", ""); 
	    		 System.out.println("firma " + kood);
		    	
		     }	 
	     }catch(Exception e ){
	    	 e.printStackTrace();
	     }
	        
	     
	        }
	    
	     
	     
	  try{

	        firma = driver.identifyCustomer(kood,null);
	         System.out.println("'"+firma+"'");
	      } catch (SQLException e) {
	      // e.printStackTrace();	
	       } 
     
	       if (firma == null || firma.trim().length() == 0){
	         addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	         troubles.append("Customer CANNOT BE RESOLVED PDF TELEMA format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
	  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
	         doknr=reference;
	       }
	     
	  
	    
	 
	     while( (line = br.readLine()) != null){  
	    	 
		     /*Order no: 4506835195
	    	
	    	 RN *TIN Supplier productcode Product description
	    	 4uantity
	    	 ordered Unit Price Amount
	    	 00010 3046920028721 172521 Lindt EXCELLENCE tume šokolaad 99% kakaosisaldusega50 g 18 tükk    
	    	 00020 8851978801036 321012 THAI CHOICE magus tšillikaste 200ml 12 tükk    
	    	 00030 8851978804075 321282 THAI CHOICE väherasvane kookospiim purk 400 ml 72 tükk    
	    	 00040 8851978803870 321284 THAI CHOICE kookoskreem 400ml 48 tükk    
	    	 00050 8851978814067 321219 THAI CHOICE roheline karripasta 110g 24 tükk    
	    	 1/1*/
	        	line = line.replaceAll(" ", " |");
	        	line = line.replaceAll("  ", " ");
	        	 System.out.println("liin3 " + line);
	        	try{
	        		
	        		 //System.out.println("liin1 " +line);
	        		 if(line.indexOf("Baashind") == -1 ){
	        		
	        		 try{
	        			 line = line.substring(0,line.indexOf("| | | | |"));
	        		 }catch(Exception e){
	        			// line = line.substring(0,line.indexOf("|  |  |"));    �/// |8410702015646 |� |PENASOL |TINTO |6 |tk
	        		 }
	        	
	        	StringTokenizer st = new StringTokenizer(line,"|");
	        	 System.out.println("liin2 " + line);
	             String prod = " ";
	             String ean  = null;
	         
	         		
	        		st.nextToken();
	        	    ean  = st.nextToken().trim();
	        	   
	        	    line = line.substring(0,line.lastIndexOf("|"));
	           	    line = line.substring(0,line.lastIndexOf("|"));
	           	    int ind = line.lastIndexOf("|"); 
	           	   String unit = line.substring(ind+1);
	           	   line = line.substring(0,line.lastIndexOf(unit)-1);
	           	 
	           	   ind = line.lastIndexOf("|"); 
	           	     String kog = line.substring(ind+1);
	           	     String abi =  line.substring(0,line.lastIndexOf(kog)-1);
	           	  
	           	    // System.out.println("abi " + unit + " " + kog  +" "+ abi);
	           	     ind = abi.lastIndexOf("|"); 
	           	     String kog2 = abi.substring(ind+1);
	           	  //System.out.println("kog2 " + kog2);
	           	  String kogabi = kog;
	            
	        if(Converter.isNumeric(kog2.trim())) {
	            	kogabi = kog2.trim() + kog.trim();
	            }
	        	     int aa = 0;
	        	     try {
	        	    	 aa = Integer.parseInt(kogabi.trim());
	        	     }catch(Exception r){
	        	    	 
	        	     }
	         	 System.out.println("XXXX ean " + ean + " " + unit + " " + aa + " " + kog );
	             String name = line.substring(line.indexOf(ean) + 13,ind).replace('|', ' ').replace('|', ' ').replace('|', ' ').trim();
	             name = name.replaceAll("  ", " ");
	             name = name.replace('\'', ' ');
	           //  System.out.println(counter + ". ean : " + ean + " * " + prod + " * " + name + " * " + kog ); 
	             try{
	            	  prod = driver.identifyProduct(ean,null);
	              }catch (Exception e){
	            	 
	             }
	             
	        
	        	 
	             // System.out.println(counter + " xx. ean : " + ean + " * " + prod + " * " + name + " * " + kog ); 
		           
	             
	             if (prod == null ){
	                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                 //break;
	                 prod = " ";
	            		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF TELEMA format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + aa + "\n\n");
	            		 doknr=reference;
	               
	               }
	               
	               
	                  double kogus =  0;
	                  try{
	               	   kogus =  aa;// siit v�tsin aa+ maha MG beauty case
	                  }catch (Exception e){
	               	   kog = aa + kog.substring(0,kog.indexOf(".")) + kog.substring(kog.indexOf(".")+1) ;
	               	  // System.out.println("ffffff" + kog);
	               	   kogus =  Double.parseDouble(kog);   
	                  }
	                  System.out.println(firma+"'"+prod+"'"+kogus);
	              
	                  
	                  orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",92," ",null,"",ean,null,true,null,null);
	                
	                  orders.setInfo(reference);
	                  counter++;
	             	 
	        		 }
	        	 }catch(Exception e){
	        		//e.printStackTrace();
	        	 }
	        	} 
	   
	     addLog("Total Order Lines added:" + counter);
	     
	     br.close();
	     fr.close();
	  }
	  


  
  
  private void loadRTFOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
  	 
  	String fileName = f.getAbsolutePath();
  	 File fil = new File ("C:/x/rtftemp.txt");
  	
  	 FileInputStream stream = null;
  	try {
		stream = new FileInputStream(f);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} 
  	  RTFEditorKit kit = new RTFEditorKit();  
  	javax.swing.text.Document doc = kit.createDefaultDocument();  
      try {
			kit.read(stream, doc, 0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
		e.printStackTrace();
		} 
		
      String plainText = null;
		try {
			plainText = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}  
		stream.close();
   if(fil.exists()){
             	fil.delete();
             }else{
            	 File dir = new File("C:/x/");
            	 if(!dir.exists()){
            		 dir.mkdir();
            	 }
            	 }
            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
            fos.write(plainText.getBytes());
            fos.close();

     
            
   	     FileReader fr = new FileReader(fil);
   	     BufferedReader br = new BufferedReader(fr);
   	     
   	     
   	     String line = null;
   	     int counter = 0;
   	     String tellijaKood = null;
   	      firma = "";
   	      String datelong = "";
   	     String tellijaRegnr = null;
   	     String kood = null;
   	  String reference = null;
   	     
   	     while( (line = br.readLine()) != null ){
   	    	 if (line.indexOf("Order") != -1 || line.startsWith("Pas")){
   	    		 break;
   	    	 }
   	     }
   	     
   	    	 
   	    	
   	      reference = br.readLine();
   	      br.readLine();
   	    
   	    	 String datum = br.readLine();
   	    	 datelong = getCalTel(datum);  
   	    	 System.out.println("Dates1 " + datum + " " + datelong + " "  + reference + " " + firma + " " + line);
   	    
   	    	 
   	    	 while( (line = br.readLine()) != null ){
   	   	    	 if (line.indexOf("Delivery address") != -1 || line.startsWith("Pieg")){
   	   	    		 break;
   	   	    	 }
   	   	     }
   	   	    
   	   
   	    	 try{
   	    		 line = br.readLine();
   	    	    	kood = Converter.cleanLitLatEst(line.substring(0,line.indexOf(",")).trim()); 
   	    		
   	     }catch(Exception e ){
   	    	 e.printStackTrace();
   	     }
   	 	
   	  
   	     
   	        
   	    
   	     try{
             firma = driver.identifyCustomer(kood,null);
   	         } catch (SQLException e) {
   	           e.printStackTrace();	
   	       } 
          
   	       if (firma == null || firma.trim().length() == 0){
   	         addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
   	         troubles.append("Customer CANNOT BE RESOLVED RTF DEPO format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
   	  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
   	         doknr=reference;
   	       }
   	     
        	 while( (line = br.readLine()) != null ){
  	   	    	 if (line.indexOf("Amount excl. sales tax") != -1 || line.startsWith("Summa bez PVN")){
  	   	    		 break;
  	   	    	 }
  	   	     }
   	    
   	
   	     while( (line = br.readLine()) != null && !line.equals("Post Mobile")){  
   	        	try{
   	        		
   	        
   	          System.out.println("liin1 " +line);
   	       if(line.trim().equals("") ){
   	        			br.readLine();
   	        	 String prod = " ";
   	             String ean  =  br.readLine();
   	          if((!ean.equals("Total") || !ean.equals("Kopsumma")) && !ean.equals("Post Mobile")){
   	        	  
   	          
   	             
   	          String name = br.readLine();
   	                 br.readLine();
   	           String kog =  br.readLine();
   	           String unit = br.readLine();
   	       
   	        	    

 	        	 
   	             try{
   	            	 prod = driver.identifyProduct(ean,null);
   	              }catch (Exception e){
   	            	 
   	             }
   	             
   	        
   	        	 
   	             
   	             
   	             if (prod == null ){
   	                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
   	                 //break;
   	                 prod = " ";
   	            		 troubles.append("PRODUCT CANNOT BE RESOLVED RTP DEPO format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
   	            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + kog + "\n\n");
   	            		 doknr=reference;
   	               
   	               }
   	               
   	               
   	                  double kogus =  0;
   	                  try{
   	               	   kogus =  Double.parseDouble(kog);
   	                  }catch (Exception e){
   	               	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
   	               	   kogus =  Double.parseDouble(kog);   
   	                  }
   	                  System.out.println(firma+"'"+prod+"'"+kogus);
   	    
   	                  orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",84," ",null,"",ean,null,true,null,null);  	                
   	                  orders.setInfo(reference);
   	                  counter++;
   	             	 
   	        		 } }
   	        	}catch(Exception e){
   	        		//e.printStackTrace();
   	        	}
   	        	} 
   	   
   	     addLog("Total Order Lines added:" + counter);
   	     
   	     br.close();
   	     fr.close();
 
     
  }
  
  
  
  
  
  
  private void loadPDFOrder(Orders orders, File f,StringBuffer troubles ) throws Exception{
  	 
  	String fileName = f.getAbsolutePath();
  	 File fil = new File ("C:/x/pdftemp.txt");
  	
     PDDocument document = null;
     try
     {
         document = PDDocument.load( f.getAbsolutePath() );
         if( document.isEncrypted() )
         {
             try
             {
                 document.decrypt( "" );
             }
             catch( InvalidPasswordException e )
             {
            	 troubles.append("Error: PDF " + f.getAbsolutePath()+ " Document is encrypted with a password.\n\n");
                 System.err.println( "Error: Document is encrypted with a password." );
                 System.exit( 1 );
             }
         }
         
         PDFTextStripper ss = new PDFTextStripper();
        
             String aa =  ss.getText(document);
             
  
         // System.out.println(aa);
            
             if(fil.exists()){
             	fil.delete();
             }else{
            	 File dir = new File("C:/x/");
            	 if(!dir.exists()){
            		 dir.mkdir();
            	 }
            	 }
            FileOutputStream fos = new FileOutputStream(fil.getAbsolutePath());
            fos.write(aa.getBytes());
            fos.close();
          

        
     } finally{
         if( document != null ) {
             document.close();
         }
     }
     
     //System.exit(0);
     
     FileReader fr = new FileReader(fil);
     BufferedReader br = new BufferedReader(fr);
     
     FileReader frlin = new FileReader(fil);
     BufferedReader brlin = new BufferedReader(frlin);

     FileReader frch = new FileReader(fil);
     BufferedReader brch = new BufferedReader(frch);
     
     String line = null;
     int counter = 0;
     String tellijaKood = null;
      firma = "";
      String datelong = "";
     String tellijaRegnr = null;
     String kood = null;
     String reference = null;
     String check = null;
     boolean rimi = false;
     while ((check = brch.readLine()) != null){
    	 if(check.indexOf("Rimi Lietuva")!=-1){
    		 rimi = true;
    		 System.out.println("Rimi variant");
    		// return;
    	 }
     }
     brch.close();
     frch.close();
     
     if(rimi){//Rimi variant
    	 
    
     while( (line = br.readLine()) != null){        
            if (line.indexOf("Nr Artikulas") != -1){
              break;
            }
     }
       
     line = br.readLine();
     tellijaRegnr = line.substring(line.indexOf("s kodas:")+8).trim(); 
     tellijaKood = line.substring(0,line.indexOf("s kodas:")-5).trim();
     
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     
    
     
     reference = line.substring(line.indexOf("Nr.")+3).trim();
     
     String datum = br.readLine();
     
     datelong = this.getCalHTM(datum);
     
     System.out.println("Dates " + datum + " " + datelong);
     

     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     
     String deldate = line.substring(line.indexOf("Pristatymo data:")+17).trim().substring(0,10);
     if(deldate.trim().length()>9){
         datelong = this.getCalHTM(deldate); 
     }
     System.out.println("Dates2 " + deldate + " " + datelong);
     
     try{
       if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
       kood = tellijaRegnr ; } else {kood = tellijaKood;}
       // System.out.println("Kood: "+ kood);
       firma = driver.identifyCustomer(kood,"1111111");
       System.out.println("'"+firma+"'");
    } catch (SQLException e) {
    // e.printStackTrace();	
     }
     
     if (firma.trim().length() == 0){
       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
       troubles.append("Customer CANNOT BE RESOLVED PDF RIMI format \n" + "Dok Nr: " + doknr + " " + reference + reference + "\n"
				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + reference + "\n\n");
       doknr=reference;
     }
   
     
     while( (line = brlin.readLine()) != null){
      

    		//System.out.println("'J�udsime ridade juurde1'" + line);

      		System.out.println("'Len1' " + line.trim().length());
        	if(line.trim().length()>90  ){
       	
       		
       		String line2 = brlin.readLine();
       		brlin.mark(10000);
       		String line3 = brlin.readLine();
       		String line4 = null;
       	//	System.out.println("'J�udsime ridade juurde2'" + line2);
       	//	System.out.println("'J�udsime ridade juurde3'" + line3);
       		if(!line3.startsWith("EAN:")){
       			try{    			
       				brlin.reset();
       			}catch(Exception e){ e.printStackTrace();
       			brlin.reset();
       			// System.out.println("line 2 "  +line2);
        		// System.out.println("line 3 "  +line3);
       			 System.exit(0);
       			}	
       		}else {
       			brlin.mark(10000);
       			line4 = brlin.readLine();
       			if(!line4.startsWith("EAN:")){
           			try{    			
           				brlin.reset();
           			}catch(Exception e){ e.printStackTrace();
           			brlin.reset();
           			// System.out.println("line 2 "  +line2);
            		// System.out.println("line 3 "  +line3);
           			 System.exit(0);
           			}
           			}
       		}
       			
       			
       			
      // 	
       		
        	String ol = line;
         String prod = " ";
    
     //    System.out.println("line3  " + line3);

         String name  = ol.substring(ol.indexOf(" ")+14,ol.indexOf(" ")+60).trim();

        // System.out.println(line + "toode" + ol.indexOf(" ") + name);
             if(line2.substring(0,line2.indexOf(" ")+1).trim().equals("Nr")){
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	line2 = brlin.readLine();
             	//line2 = brlin.readLine();
             }
            
             if(line2.startsWith("Lapas")){
               	 line2 = brlin.readLine();
                }
                
            if( line2.startsWith("Tiek")){
               for (int i = 0 ;i<21;i++){
            	   line2= brlin.readLine();
               }
            	
            }
            
            
            if(line2.indexOf("jimo termi") != -1){
            	//System.out.println("11 " + line2);
             	 line2 = brlin.readLine();
  //           	System.out.println("22 " + line2);
              }
            
            
            if(line2.startsWith("Valiuta")){
              	 line2 = brlin.readLine();

//                 System.out.println("33 " + line2);
               }
            if(line2.indexOf("Numatomas pale") != -1 || line2.indexOf("sakymo numer") != -1){
             	 line2 = brlin.readLine();

//                System.out.println("33 " + line2);
              }
              
            if(line2.indexOf("sakymo numer") != -1){
            	 line2 = brlin.readLine();

             }
            if(line2.indexOf("Kiekis Kaina Suma") != -1){
           	 line2 = brlin.readLine();

            }
           
            
         String toode  = line2.substring(0,line2.indexOf(" ")+1).trim();
          
         // System.out.println("toode"+ toode);

     	 //System.out.println("line " + line);
     	 
         System.out.println("line2 " + line2);
     	 String ean  = null;
         try{

        	ean =  line2.substring(line2.indexOf("EAN: ")+5,line2.indexOf("EAN: ")+18).trim();
        	 if(ean.trim().indexOf(" ") != -1){
            	 ean = ean.substring(0,ean.trim().indexOf(" "));
             }
         }catch (Exception e){
        	e.printStackTrace(); 
         }
        
         String ean2  = null;
         try{
             ean2 = line2.substring(line2.indexOf("EAN: ")+24,line2.indexOf("EAN: ")+38).trim();
            //System.out.println("ean2 " + ean2);
             if(ean2.trim().indexOf(" ") != -1){
            	 ean2 = ean2.substring(0,ean2.trim().indexOf(" "));
             }
         }catch (Exception e){
        	 try{
        	  ean2 = line2.substring(line2.indexOf("EAN: ")+24,line2.indexOf("EAN: ")+36).trim();
              //System.out.println("ean22 " + ean2);
               if(ean2.trim().indexOf(" ") != -1){
              	 ean2 = ean2.substring(0,ean2.trim().indexOf(" "));
               }
        	 }catch (Exception ee){
        		 
        	 }
        	//e.printStackTrace();
         }
         
         
         System.out.println("ean" + ean + " * " + ean2 + "\n" + line3.startsWith("EAN"));
         String ean3 = null;
         String ean4 = null;
         String ean5 = null;
         String ean6 = null;
         
         if(line3.startsWith("EAN:")){
        	 try{
        	 ean3 = line3.substring(line3.indexOf("AN: ")+4,line3.indexOf("AN: ")+17).trim();
        	 }catch (Exception e){
        		 try{
                	 ean3 = line3.substring(line3.indexOf("AN: ")+4,line3.indexOf("AN: ")+15).trim();
                	 }catch (Exception eb){
                		 
                     }
             }
        	 try{
        	 ean4 = line3.substring(line3.indexOf("AN: ")+23,line3.indexOf("AN: ")+36).trim();
        	 }catch (Exception e){
             }
         }
         

         if(line4 != null && line4.startsWith("EAN:")){
        	// System.out.println("line4 " + line4);
        	 ean5 = line4.substring(line4.indexOf("AN: ")+4,line4.indexOf("AN: ")+17).trim();
        	 try{
        		 ean6= line4.substring(line4.indexOf("AN: ")+23,line4.indexOf("AN: ")+37).trim();
        	 }catch (Exception e){
            	 //System.exit(0);
             }
        	 
         }
         	
            System.out.println("ean rida"+ ean + " " + ean2 + " " + ean3 + " " + ean4 + " " + reference  + " " + ean5 + " " + ean6);
         
         	if(toode.equals("")){
         		toode = ean;
         	}
         	String unit  = "";
            String kog = "";
            try{
            	if(ean2 == null || ean2.length() == 0){
            		System.out.println("kogusyyy "+ line2 + " ||" +line2.indexOf(ean) + " "+ ean.length());
            		kog = line2.substring(line2.indexOf(ean) + ean.length()+1).trim();
            	}else{
            		kog = line2.substring(line2.lastIndexOf(ean2) + ean2.length()+1).trim();
            	}
            	
            	System.out.println("koguszzz "+kog );
            	
            	unit = kog.substring(kog.length()-3,kog.length());     
            	//kog = kog.replace(".", " ");
                kog = kog.substring(0,kog.indexOf(" ")).replace(',','.').trim();	
            }catch (Exception e){
            	//e.printStackTrace();
            	//System.out.println("�����" + line2);
            	//System.out.println("!!!!!" + line);
            	try{
            		 kog = line.substring(58,77).trim();
            		// kog = kog.replaceAll(".", " ");
            		  System.out.println("!!!!!!" + kog + " *  " + line);
            		 unit = kog.substring(kog.length()-3,kog.length());     
                     kog = kog.substring(0,kog.indexOf(" ")).replace(',','.').trim();	
            	}catch(Exception rr){
            		
            		try{
            			 kog = line.substring(54,77).trim();
                 		  System.out.println("!!!!!!" + kog + " *  " + line);
                 		 unit = kog.substring(kog.length()-3,kog.length());     
                          kog = kog.substring(0,kog.indexOf(" ")).replace(',','.').trim();
            		}catch(Exception rg){

                		rr.printStackTrace();
                		//System.exit(0);
            		}
            			
            		
            	}
              }
            
            
           System.out.println("kogus"+kog + "#"+ unit + " "+ ean + " " + ean2 + " " + ean3);

            	
         	
       
           try{
           prod = driver.identifyProduct(ean,null);
          // System.out.println(toode +"  "+ prod);
           if(prod == null){
        	   prod = driver.identifyProduct(ean2,null);
            //   System.out.println("ean2 : " + toode +"  "+ prod);
           }
           if(prod == null){
        	   prod = driver.identifyProduct(ean3,null);
              // System.out.println("ean3 : " + toode +"  "+ prod);
           }
           if(prod == null){
        	   prod = driver.identifyProduct(ean4,null);
              // System.out.println("ean4 : " + toode +"  "+ prod);
           }
           if(prod == null){
        	   prod = driver.identifyProduct(ean5,null);
               //System.out.println("eanToode : " + toode +"  "+ prod);
           }  
           if(prod == null){
        	   prod = driver.identifyProduct(ean6,null);
               //System.out.println("eanToode : " + toode +"  "+ prod);
           }
           if(prod == null){
        	   prod = driver.identifyProduct(ean,toode);
               //System.out.println("eanToode : " + toode +"  "+ prod);
           }
        } catch (SQLException e) {
        e.printStackTrace();	
        }

        try{
        	 if  (driver instanceof SkaiteEugImportDriver ){
        		  String   prod2 = driver.getKodirovka(ean, 89);  
    		       if(prod2 != null){
    		    	   prod = prod2;
    		       }
       	  }
 	          System.out.println(" kodirovka "+ prod + " ean " + ean);
 	       } catch (Exception e) {
 	       e.printStackTrace();	
 	       }
        
        System.out.println("L�PLIK PROD " + prod);
        if (prod == null ){
          addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
          //break;
          prod = " ";
          if(prod.trim().length() == 0 ){
     		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF RIMI format : \n" + "Dok Nr: " + doknr + " " + reference+ reference + "\n"
     				+ "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" + toode + " amount: " + kog + "\n\n");
     		// System.out.println("PRODUCT CANNOT BE RESOLVED PDF format : \n" 
     			//	 + "\t" + name +" EAN:" + ean + " Code:" + toode + " amuont: " + kog );
     		 doknr=reference;
        }
        }
        
        
           double kogus =  0;
           try{
        	   kogus =  Double.parseDouble(kog);
           }catch (Exception e){
        	   System.out.println("ffffff" + kog);
        	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
        	
        	   kogus =  Double.parseDouble(kog);   
           }
          // System.out.println(firma+"'"+prod+"'"+kogus);
           //System.out.println(toode +"  ean1:"+ean + " ean2:" + ean2 +  "  nimi" + name + "  "+ unit + "  " + kog);
           if(!datelong.equals("")){
           		//datelong = this.getCalHTM(datelong);
           }
           
           orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",89,unit,null,"",ean,null,true,null,null);
        
           orders.setInfo(reference);
           counter++;
         }
       }
     
 }else{ // RIvona Variant
	 
	    

    String datum = br.readLine();
    try{
    	 datelong = this.getCalLit(datum).getTime() +"";    	
    }catch(Exception e){
    	System.out.println("datum " + datum);
    	e.printStackTrace();
    	System.out.println("No valid PDF  order format returning...");
    	return;
    }
     line = br.readLine();
     line = br.readLine();
     reference = line.trim();
     line = br.readLine();
     datelong = this.getCalLit(line).getTime() +"";
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     line = br.readLine();
     //line = br.readLine();
     
     System.out.println("Dates1 " + datum + " " + datelong + " "  + reference + " " + firma + " " + line);
    
  
     
     while( (line = br.readLine()).indexOf("U�sakovas:")>-1){ 
    	 
        }
     
     if(line.trim().indexOf("EUGESTA") > -1){
    	 firma = "240KA3658";
        }
     
     System.out.println("Dates " + datum + " " + datelong + " "  + reference + " " + firma + " " + line);
    
 
     while( (line = br.readLine()) != null){        
         if (line.length() > 40 && line.indexOf("Viso :") == -1){
        	if(line.length()>12 && this.isNumeric(line.substring(0,12))){
        		
        	
        	 String ean  = null;
        	 String origean = null;
             try{
            	ean =  line.substring(0,12);
            	origean = ean;
            	System.out.println(ean);
            	EAN13CheckDigit ee = new EAN13CheckDigit();
            	ean = ean + ee.calculate(ean);
            	while(ean.startsWith("0")){
            		ean = ean.substring(1);
            	//	System.out.println(ean);
            	}
             }catch (Exception e){
            	 e.printStackTrace();
             }
             
             String prod = " ";
          
             try{
           	   prod = driver.identifyProduct(ean,null);
             }catch (Exception e){
            	 
             }

             try{
             if (prod == null ){
            	  prod = driver.identifyProduct(origean,null);
             }
              }catch (Exception e){
            	 
             }
              try{
            	  if  (driver instanceof SkaiteEugImportDriver ){
			         
			          String   prod2 = driver.getKodirovka(ean, 92);  
	     		       if(prod2 != null){
	     		    	   prod = prod2;
	     		       }
	        	  }
		          System.out.println(" kodirovka "+ prod + " ean " + ean);
		       } catch (Exception e) {
		       e.printStackTrace();	
		       }
             String name = line.substring(13,line.trim().lastIndexOf(" "));
             String subline = line.substring(0,line.indexOf("VNT")).trim();
             
             System.out.println(subline); 
             String kog = subline.substring(subline.trim().lastIndexOf(" "));
             System.out.println(counter + ". ean : " + ean + " | " + prod + " | " + name + " | " + kog ); 
        	 
             
             
             if (prod == null ){
                 addLog(parent.getString("noprodid") + "\n " + ean  + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
                 //break;
                 prod = " ";
            		 troubles.append("PRODUCT CANNOT BE RESOLVED PDF RIVONA format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
            				 + "\tLineNum: "  +  (counter+1) + " "  + name +" EAN:" + ean + " Code:" +   " amount: " + kog + "\n\n");
            		 doknr=reference;
               
               }
               
               
                  double kogus =  0;
                  try{
               	   kogus =  Double.parseDouble(kog);
                  }catch (Exception e){
               	   kog = kog.substring(0,kog.indexOf(".")) +kog.substring(kog.indexOf(".")+1) ;
               	  // System.out.println("ffffff" + kog);
               	   kogus =  Double.parseDouble(kog);   
                  }
                  System.out.println(firma+"'"+prod+"'"+kogus);
              
                  
                  orders.add(firma, prod, name ,kogus, reference, null, 0, 0,datelong,"0",0,0,false,"",92," ",null,"",ean,null,true,null,null);
                  
              
                  
                  orders.setInfo(reference);
                  counter++;
        	 
        	} 
         }
      }
   }
     addLog("Total Order Lines added:" + counter);
     
     br.close();
     fr.close();	
  }
  
  

  
  
  
  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
	FileInputStream in = new FileInputStream(xmlFile);
	int bomKount = getBOMCount(xmlFile);
	if (bomKount > 0) {
	byte[] buf = new byte[bomKount];
	in.read(buf);
	}
	return in;
	}
  
  
  private int getBOMCount(File xmlFile) throws FileNotFoundException,
  IOException {
  DataInputStream din = new DataInputStream(new FileInputStream(xmlFile));
  int bomKount = 0;
  byte b = din.readByte();
  while (b < 0) {
  bomKount++;
  b = din.readByte();
  }
  din.close();
//   System.out.println("Skipping BOM, " + bomKount + " bytes");
  return bomKount;
  } 
  
  private void loadPrikePrint(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;
		Document doc = null;
		try{
			doc = db.parse(in,handler);
		}catch(SAXException a){
			System.out.println("Viga XML-is ");
			troubles.append("Viga XML-is \n" + a.toString());
			return;
		}
	   
	    Node root = getNode(doc, "SalesOrder");
	    Node pais = getNode(root, "OrderHeader");
	
	    String tellijaKood = null;
	    String agname = null;
	    String nameCu = "";
	    String reference = "" ;
	    String markused = "";
	    String aadress = "";
	    String route = "";
	    double totalOrdered = 0;
	    double totalDelivered = 0;
	    
	    try{
	    	reference = getNode(pais, "OrdNum").getFirstChild().getNodeValue() + "-" +  getNode(pais, "OrdType").getFirstChild().getNodeValue();
	    }catch(Exception d){
	    	
	    }
	    
	    try{
	    
	    	markused = getNode(pais, "Remark1").getFirstChild().getNodeValue();
	    	markused = markused + "|" + getNode(pais, "Remark2").getFirstChild().getNodeValue();
	    	
	    }catch(Exception d){
	    	
	    }
	    
	    try{
	    	route = getNode(pais, "DeliveryRoute").getFirstChild().getNodeValue();
	    }catch(Exception d){
	    	
	    }
	    try{
	    	aadress = getNode(getNode(pais, "DelivAddr"),"AddrLine1").getFirstChild().getNodeValue();
	    	aadress = aadress + "," + getNode(getNode(pais, "DelivAddr"),"AddrLine2").getFirstChild().getNodeValue();
	    	aadress = aadress + "|" + getNode(getNode(pais, "DelivAddr"),"AddrLine3").getFirstChild().getNodeValue();
	    	aadress = aadress + " " + getNode(getNode(pais, "DelivAddr"),"PostCode").getFirstChild().getNodeValue();
	    	//aadress = aadress + "|" + getNode(getNode(pais, "DelivAddr"),"TelNum").getFirstChild().getNodeValue();
	    	
	    }catch(Exception d){
	    	
	    }
	    
	    //<DelivAddr><AddrLine1>P�hja pst.17</AddrLine1><AddrLine2>Tallinn</AddrLine2><AddrLine3>Harjumaa</AddrLine3><CountryCode>EE</CountryCode><PostCode>10414</PostCode><TelNum>6412000</TelNum>
     	   nameCu = getNode(pais, "CustCodeDelivName").getFirstChild().getNodeValue();	
	       tellijaKood = getNode(pais, "CustCodeDeliv").getFirstChild().getNodeValue();
	
	    
	    firma = "";
	    int counter = 0;
	    String datelong = "";
	    String shiptime= "";
	    boolean halve = false;
	    try{
	    	datelong = parseDate(getNode(pais, "DelivDate").getFirstChild().getNodeValue()).getTime() + "";
	  	  	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    try{
	    	agname = getNode(pais, "SalesManName").getFirstChild().getNodeValue() ;
	  	  	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    try{
	    	shiptime = getNode(pais, "OrdDate").getFirstChild().getNodeValue();
	  	  	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    //  <OrdDate>2011-06-13</OrdDate><DelivDate>2011-06-13</DelivDate>

	    double hetkeKogus = 0;
	    
	    	Node read = getNode(root, "OrderLineList");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("OrderLine")){
	        	  
	        	  
	         String  toode = getNode(rida,"LineNum").getFirstChild().getNodeValue() + "  " +  
	                         getNode(rida,"StockCode").getFirstChild().getNodeValue();
	         String parim = getNode(rida,"MaxProductRealizationDate").getFirstChild().getNodeValue();
	         if(parim.indexOf("1900") != -1){
	        	 parim =  "";
	         }
             String ean =    getNode(rida,"EANCode").getFirstChild().getNodeValue();
             double  price = Double.parseDouble(getNode(rida, "UnitPrice").getFirstChild().getNodeValue()) ;
             double  kogusAll = Double.parseDouble(getNode(rida, "QtyOrdered").getFirstChild().getNodeValue()) ;
          	  totalOrdered = totalOrdered + kogusAll;
             	hetkeKogus = kogusAll;
          
	     //	loeme kokku palju on sellel real asja kokku et tada kas on puudu miskit
             	  try{
              	Node delivalg = getNode(rida, "DelivLineList");
	    	    
 	    	   for (int t = 0; t < delivalg.getChildNodes().getLength(); t++){
 	    		    Node del = delivalg.getChildNodes().item(t);
		 	        
		 	          if (del.getNodeName().equalsIgnoreCase("DelivLine")){
		 	        	
		 	        		 hetkeKogus = hetkeKogus -  Double.parseDouble(getNode(del,"QtyDeliv").getFirstChild().getNodeValue()) ; 
		 	        	
		 	        		 
		 	          }
		 	        	
 	    	   }
             	  }catch(Exception e){ 	        		  
 	        	  }
 	    	  System.out.println("Hetke kogus p�rast " + hetkeKogus);  
 		 	        		
             	
	          	String prodName ="";
	          	String unit = " ";
	          	try{
	              	prodName =            getNode(rida,"StockCodeDesc1").getFirstChild().getNodeValue();
	              	prodName = prodName + "| " + getNode(rida,"StockCodeDesc2").getFirstChild().getNodeValue();
	                
	          	}catch(Exception e ){
	          		
	          	}          	
	        	try{
	              	unit =getNode(rida,"UnitName").getFirstChild().getNodeValue(); 
	              	if(unit.length() > 3){
	              		unit = unit.substring(0,3);
	              	}
	          	}catch(Exception e ){        		
	          	}
	          	
	          	
	    	    try{
	    	    	Node deliv = getNode(rida, "DelivLineList");
	    	    
	    	    	   for (int t = 0; t < deliv.getChildNodes().getLength(); t++){

	    		 	          Node del = deliv.getChildNodes().item(t);
	    		 	          System.out.println(del.getNodeName() );
	    		 	          if (del.getNodeName().equalsIgnoreCase("DelivLine")){
	    		 	        	  try{
	    		 	        		  double  kog =   Double.parseDouble(getNode(del,"QtyDeliv").getFirstChild().getNodeValue()) ;
	    		 	        		 totalDelivered = totalDelivered + kog;
	    		 		 	         String partii = "";
	    		 		 	         try{
	    		 		 	        	 partii = getNode(del, "SupplierBatchID").getFirstChild().getNodeValue();
	    		 		 	        	 if(partii.length()>20){
	    		 		 	        		 partii = partii.substring(0,20);
	    		 		 	        	 }
	    		 		 	         }catch(Exception e){
	    		 		 	        	 
	    		 		 	         }
	    		 		 	         
	    		 		 	       String bestbefore = "";
	    		 		 	         try{
	    		 		 	        	bestbefore = "|" + getNode(del,"BestBefore").getFirstChild().getNodeValue();
	    		 		 	        	if(!parim.equals("")){
	    		 		 	        		bestbefore = bestbefore +  "|" + parim ;
	    		 		 	        	}
	    		 		 	         }catch(Exception e){
	    		 		 	        	 
	    		 		 	         }
	    		 		 	        	
	    		 	        		  
	    		 		 	       String location = "";
	    		 		 	         try{
	    		 		 	        	location = "|" + getNode(del,"BinCode").getFirstChild().getNodeValue();
	    		 		 	        	if(hetkeKogus != 0 ){
	    		 		 	        		location = location + "   Tell " +  (int)kogusAll +" \\ Tarn " + (int)(kogusAll - hetkeKogus) ;
	    		 		 	        	   halve = true;
	    		 		 	        	}
	    		 		 	        	
	    		 		 	         }catch(Exception e){
	    		 		 	        	 
	    		 		 	         }
	    		 		 	        	
	    		 		 	         if(location.equals("")){
	    		 		 	        	 location = "|ZZZ�KAUPA EI OLE !!!";
	    		 		 	        
	    		 		 	         }
	    		 		 	       if(prodName.toLowerCase().startsWith("tagatis") ){
	    		 		 	        	 location = "|ZZZ";
	    		 		 	        	}
	    orders.add(tellijaKood, toode , prodName + bestbefore , kog ,reference, null, 0, 0,datelong,nameCu,price,0,false,agname,100
	    		,unit + location,agname,partii,ean,shiptime,true,null,null);
	    counter++;
	    	System.out.println(" Rea info " +  tellijaKood + "X" + toode + "X" + prodName + bestbefore+ "X" + reference + "X" + partii+ "X" +ean+ "X" +shiptime + "X" + location);	 	   	           	  
	    				 	    	 
	    		 	        	  }catch(Exception rr){
	    		 	        		  rr.printStackTrace();
	    		 	        	  } 	    		 	        	  
	    		 	          }   
	    	    	   }
	    	    	
	    	    	 }catch(Exception e){	
	    	    		 halve = true;
	    	    		  orders.add(tellijaKood, toode , prodName  , kogusAll ,reference, null, 0, 0,datelong,nameCu,price,0,false,
	    	    				  agname,100,unit + "|ZZZ�KAUPA EI OLE !!!",agname,"!!!!!",ean,shiptime,true,null,null);
	    	    		    counter++;
	    	    		 e.printStackTrace();
	    	    }
	          	
	          
	            orders.setInfo(markused);
	            orders.setCurrency(agname);
	            orders.setTeostaja(aadress);
	            orders.setShipMode(route);
	            orders.setCompName(nameCu);
	    
	          }	
	          }
	        
	        System.out.println("1. " + totalOrdered + " 2. " + totalDelivered);
	        orders.setSumma(totalOrdered);
	        orders.setKmsumma(totalDelivered);
	        if(halve){
	        	orders.setShipTime(shiptime + "|" + ((int)totalOrdered) + "/" + ((int)totalDelivered));
	        }
	       // orders.setShipTime(shiptime + "|" + ((int)totalOrdered) + "/" + ((int)totalDelivered));
	         
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       }

  
  
  
  private void loadXMLEDISOFTOrder(Orders orders, File f,StringBuffer troubles) {
  
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	try {
		db = dbf.newDocumentBuilder();
	
//	System.out.println(f.getAbsolutePath());
	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
	
    Document doc = db.parse(in,handler);
    
    Node root = getNode(doc, "Document-Order");
  
  //  System.out.println("1111 " + getNode(getNode(root, "Order-Parties"), "Buyer"));
    
    //System.out.println("2222 " + getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
  // System.out.println("root " + root);
    
    String fileName = f.getName();
   
    String tellijaKood = null;
    String tarnekell = null;
    
   
    try{
    	tellijaKood =  getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier").getFirstChild().getNodeValue();
    }catch (Exception e){
    	
    	try{

        	tellijaKood = getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue();	
    	}catch (Exception ee){
    		try{
    		 	
    			
    			tellijaKood = getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"CodeByBuyer").getFirstChild().getNodeValue();
    	    			
    		}catch(Exception rr){
    			
    		}
       }
    }
 
    if(tellijaKood != null && (tellijaKood.equals("-") || tellijaKood.equals("00") || tellijaKood.equals("10263574")|| 
    		tellijaKood.equals("3090"))){
    try{
    	 tellijaKood = getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue();
    }catch(Exception e){
    	e.printStackTrace();
    }
   
    }
    
    
    
    String tellijaILN = null;
    try{
    	tellijaILN =  getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"ILN").getFirstChild().getNodeValue();
    }catch (Exception e){
    	
    }
    

    String deliveryILN = null;
    try{
    	deliveryILN =  getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"ILN").getFirstChild().getNodeValue();
    }catch (Exception e){
    	
    }
  
    
    System.out.println("delivery ILN " + deliveryILN);
    String tellijaNimekood = null;
    try{
    	tellijaNimekood =  getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"Name").getFirstChild().getNodeValue();
    }catch (Exception e){
    	
    }
    
    Vector conCod = null;
   
    String name = " ";
    firma = "";
    String prod = null;
    String toode = null;
    String regnr = null;
    int counter = 0;
    String datelong = "";
    String reference = "";
    String doctype  = "";
    String saatja = null;
    try{
    	datelong = parseDate(getNode(getNode(root, "Order-Header"),"ExpectedDeliveryDate").getFirstChild().getNodeValue()).getTime()+"";	
    }catch(Exception w){
	 	
	 }
    if(datelong.equals("0")){
    	datelong = new Date().getTime() + "";
    }
    	  try{
    	 	   tarnekell = getNode(getNode(root, "Order-Header"), "ExpectedDeliveryTime").getFirstChild().getNodeValue();
    			    System.out.println("kell " + tarnekell+ "'");   			    	
    	 }catch(Exception w){
    	 	
    	 }
    	
    	try{
        	doctype = getNode(getNode(root, "Order-Header"),"DocumentFunctionCode").getFirstChild().getNodeValue();
        }catch(Exception e){
        	
        }
        
        
        try{
            firma = driver.identifyCustomer(deliveryILN,regnr);
            System.out.println("firmakood " +firma + "|" + deliveryILN + "|" + regnr);
            conCod = getCustConvCodes(firma);
          } catch (SQLException es) {
           
          }
        
        
        try{
        	if(((firma == null || firma.equals("") ) && tellijaKood != null)){
        		 firma = driver.identifyCustomer(tellijaKood,regnr);
                 conCod = getCustConvCodes(firma);            
                 System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
        	}
           
           // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
          } catch (SQLException e) {
           
        	   }
          
          
          if((firma == null  || firma.equals(tellijaKood) )&& deliveryILN != null){
	       	   try{
	                  firma = driver.identifyCustomer(deliveryILN,regnr);
	                  conCod = getCustConvCodes(firma);
	                  
	                } catch (SQLException es) {
	                 
	                }  
	          }

          if(firma == null){
        	   try{
                   firma = driver.identifyCustomer(tellijaKood,regnr);
                   conCod = getCustConvCodes(firma);
                   System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
                 } catch (SQLException e) {
                  
               	   }  
          }
          
          
          if(firma == null){
       	   try{
                  firma = driver.identifyCustomer(tellijaILN,regnr);
                  conCod = getCustConvCodes(firma);
                } catch (SQLException e) {
                }  
          }
          if(firma == null){
       	   try{
                  firma = driver.identifyCustomer(tellijaNimekood.substring(0,tellijaNimekood.indexOf(" ")),regnr);
                  conCod = getCustConvCodes(firma);
                } catch (Exception e) {
                }  
          }
          
          if(firma == null){
          	   try{
                     firma = driver.identifyCustomer(getNode(getNode(getNode(root, "Order-Parties"),
                    		 "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue(),null);
                     conCod = getCustConvCodes(firma);
                     
                     //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
                    // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
                   } catch (Exception e) {
                    
                   }  
             }
     
          
          System.out.println("Firma Identified " + firma );
          
          if (firma == null || firma.trim().length() == 0){
              addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
              troubles.append("Customer CANNOT BE RESOLVED Edisoft XML format \n" + "Dok Nr: " + doknr + " " + reference+  " CustomerCode: "  + firma +" ImportCode: " + tellijaKood + " Custname: " + tellijaNimekood +  "\n"
       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
             System.out.println(troubles);
          }
    	
    	
    	
    	Node read = getNode(root, "Order-Lines");
    	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
          Node rida = read.getChildNodes().item(i);
          if (rida.getNodeName().equalsIgnoreCase("Line")){
            
     String partii = "";
           
           reference =  getNode(getNode(root, "Order-Header"), "OrderNumber").getFirstChild().getNodeValue();
           if(reference == null){ 	
           	reference = " ";
           }
           
          // System.out.println("reference " + reference + " " + 	getNode(getNode(rida, "Line-Item"),"ItemDescription").getFirstChild().getNodeValue());

           String ean = null;
           try{
        	   ean = getNode(getNode(rida, "Line-Item"),"EAN").getFirstChild().getNodeValue();
              }catch(Exception o ){
            	  try{
            		 	ean = getNode(getNode(rida, "Line-Item"),"BuyerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
            	  }catch(Exception s){
            		  
            	  }
             
              }
          if(ean != null && !ean.equals("2050000039348") && !ean.equals("2050000039560") || getNode(getNode(rida, "Line-Item"),"SupplierItemCode") != null){       	         
          // System.out.println(xml.getTarneKuup�ev().toLocaleString() + "   ref:" + reference);	
           try{
           	toode = getNode(getNode(rida, "Line-Item"),"SupplierItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
           }catch(Exception o ){
        	   try{
        		   toode = getNode(getNode(rida, "Line-Item"),"EAN").getFirstChild().getNodeValue();//ol.getPartnerProdID();
        	   }catch (Exception aa){
        		   toode = getNode(getNode(rida, "Line-Item"),"BuyerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
        	   }
           }
          	String prodName ="";
          	String unit = " ";
          	String lineNum = null;
          	try{
              	lineNum =getNode(getNode(rida, "Line-Item"),"LineNumber").getFirstChild().getNodeValue();
          		
          	}catch(Exception e ){
          		
          	}
          	
          	try{
              	prodName =getNode(getNode(rida, "Line-Item"),"ItemDescription").getFirstChild().getNodeValue();
          		
          	}catch(Exception e ){
          		
          	}
          	
        	
          	
        	try{
              	unit =getNode(getNode(rida, "Line-Item"),"UnitOfMeasure").getFirstChild().getNodeValue();          		
          	}catch(Exception e ){
          		
          	}
          	
        	
        	try{
              	partii = getNode(getNode(rida, "Line-Item"),"ExpirationDate").getFirstChild().getNodeValue();            	
              	partii = partii + " " + getNode(getNode(root, "Order-Header"),"ExpectedDeliveryDate").getFirstChild().getNodeValue();  
          	}catch(Exception e ){
          		
          	}
           System.out.println("id '" + toode + "'" + partii);
           double  kog = Double.parseDouble(getNode(getNode(rida, "Line-Item"),"OrderedQuantity").getFirstChild().getNodeValue()) ;
           
           double inunit = 0;
           try{
             	inunit =Double.parseDouble( getNode(getNode(rida, "Line-Item"),"OrderedUnitPacksize").getFirstChild().getNodeValue());
         		
         	}catch(Exception e ){
         	}
           
           
         	double discount = 0;
         	try{
         		discount =Double.parseDouble( getNode(getNode(rida, "Line-Item"),"Discount").getFirstChild().getNodeValue());
         		
         	}catch(Exception e ){
         	}
           
         	String dates = "";
         	
         	try{
         		dates = "1:" + ( getNode(getNode(rida, "Line-Item"),"ExpirationDate").getFirstChild().getNodeValue());
         		dates = dates + "2:" + ( getNode(getNode(rida, "Line-Item"),"MaxExpirationDate").getFirstChild().getNodeValue());
         	}catch(Exception e ){
         	}
           
         	if(dates.trim().length() > 0) {
         		partii = dates;
         	}
         	
            try{
            prod = driver.identifyProduct(ean,toode);
        
              
              
              try{
            	  if (driver instanceof VichiLITSQLImportDriver){
                   String  prod1 =  ((VichiLITSQLImportDriver)driver).identifyProduct2(ean, toode);
                   if(prod1 != null && prod1.trim().length() > 0){
                	   prod=prod1;
                   }
                    } 	  
                }catch(Exception e ){
                	
                }
              
                  } catch (SQLException e) {
                	  e.printStackTrace();
                  }
                  if (prod == null){
                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
                   // break;
                	prod = " ";
                	if(prod.trim().length() == 0 ){
                		troubles.append("PRODUCT CANNOT BE RESOLVED Edisoft XML format : \n" + "Dok Nr: " + doknr + " " + reference+  " Custname: " + tellijaNimekood +"\n"
                				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + prodName + " amount: " + kog + "\n\n");
                   }
                  }
                  double convert = 1 ;
                  boolean up = false;
           //       System.out.println("conCod.size()" + conCod.size());
                  if(conCod != null){
                  	Enumeration enume = conCod.elements();
                  	while(enume.hasMoreElements()){
                  		ConvertData dd= (ConvertData)enume.nextElement();
                  		System.out.println("toode" + toode + "'" + prod + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+
                  				" " + dd.getConvert() +" " + dd.getProdOrder());
                  		if(dd.getProdOwn().equals(prod)){
                  			convert = dd.getConvert();
                  			unit = "PC.";
                  			up = dd.isRoundup();
                  			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
                  				prod = dd.getProdOrder();
                  			}
                  		}
                  	}
                  	
                  }
                  
                double  kogus = Double.parseDouble(getNode(getNode(rida, "Line-Item"),"OrderedQuantity").getFirstChild().getNodeValue()) * convert;
                 double price = 0;
                 try{
                	 price = Double.parseDouble(getNode(getNode(rida, "Line-Item"),"OrderedUnitNetPrice").getFirstChild().getNodeValue());
                      
                 }catch(Exception e){
                	 
                 }
              if(up){
                	System.out.println("kogus" + kogus);
                	kogus = Math.round(kogus + 0.49);
                	System.out.println("kogusrr" + kogus);
                }
              
              int status = 80;
               if(doctype.equals("R")){
            	   status = 1 ;
               }
               
               String nameCu = "";
               try{
            	   nameCu =  getNode(getNode(getNode(root, "Order-Parties"), "DeliveryPoint"),"Name").getFirstChild().getNodeValue();	
               }catch(Exception e){
            	   
               }
               if(firma == null){
            	 //  nameCu = tellijaKood;
               }
               
             
               orders.setInfo(reference);   
               orders.setCompName(nameCu);
               orders.setTeostaja(saatja);
               if(deliveryILN != null && deliveryILN.trim().length() > 0){
               	 orders.setILN(deliveryILN);
               }
                  
               if(prod != null && !prod.equals("NOPANT") ){
            	   orders.add(firma, prod, prodName , kogus ,reference, null, inunit, discount,datelong,nameCu,price,0,false,
                  		 "",status,unit,tarnekell,partii,ean,null,true,null,lineNum);
               }
           
            prod = null;
            counter++;
          
          }	
          }
         }
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
       }

  
  

  
  private void loadXMLGrusteOrder(Orders orders, File f,StringBuffer troubles) {
  
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	try {
		db = dbf.newDocumentBuilder();
	
	System.out.println(f.getAbsolutePath());
	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
	
    Document doc = db.parse(in,handler);
    
    Node root = getNode(doc, "Uzsakymas");
  
  //  System.out.println("1111 " + getNode(getNode(root, "Order-Parties"), "Buyer"));
    
    //System.out.println("2222 " + getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
  // System.out.println("root " + root);
    
    String fileName = f.getName();
   
    String tellijaKood = null;
    String tarnekell = null;
    
   
    try{
    	tellijaKood =  getNode(getNode(root, "Uzsakovas"), "ParduotuvesKodas").getFirstChild().getNodeValue();
    }catch (Exception e){

    }
   
    Vector conCod = null;
   
    String name = " ";
    firma = "";
    String prod = null;
    String ean = null;
    String toode = null;
    String regnr = "gruste";
    int counter = 0;
    String datelong = "";
    String reference = "";
    String saatja = null;
    try{
    	datelong = parseDate(getNode(root,"PristatymoData").getFirstChild().getNodeValue()).getTime()+"";	
    }catch(Exception w){
    }
    
    if(datelong.equals("0")){
    	datelong = new Date().getTime() + "";
    }
    	
    
    reference =  getNode(root,"UzsakymoNumeris").getFirstChild().getNodeValue();
    if(reference == null){ 	
    	reference = " ";
    }
        

        try{
        	if(((firma == null || firma.equals("") ) && tellijaKood != null)){
        		 firma = driver.identifyCustomer(tellijaKood,regnr);
                 conCod = getCustConvCodes(firma);            
                 System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
        	}
           
           // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
          } catch (SQLException e) {
           
        	   }
          
          

          if(firma == null){
        	   try{
                   firma = driver.identifyCustomer(tellijaKood,regnr);
                   conCod = getCustConvCodes(firma);
                   System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
                 } catch (SQLException e) {
                  
               	   }  
          }
          
          
          System.out.println("Firma Identified " + firma + " " + reference  );
          
          if (firma == null || firma.trim().length() == 0){
              addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
              troubles.append("Customer CANNOT BE RESOLVED Gruste XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
             System.out.println(troubles);
          }
    	
    	
    	
    	Node read = getNode(root, "Eilutes");
    	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
          Node rida = read.getChildNodes().item(i);
          if (rida.getNodeName().equalsIgnoreCase("Eilute")){
            
     String partii = "";
           
           
           try{
        	   ean = getNode(rida,"Barkodas").getFirstChild().getNodeValue();
              }catch(Exception o ){
            	  try{
            		 	ean = getNode(getNode(rida, "Eilute"),"PrekesKodas").getFirstChild().getNodeValue();//ol.getPartnerProdID();
            	  }catch(Exception s){
            		  
            	  }
             
              }
              
                     
          if(ean != null && !ean.equals("2050000039348")){       	         
          // System.out.println(xml.getTarneKuup�ev().toLocaleString() + "   ref:" + reference);	
           try{
           	toode = getNode(rida,"PrekesKodas").getFirstChild().getNodeValue();//ol.getPartnerProdID();
           }catch(Exception o ){
        	   try{
        		   toode = getNode(rida,"Barkodas").getFirstChild().getNodeValue();//ol.getPartnerProdID();
        	   }catch (Exception aa){
        		   toode = getNode(rida,"PrekesKodas").getFirstChild().getNodeValue();//ol.getPartnerProdID();
        	   }
           }
          	String prodName ="";
          	String unit = " ";
          	try{
              	prodName =getNode(rida,"PrekesPavadinimas").getFirstChild().getNodeValue();
          		
          	}catch(Exception e ){
          	}
          	
        	try{
              	unit = getNode(rida, "Matas").getFirstChild().getNodeValue();          		
          	}catch(Exception e ){
          	}
          	
        	
           double  kog = Double.parseDouble(getNode(rida,"Kiekis").getFirstChild().getNodeValue()) ;
           

           System.out.println("reference " + reference + " " + ean + " " + kog + " " + prodName + " " + unit);
            try{
            prod = driver.identifyProduct(ean,toode);    
                  } catch (SQLException e) {
                	  e.printStackTrace();
                  }
                  if (prod == null){
                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
                   // break;
                	prod = " ";
                	if(prod.trim().length() == 0 ){
                		troubles.append("PRODUCT CANNOT BE RESOLVED Gruste XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
                				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + prodName + " amount: " + kog + "\n\n");
                   }
                  }
                  double convert = 1 ;
                  boolean up = false;
           //       System.out.println("conCod.size()" + conCod.size());
                  if(conCod != null){
                  	Enumeration enume = conCod.elements();
                  	while(enume.hasMoreElements()){
                  		ConvertData dd= (ConvertData)enume.nextElement();
                  		System.out.println("toode" + toode + "'" + prod + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+
                  				" " + dd.getConvert() +" " + dd.getProdOrder());
                  		if(dd.getProdOwn().equals(prod)){
                  			convert = dd.getConvert();
                  			unit = "PC.";
                  			up = dd.isRoundup();
                  			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
                  				prod = dd.getProdOrder();
                  			}
                  		}
                  	}
                }
                  
                double  kogus = Double.parseDouble(getNode(rida,"Kiekis").getFirstChild().getNodeValue()) * convert;
                 double price = 0;
                
              if(up){
                	System.out.println("kogus" + kogus);
                	kogus = Math.round(kogus + 0.49);
                	System.out.println("kogusrr" + kogus);
                }
              
              int status = 45;
               
               
               String nameCu = "";
               try{
            	   nameCu =  getNode(getNode(root, "Uzsakovas"), "Parduotuve").getFirstChild().getNodeValue();	
               }catch(Exception e){
            	   
               }
             
               
            orders.add(firma, prod, prodName , kogus ,reference, null, 0, 0,datelong,nameCu,price,0,false,"",status,unit,
            		tarnekell,partii,ean,null,true,null,null);
            orders.setInfo(reference);   
            orders.setCompName(nameCu);
            orders.setTeostaja(saatja);
            
           
            prod = null;
            counter++;
          
          }	
          }
         }
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
       }

  private void loadXMLLaimaOrder(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;
		
	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "Data");
	  
	  //  System.out.println("1111 " + getNode(getNode(root, "Order-Parties"), "Buyer"));
	    
	    //System.out.println("2222 " + getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
	  // System.out.println("root " + root);
	    
	    String fileName = f.getName();
	   
	    String tellijaKood = null;
	    String tarnekell = null;
	    
	   
	    try{
	    	tellijaKood =  getNode(getNode(root, "CompanyInfo"), "CoRegNum").getFirstChild().getNodeValue();
	    }catch (Exception e){

	    }
	   
	    Vector conCod = null;
	   
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = "laima";
	    int counter = 0;
	    String datelong = "";
	    String reference = "";
	    String saatja = null;
	    
	    datelong = new Date().getTime() + "";
	    
	    reference =  getNode(getNode(root, "tplJournalSpec"), "JournalId").getFirstChild().getNodeValue();//RefId
	    if(reference == null){ 	
	    	reference = " ";
	    }
	    
	    datelong =  parseDate( getNode(getNode(root, "tplJournalSpec"), "TransDate").getFirstChild().getNodeValue())+"";
	    firma = "M20240";
	        try{
	        	if(((firma == null || firma.equals("") ) && tellijaKood != null)){
	        		 firma = driver.identifyCustomer(tellijaKood,regnr);
	                 conCod = getCustConvCodes(firma);            
	                 System.out.println("firmakood " + firma + "|" + tellijaKood + "|" + regnr);
	        	}
	           
	           // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	          } catch (SQLException e) {
	           
	        	   }
	          
	          

	          if(firma == null){
	        	   try{
	                   firma = driver.identifyCustomer(tellijaKood,regnr);
	                   conCod = getCustConvCodes(firma);
	                   System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                 } catch (SQLException e) {
	                  
	               	   }  
	          }
	          
	          
	          System.out.println("Firma Identified " + firma + " " + reference  );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED Laima XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
	             System.out.println(troubles);
	          }
	    	
	    	
	    	
	    	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	        for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          Node rida = root.getChildNodes().item(i);
	      	System.out.println("rida  " + rida.getNodeName() );
		    
	          if (rida.getNodeName().equalsIgnoreCase("tplJournalLine")){
	        	  
	     String partii = "";
	           
	           
	           try{
	        	   ean = getNode(rida, "ItemIdKalev").getFirstChild().getNodeValue();
	              }catch(Exception o ){
	            	
	              }
	              
	        /*   <JournalId>00070475</JournalId>
	           <TransferInvoiceDate/>
	           <TransferInvoiceNumber>00070475</TransferInvoiceNumber>
	           <LineNum>1</LineNum>
	           <InventQty>800.0000000000</InventQty>
	           <ItemId>H180000800</ItemId>
	           <ItemIdKalev>848769</ItemIdKalev>
	           <QtyBox>20.0000000000</QtyBox>
	           <InventBatchCustAccount/>
	           <InventBatchExpDate>2018-07-11</InventBatchExpDate>
	           <InventBatchId>110718</InventBatchId */
	           
	                     
	          if(ean != null ){       	         
	          // System.out.println(xml.getTarneKuup�ev().toLocaleString() + "   ref:" + reference);	
	           	toode = ean;//ol.getPartnerProdID();
	           
	          	String prodName ="";
	          	String unit = " ";
	          	
	           double  kog = Double.parseDouble(getNode(rida,"InventQty").getFirstChild().getNodeValue()) ;
	           
	           System.out.println("reference " + reference + " " + ean + " " + kog + " " + prodName + " " + unit);
	           
	            prod = ean;    
	                 
	            double kogus = Double.parseDouble(getNode(rida,"InventQty").getFirstChild().getNodeValue());
	            double price = 0;
	            int status = 45;
	            String nameCu = "";
	               
	            orders.add(firma, prod, prodName , kogus ,reference, null, 0, 0,datelong,nameCu,price,0,false,"",status,unit,
	            		tarnekell,partii,ean,null,true,null,null);
	            orders.setInfo(reference);   
	            orders.setCompName(nameCu);
	            orders.setTeostaja(saatja);
	           
	            prod = null;
	            counter++;
	          
	          }	
	          }
	         }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       }

  
  
  
  private void loadXMLEDISOFTRecadv(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;
		
	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "Document-ReceivingAdvice");
	  
	
	  // System.out.println("root " + root);
	    
	    String fileName = f.getName();
	   
	    String tellijaKood = null;
	   
	    try{
	    	tellijaKood =  getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "Buyer"),"CodeBySupplier").getFirstChild().getNodeValue();
	    }catch (Exception e){
	    	
	    	try{

	        	tellijaKood = getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue();	
	    	}catch (Exception ee){
	    		try{
	    		 	
	    			
	    			tellijaKood = getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"CodeByBuyer").getFirstChild().getNodeValue();
	    	    			
	    		}catch(Exception rr){
	    			
	    		}
	       }
	    }
	 
	    if(tellijaKood != null && (tellijaKood.equals("-") || tellijaKood.equals("00") || tellijaKood.equals("10263574")|| 
	    		tellijaKood.equals("3090"))){
	    try{
	    	 tellijaKood = getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue();
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	   
	    }
	    
	    
	    
	    String tellijaILN = null;
	    try{
	    	tellijaILN =  getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "Buyer"),"ILN").getFirstChild().getNodeValue();
	    }catch (Exception e){
	    	
	    }
	    

	    String deliveryILN = null;
	    try{
	    	deliveryILN =  getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"ILN").getFirstChild().getNodeValue();
	    }catch (Exception e){
	    	
	    }
	    System.out.println("delivery ILN " + deliveryILN);
	    String tellijaNimekood = null;
	    try{
	    	tellijaNimekood =  getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"Name").getFirstChild().getNodeValue();
	    }catch (Exception e){
	    	
	    }
	    
	    Vector conCod = null;
	   
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    String datelong = "";
	    String reference = "";
	    String doctype  = "";
	    String saatja = null;
	    	datelong = parseDate(getNode(getNode(root, "ReceivingAdvice-Header"),"ReceivingAdviceDate").getFirstChild().getNodeValue()).getTime()+"";	
	        try{
	        	doctype = getNode(getNode(root, "ReceivingAdvice-Header"),"DocumentFunctionCode").getFirstChild().getNodeValue();
	        }catch(Exception e){
	        	
	        }
	        
	        
	        try{
	            firma = driver.identifyCustomer(deliveryILN,regnr);
	            System.out.println("firmakood " +firma + "|" + deliveryILN + "|" + regnr);
	            conCod = getCustConvCodes(firma);
	            
	            //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	           // haddLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	          } catch (SQLException es) {
	           
	          }
	        
	        
	        try{
	        	if(((firma == null || firma.equals("") ) && tellijaKood != null)){
	        		 firma = driver.identifyCustomer(tellijaKood,regnr);
	                 conCod = getCustConvCodes(firma);            
	                 System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	        	}
	           
	           // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	          } catch (SQLException e) {
	           
	        	   }
	          
	          
	          if((firma == null  || firma.equals(tellijaKood) )&& deliveryILN != null){
		       	   try{
		                  firma = driver.identifyCustomer(deliveryILN,regnr);
		                  conCod = getCustConvCodes(firma);
		                  
		                  //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
		                 // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
		                } catch (SQLException es) {
		                 
		                }  
		          }

	          if(firma == null){
	        	   try{
	                   firma = driver.identifyCustomer(tellijaKood,regnr);
	                   conCod = getCustConvCodes(firma);
	                   
	                   System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                  // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                 } catch (SQLException e) {
	                  
	               	   }  
	          }
	          
	          
	          if(firma == null){
	       	   try{
	                  firma = driver.identifyCustomer(tellijaILN,regnr);
	                  conCod = getCustConvCodes(firma);
	                  
	                  //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                 // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                } catch (SQLException e) {
	                 
	                }  
	          }
	          if(firma == null){
	       	   try{
	                  firma = driver.identifyCustomer(tellijaNimekood.substring(0,tellijaNimekood.indexOf(" ")),regnr);
	                  conCod = getCustConvCodes(firma);
	                  
	                  //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                 // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                } catch (Exception e) {
	                 
	                }  
	          }
	          
	          if(firma == null){
	          	   try{
	                     firma = driver.identifyCustomer(getNode(getNode(getNode(root, "ReceivingAdvice-Parties"),
	                    		 "DeliveryPoint"),"CodeBySupplier").getFirstChild().getNodeValue(),null);
	                     conCod = getCustConvCodes(firma);
	                     
	                     //System.out.println("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                    // addLog("firmakood " +firma + "|" + tellijaKood + "|" + regnr);
	                   } catch (Exception e) {
	                    
	                   }  
	             }
	     
	          
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED Edisoft XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
	             System.out.println(troubles);
	          }
	    	
	    	
	    	
	    	Node read = getNode(root, "ReceivingAdvice-Lines");
	    	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("Line")){
	            
	     String partii = "";
	           
	           reference =  getNode(getNode(root, "ReceivingAdvice-Header"), "BuyerOrderNumber").getFirstChild().getNodeValue();
	           if(reference == null){ 	
	           	reference = " ";
	           }else{
	        	   reference = "0" + reference;
	           }
	           
	          // System.out.println("reference " + reference + " " + 	getNode(getNode(rida, "Line-Item"),"ItemDescription").getFirstChild().getNodeValue());
	           
	           try{
	        	   ean = getNode(getNode(rida, "Line-Item"),"EAN").getFirstChild().getNodeValue();
	              }catch(Exception o ){
	            	  try{
	            		 	ean = getNode(getNode(rida, "Line-Item"),"BuyerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	            	  }catch(Exception s){
	            		  
	            	  }
	             
	              }
	          if(!ean.equals("2050000039348")){       	         
	          // System.out.println(xml.getTarneKuup�ev().toLocaleString() + "   ref:" + reference);	
	           try{
	           	toode = getNode(getNode(rida, "Line-Item"),"SupplierItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           }catch(Exception o ){
	        	   try{
	        		   toode = getNode(getNode(rida, "Line-Item"),"EAN").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	        	   }catch (Exception aa){
	        		   toode = getNode(getNode(rida, "Line-Item"),"BuyerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	        	   }
	           }
	          	String prodName ="";
	          	String unit = " ";
	          	try{
	              	prodName =getNode(getNode(rida, "Line-Item"),"ItemDescription").getFirstChild().getNodeValue();
	          		
	          	}catch(Exception e ){
	          		
	          	}
	          	
	        	try{
	              	unit =getNode(getNode(rida, "Line-Item"),"UnitOfMeasure").getFirstChild().getNodeValue();          		
	          	}catch(Exception e ){
	          		
	          	}
	          	
	        	
	        
	         //  System.out.println("id '" + toode + "'" + getNode(getNode(getNode(read, "Line"), "Line-Item"),"BuyerItemCode").getFirstChild().getNodeValue());
	           double  kog = Double.parseDouble(getNode(getNode(rida, "Line-Item"),"QuantityAccepted").getFirstChild().getNodeValue()) ;
	           
	           double inunit = 0;
	           try{
	             	inunit =Double.parseDouble( getNode(getNode(rida, "Line-Item"),"UnitPacksize").getFirstChild().getNodeValue());
	         		
	         	}catch(Exception e ){
	         	}
	           
	           
	            try{
	            prod = driver.identifyProduct(ean,toode);
	            
	            try{
	            	 if  (driver instanceof SkaiteEugImportDriver ){
		           		  float iln = Float.parseFloat(tellijaILN);
		           		  String   prod2 = driver.getKodirovka(ean, iln);  
		     		       if(prod2 != null){
		     		    	   prod = prod2;
		     		       } 		          
		           	  }
		     	          System.out.println(" kodirovka "+ prod + " ean " + ean);
		     	       } catch (Exception e) {
		     	       e.printStackTrace();	
		     	       }
	         
	              
	              
	              try{
	            	  if (driver instanceof VichiLITSQLImportDriver){
	                   String  prod1 =  ((VichiLITSQLImportDriver)driver).identifyProduct2(ean, toode);
	                   if(prod1 != null && prod1.trim().length() > 0){
	                	   prod=prod1;
	                   }
	                    } 	  
	                }catch(Exception e ){
	                	
	                }
	              
	                  } catch (SQLException e) {
	                	  e.printStackTrace();
	                  }
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED Edisoft XML recadv format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + prodName + " amount: " + kog + "\n\n");
	                   }
	                  }
	                  double convert = 1 ;
	                  boolean up = false;
	           //       System.out.println("conCod.size()" + conCod.size());
	                  if(conCod != null){
	                  	Enumeration enume = conCod.elements();
	                  	while(enume.hasMoreElements()){
	                  		ConvertData dd= (ConvertData)enume.nextElement();
	                  		System.out.println("toode" + toode + "'" + prod + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+
	                  				" " + dd.getConvert() +" " + dd.getProdOrder());
	                  		if(dd.getProdOwn().equals(prod)){
	                  			convert = dd.getConvert();
	                  			unit = "PC.";
	                  			up = dd.isRoundup();
	                  			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
	                  				prod = dd.getProdOrder();
	                  			}
	                  		}
	                  	}
	                  	
	                  }
	                  
	                double  kogus = Double.parseDouble(getNode(getNode(rida, "Line-Item"),"QuantityAccepted").getFirstChild().getNodeValue()) * convert;
	                 double price = 0;
	               
	              if(up){
	                	System.out.println("kogus" + kogus);
	                	kogus = Math.round(kogus + 0.49);
	                	System.out.println("kogusrr" + kogus);
	                }
	              
	              int status = 79;
	               if(doctype.equals("R")){
	            	   status = 1 ;
	               }
	               
	               String nameCu = "";
	               try{
	            	   nameCu =  getNode(getNode(getNode(root, "ReceivingAdvice-Parties"), "DeliveryPoint"),"Name").getFirstChild().getNodeValue();	
	               }catch(Exception e){
	            	   
	               }
	               if(firma == null){
	            	 //  nameCu = tellijaKood;
	               }
	               
	             
	               
	               
	            orders.add(firma, prod, prodName , kogus ,reference, null, inunit, 0,datelong,nameCu,price,0,
	            		false,"",status,unit,null,partii,ean,null,true,null,null);
	            orders.setInfo(reference);   
	            orders.setCompName(nameCu);
	            orders.setTeostaja(saatja);
	            prod = null;
	            counter++;
	          
	          }	
	          }
	         }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	       }

  
  private void loadXMLTelemaUusOrder(Orders orders, File f,StringBuffer troubles) {
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    Node rootmain = getNode(doc, "E-Document");
	    Node root = getNode(rootmain, "Document");
   String editype = "";
       try{
    	   editype  = getNode(getNode(rootmain, "Header"), "EDIOperator").getFirstChild().getNodeValue();
    	  
       }catch(Exception e){
    	   
       }

	    String reference  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	    String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
	   
	    String reference2 = null;
	    String delyveryName = null;
	    String tegija = null;
	    String tarnekell = null;
	    String markused = null;
	    
	    
	    try{
	   
	  	   markused = getNode(getNode(getNode(root, "AdditionalInfo"), "Extension"),"InfoContent").getFirstChild().getNodeValue();

    
  	  
    		/*-<AdditionalInfo> -
    		 * <Extension extensionId="remarks">
    		 *  <InfoContent>Tarneaeg kell 09:30; PRINCESS 23,09,11</InfoContent> 
    		 *  </Extension>
    		 *   </AdditionalInfo>*/
	     }catch(Exception e){
	  	  // e.printStackTrace();
	     }
	     
	     
	     try{
	  	      markused = getNode(getNode(root, "AdditionalInfo"),"InfoContent").getFirstChild().getNodeValue();	    
		     }catch(Exception e){
		  	  // e.printStackTrace();
		     }

		     try{
		    	Node aaa = getNode(root, "AdditionalInfo");
		    	String ada = "";
		    	 for (int i = 0; i < aaa.getChildNodes().getLength(); i++){
			          Node rida = aaa.getChildNodes().item(i);
			           System.out.println("nodename " + rida.getNodeName());
			          if (rida.getNodeName().equalsIgnoreCase("Extension")){
			        	  try{
					        	String yyy = getNode(rida, "InfoContent").getFirstChild().getNodeValue();
					        	String xxx = getNode(rida, "InfoName").getFirstChild().getNodeValue();
					        	if(yyy != null && yyy.trim().length() > 0){

						        	  ada = ada + " " + xxx + " " + yyy ; 
					        	  }			        		  
			        	  }catch (Exception eee){
			        		//eee.printStackTrace();  
			        	  }

			          }
		    	 }
		    	 if(ada.length()>2){
				  	   markused = ada; 
		    	 }
		     }catch(Exception e){
		    	// e.printStackTrace();
		     }
		     
		     
	     System.out.println("Markused " + markused);
	      try{
	    	  tegija = getNode(getNode(root, "DocumentInfo"), "CreatedBy").getFirstChild().getNodeValue();
		  }catch(Exception rr){
     		 // rr.printStackTrace();
     	  } 
	    
	    try{
	    	Node ref = getNode(getNode(root, "DocumentInfo"), "RefInfo");
	    
	    	   for (int i = 0; i < ref.getChildNodes().getLength(); i++){

		 	          Node rida = ref.getChildNodes().item(i);
		 	          System.out.println(rida.getNodeName() );
		 	          if (rida.getNodeName().equalsIgnoreCase("SourceDocument")){
		 	        	  try{
		 	        		   String tyyp =	rida.getAttributes().getNamedItem("type").getNodeValue(); 
				 	    	    if(tyyp.equals("order")){
				 	//   	    	System.out.println("ref1 " + reference + " " + reference2 + " tyyp " + tyyp);
				 	    	    	reference = getNode(rida,"SourceDocumentNum").getFirstChild().getNodeValue();
				 	    	    }
				 	    	   if(tyyp.equals("desadv")){
				 //	  	    	System.out.println("ref2 " + reference + " " + reference2 + " tyyp " + tyyp);
				 	    	    	reference2 = getNode(rida,"SourceDocumentNum").getFirstChild().getNodeValue();
				 	    	    }
		 	        	  }catch(Exception rr){
		 	        		  rr.printStackTrace();
		 	        	  } 	    	 
		 	          }   
	    	   }
	    	System.out.println("ref " + reference + " " + reference2);
	    	   if(reference2 != null){
	    		   reference = reference + "@" + reference2;
	    	   }else{
	    		   reference  = getNode(getNode(getNode(getNode(root, "DocumentInfo"), "RefInfo"),"SourceDocument"),"SourceDocumentNum").getFirstChild().getNodeValue();	    		   	   
	    	   }
	    	 }catch(Exception e){	
	    }
	    
	    String delivery = null;
	    try{
		    delivery = getNode(getNode(getNode(root, "DocumentInfo"), "DateInfo"),"DeliveryDateRequested").getFirstChild().getNodeValue();
		    delivery = delivery.substring(0,10);
		    try{
		    	   tarnekell = delivery.substring(11,16);
				    System.out.println("kell " + tarnekell+ " '" +delivery + "'");
				    	
		    }catch(Exception w){
		    	
		    }
		 
		   
	   }catch(Exception ee ){
		   delivery = null;
	   }
	   
	   try{
		    delivery = getNode(getNode(getNode(root, "DocumentInfo"), "DateInfo"),"DeliveryDateActual").getFirstChild().getNodeValue();
		   delivery = delivery.substring(0,10);
	   }catch(Exception ee ){
		   
	   }
	   	 System.out.println("DeliveryLoetud " + delivery);
		 
	    String fileName = f.getName();
	   
	    String tellijaKood = null;
        String logistika = null;
	    String glnkood = null;
	    String ladu = null;
        String buyerGLN = null;
        
	  
        
        try{
        	delyveryName =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"Name").getFirstChild().getNodeValue();
	    	
	    	 System.out.println("DeliveryParty " + tellijaKood + " " + reference + " " + delivery);
	    }catch (Exception e){    
	    }
        
	    try{
        	ladu =  getNode(getNode(getNode(root, "DocumentParties"), "WarehouseParty"),"PartyCode").getFirstChild().getNodeValue();
	    	
	    	 System.out.println("Ladu " + ladu );
	    }catch (Exception e){    
	    }
	    
        try{
	    	tellijaKood =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	logistika = getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	 System.out.println("DeliveryParty " + tellijaKood + " " + reference + " " + delivery);
	    }catch (Exception e){    
	    }
	    try{
	    	glnkood =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"GLN").getFirstChild().getNodeValue();
	    	
	    }catch (Exception e){    
	    }
	    
	    try{
	    	buyerGLN =  getNode(getNode(getNode(root, "DocumentParties"), "BuyerParty"),"GLN").getFirstChild().getNodeValue();
	    }catch (Exception e){    
	    }
	    
	    try{
	    	logistika = getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    }catch (Exception e){    
	    }
	    
	    
	    try{
	    	tellijaKood =  getNode(getNode(getNode(root, "DocumentParties"), "OrderParty"),"PartyCode").getFirstChild().getNodeValue();
	    	 System.out.println("OrderParty " + tellijaKood + " " + reference + " " + delivery);
	    }catch (Exception e){    
	    }
	   // System.out.println("3333 " + tellijaKood + " " + reference + " " + delivery);
		 
	    
	    Vector conCod = null;
	   
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    String datelong = "";
	    int counter = 0;
	    
	    
	    	datelong = f.lastModified()+"";	
	    	if(delivery != null){
	    		datelong = this.getCalLit(delivery).getTime() + "";
	    	}else{
	    		Calendar ccc = Calendar.getInstance();
	    		if(ccc.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
	    			ccc.add(Calendar.DAY_OF_YEAR, 3);
	    		}else{
	    			ccc.add(Calendar.DAY_OF_YEAR, 1);
	    		}
	    		
	    		datelong = ccc.getTimeInMillis() + "";
	    	}
	        try{
	    	      
	    	  if (glnkood != null){
	        	  firma = driver.identifyCustomer(glnkood,regnr);
	          }
	        
		} catch (SQLException e) {
	           
 	   }
	        try{
	        	if(firma == null || firma.trim().length() == 0){
	        	       firma = driver.identifyCustomer(tellijaKood,regnr);
	   	            conCod = getCustConvCodes(firma);
	   	       }
	          } catch (SQLException e) {
	           
	        	   }
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED TelemaUus XML format \n" + "Dok Nr: " + doknr + " " + reference+  " Custname: " + delyveryName + "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + " tyyp: " + type  + "\n\n");
	            }
	    	
	    	
	    	
	    	Node read = getNode(root, "DocumentItem");
	    	
		    
	        
	    	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	         	toode = null;
	         	name = " ";
	         	ean = null;
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	      
	        
	        	 	try{
	       	              
	           	ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	         System.out.println("toode "+ean);
	         
	      	}catch(Exception e ){
          	}
	      
	          	try{
	              	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();	 
	              	name = toode;
	          	}catch(Exception e ){
	          	}
	        String linenum = null;  
	        try{
              	linenum = Integer.parseInt(getNode(rida, "LineItemNum").getFirstChild().getNodeValue())+"";	          		
          	}catch(Exception e ){
          	}
	          	try{
	              	name = getNode(rida, "ItemDescription").getFirstChild().getNodeValue();	
	              	// System.out.println("prodname " + name);
	          	}catch(Exception e ){
	          	}
	          	
	          	String unit = " ";
	          	
	          	try{
	              	unit =  getNode(getNode(rida, "ItemUnitRecord"), "ItemUnit").getFirstChild().getNodeValue();	
	              	 
	          	}catch(Exception e ){
	          	}

	          	if(unit.trim().length() == 0){
	          		try{
		              	unit = getNode(rida,  "BaseUnit").getFirstChild().getNodeValue();	
		              	 
		          	}catch(Exception e ){
		          	}
		          		
	          	}
	          	
	          	
	           String partii = null;
	           try{
                  partii =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
		       	partii = partii + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
		       			"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	           }catch(Exception e){
	        	   
	           }
	           
	         
	           
	          	double  kogus =  0;
	          	try{
	          		kogus = Double.parseDouble(getNode(rida, "AmountOrdered").getFirstChild().getNodeValue()) ;
	  	          		
	          	}catch(Exception e){try{
	          		
	          	}catch(Exception r){
	        		kogus = Double.parseDouble(this.makeNumeric(getNode(rida, "AmountOrdered").getFirstChild().getNodeValue())) ;
	  	       }
	          	}
	          	
	          	
	          	try{
	          		kogus =  -1 * Double.parseDouble(getNode(rida, "AmountToReturn").getFirstChild().getNodeValue()) ;
	  	          		
	          	}catch(Exception e){try{
	          		
	          	}catch(Exception r){
	        		kogus = -1 * Double.parseDouble(this.makeNumeric(getNode(rida, "AmountToReturn").getFirstChild().getNodeValue())) ;
	  	       }
	          	}
	          	
	          	
	          	double kogus2 = 0;
	          	if(type.equals("recadv") || type.equals("RECADV")){
	          		kogus2 = Double.parseDouble(getNode(rida, "AmountActual").getFirstChild().getNodeValue()) ;
	               // reference = getNode(getNode(getNode(getNode(root, "DocumentInfo"), "RefInfo"),"SourceDocument"),"SourceDocumentNum").getFirstChild().getNodeValue();
	          	}
	            try{
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {
	                	  e.printStackTrace();
	                  }

	                  
	                  
	                  
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   	troubles.append("PRODUCT CANNOT BE RESOLVED Telema Uus XML format : \n" + "Dok Nr: " + doknr + " " + reference+  " Custname: " + delyveryName + "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference + " " + name + " Code:" + toode + " EAN:" + ean + " amount: " + kogus + " tyyp: " + type  + "\n\n");
	                   prod =  " ";
	                  }
	                  boolean up = false;
	                  double convert = 1;
	               //   System.out.println("conCod.size()" + conCod.size() + " prod:" + prod);
	                  if(conCod != null && prod != null){
	                  	Enumeration enume = conCod.elements();
	                  	while(enume.hasMoreElements()){
	                  		ConvertData dd= (ConvertData)enume.nextElement();
	                  		//System.out.println("toode" + toode + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+ " " + dd.getConvert());
	                  		if(dd.getProdOwn().equals(prod)){
	                  			up = dd.isRoundup();
	                  			convert = dd.getConvert();
	                  			if(dd.getProdOrder() != null && dd.getProdOrder().trim().length() > 0){
	                  				prod = dd.getProdOrder();
	                  			}
	                  		}
	                  	}
	                  	
	                  }
	              	//System.out.println("kogus a " + kogus + " " + convert);
	                  kogus =  kogus*convert;
	              //	System.out.println("kogus b " + kogus);
	                
	                if(up){
	                	System.out.println("kogus e" + kogus);
	                	kogus = Math.round(kogus + 0.49);
	                	System.out.println("kogusrr" + kogus);
	                }
	                int status = 60;
	                double discount = 0;
	                double price = 0;
	                
	                boolean readprice = false;
	                if("true".equals(parent.getProps().getProperty("readprice"))){
	                	readprice = true;
	                }
	                if(editype.equals("Web") || readprice){ //kodupaberi Web poe juhtum 
	                	status = 59;
	                	try{
	                		discount = Double.parseDouble(getNode(getNode(rida, "Addition"), "AddRate").getFirstChild().getNodeValue());
	                	}catch(Exception e){
	                		
	                	}
	                	try{
	                		price = Double.parseDouble(getNode(rida, "ItemPrice").getFirstChild().getNodeValue());
		                }catch(Exception e){
	                		
	                	}
	                	
	                }
	                	
	             if(prod != null){
	            	 if(buyerGLN != null){
		 	            	orders.setILN(buyerGLN);
		 		 	    }
	           if(kogus != 0){
	        	   if(!prod.equals("NOPANT")){
	        		   orders.add(firma, prod, name , kogus ,doknum, logistika, kogus2, discount ,datelong,"0",price,0,false,
		            			 "",status,unit,tarnekell,partii,ean,delyveryName,true,null,linenum);
		                 System.out.println("status " + status + " disc " + discount + " " + linenum + " " + price + " *" + unit + "* " + orders.getILN());
		                 counter++;     
	        	   }
	            	
	                   	 
	            	  if (markused != null){
	            	    	 orders.setInfo(reference + " " + markused); 
	            	     }else{
	            	    	 orders.setInfo(reference); 
	            	     }
	 	            orders.setTeostaja(tegija);
	 	            orders.setCompName(type);
	 	            if(ladu != null){
	 	            	 orders.setTeostaja(ladu);	
	 	            }
	 	           
	 	           if(buyerGLN != null){
	 	            	orders.setILN(buyerGLN);
	 		 	    }
           	 
	 	           
	           }     	 
	             }
	            prod = null;
	          
	         
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }


  public static final String removeString(String sisse ,String eemalda){
  	
  	while (sisse.toLowerCase().indexOf(eemalda.toLowerCase()) != -1){
  		int index = sisse.toLowerCase().indexOf(eemalda.toLowerCase());
  		sisse = sisse .substring(0,index ) + sisse.substring(index + eemalda.length() );
  	}  
      return sisse;
    }


  
  private void loadXMLTelemaUusDesOrder(Orders orders, File f,StringBuffer troubles) {
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    Node rootmain = getNode(doc, "E-Document");
	    Node root = getNode(rootmain, "Document");


	    String reference  = "";
	    String doknum  = getNode(getNode(root, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	    String type  = getNode(root,"DocumentType").getFirstChild().getNodeValue();
	   
	    String reference2 = null;
	    String delyveryName = null;
	    String tegija = null;
	    String tarnekell = null;
	    String markused = null;
	    
	  

	    try{
	    	
	    	Node aaa = getNode(root, "AdditionalInfo");
	    	String ada = "";
	    	if(aaa != null){
		    	 for (int i = 0; i < aaa.getChildNodes().getLength(); i++){
			          Node rida = aaa.getChildNodes().item(i);
			           System.out.println("nodename " + rida.getNodeName());
			          if (rida.getNodeName().equalsIgnoreCase("InfoContent")){
			        	String yyy = getNode(getNode(rida, "Extension"),"InfoContent").getFirstChild().getNodeValue();
			        	String xxx = getNode(getNode(rida, "Extension"),"InfoName").getFirstChild().getNodeValue();
			        	
			        	  if(yyy != null && yyy.trim().length() > 0){

				        	  ada = ada + " " + xxx + " " + yyy ; 
			        	  }
			          }
		    	 }	    		
	    	}

	  	   markused = ada;
	     }catch(Exception e){
	    	 e.printStackTrace();
	     }
	     System.out.println("Markused " + markused);
	
	     Node header = getNode(rootmain, "Header");
	     
	     try{
	    	  tegija = getNode(header, "SenderID").getFirstChild().getNodeValue();
		  }catch(Exception rr){
   		 // rr.printStackTrace();
   	  } 
	     
	     try{
	    	  tegija = getNode(getNode(root, "DocumentInfo"), "CreatedBy").getFirstChild().getNodeValue();
		  }catch(Exception rr){
   		 // rr.printStackTrace();
   	     } 
		  System.out.println("Tegija " + tegija);
		  
	    try{
	    	
	    	/*-<AdditionalInfo><InfoName>Customer order</InfoName><InfoContent>600027974</InfoContent></AdditionalInfo>*/
	    	Node ref = getNode(root, "DocumentInfo");
	    
	    	   for (int i = 0; i < ref.getChildNodes().getLength(); i++){

		 	          Node rida = ref.getChildNodes().item(i);
		 	          System.out.println(rida.getNodeName() );
		 	          if (rida.getNodeName().equalsIgnoreCase("AdditionalInfo")){
		 	        	  try{
		 	        		 if(getNode(rida,"InfoName").getFirstChild().getNodeValue().equals("Customer order")){
		 	        			   if (reference.length()== 0){	 	        			   
			 	        				reference = getNode(rida,"InfoContent").getFirstChild().getNodeValue();  
			 	        		   }else{
			 	        				reference = reference + "," +  getNode(rida,"InfoContent").getFirstChild().getNodeValue();
			 	        		   }
					 	    	    
		 	        		 }
		 	        		try{
			 	        		
		 	        		 if(getNode(rida,"InfoName").getFirstChild().getNodeValue().equals("Notes")){
		 	        			markused = getNode(getNode(getNode(root, "AdditionalInfo"), "Extension"),"InfoContent").getFirstChild().getNodeValue();
		 	        		 }
		 	        		 }catch(Exception e){
			 	        			//e.printStackTrace(); 
			 	        		 }
			 	        		
		 	        		 
		 	        		 
		 	        		 try{
		 	        			if(markused.equals(reference)){
			 	        			markused = "";
			 	        		} 
		 	        		 }catch(Exception e){
		 	        			e.printStackTrace(); 
		 	        		 }
		 	        		
		 	        		 if(markused == null && reference != null){
		 	        			 markused = reference;
		 	        			 
		 	        		 }
				 	    	   
		 	        	  }catch(Exception rr){
		 	        		  rr.printStackTrace();
		 	        	  } 	    	 
		 	          }   
		 	          
		 	          
		 	          
		 	          

		 	         
		 	        
		 	        
	    	   }
	    	   
  try{
	 	       reference = getNode(getNode(getNode(getNode(root, "DocumentInfo"), "RefInfo"),"SourceDocument"),"SourceDocumentNum").getFirstChild().getNodeValue();
	 	      		   }catch(Exception e){
	 	        	//	e.printStackTrace()  ;
	 	        	  }
	    	System.out.println("refXXX " + reference + " " + reference2);
	    	
	    	 }catch(Exception e){	
	    }
	    
	    String delivery = null;
	    try{
		    delivery = getNode(getNode(getNode(root, "DocumentInfo"), "DateInfo"),"DeliveryDateRequested").getFirstChild().getNodeValue();
		    tarnekell = delivery.substring(11,16);
		    System.out.println("kell " + tarnekell+ " '" +delivery + "'");
		    delivery = delivery.substring(0,10);
		 
		   
	   }catch(Exception ee ){
		   
	   }
	   
	   String payment = null;

	   try{
		    payment = getNode(getNode(getNode(root, "DocumentInfo"), "DateInfo"),"DueDate").getFirstChild().getNodeValue();
		  	   }catch(Exception ee ){
		   
	   }
	   
	   try{
		    delivery = getNode(getNode(getNode(root, "DocumentInfo"), "DateInfo"),"DeliveryDateActual").getFirstChild().getNodeValue();
		   delivery = delivery.substring(0,10);
	   }catch(Exception ee ){
		   
	   }
	   	 System.out.println("DeliveryLoetud " + delivery);
		 
	    String fileName = f.getName();
	   
	    String tellijaKood = null;

	    String tellijaKood2 = null;
      String logistika = null;
	    String glnkood = null;
      
      try{
      	delyveryName =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"Name").getFirstChild().getNodeValue();
	    	
	  //  	 System.out.println("DeliveryParty " + tellijaKood + " "  + delyveryName);
	    }catch (Exception e){    
	    }
      
      try{
	    	tellijaKood =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	logistika = getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	tellijaKood =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"DepID").getFirstChild().getNodeValue();
	    	 System.out.println("DeliveryParty " + tellijaKood + " " + tellijaKood2 + " " + reference + " " + delivery);
	    }catch (Exception e){    
	    }
	    try{
	    	glnkood =  getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyGLN").getFirstChild().getNodeValue();
	    	
	    }catch (Exception e){    
	    }
	    try{
	    	logistika = getNode(getNode(getNode(root, "DocumentParties"), "DeliveryParty"),"PartyCode").getFirstChild().getNodeValue();
	    	 System.out.println("DeliveryParty.PartyCode " + logistika + " " + reference + " " + delivery);
	    }catch (Exception e){    
	    }
	    
	    
	    try{
	    	tellijaKood =  getNode(getNode(getNode(root, "DocumentParties"), "OrderParty"),"PartyCode").getFirstChild().getNodeValue();
	    	
	    }catch (Exception e){    
	    }
	   // System.out.println("3333 " + tellijaKood + " " + reference + " " + delivery);
		 
	    
	    Vector conCod = null;
	   
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    String datelong = "";
	    int counter = 0;
	    int paym = 0;
	    
	    try{
	        paym = getPaydays(getCalLit(delivery).getTime(),	getCalLit(payment).getTime());
	    }catch(Exception e){
	    	
	    }
	    

		     try{
		    	  paym = Integer.parseInt(getNode(getNode(root, "DocumentInfo"), "PaymenTerm").getFirstChild().getNodeValue());
			  }catch(Exception rr){
	   		 // rr.printStackTrace();
	   	     } 
	    
	    System.out.println("Paym  " + payment + " " + delivery + " " + paym);
	    	datelong = f.lastModified()+"";	
	    	if(delivery != null){
	    		datelong = this.getCalLit(delivery).getTime() + "";
	    	}else{
	    		Calendar ccc = Calendar.getInstance();
	    		if(ccc.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
	    			ccc.add(Calendar.DAY_OF_YEAR, 3);
	    		}else{
	    			ccc.add(Calendar.DAY_OF_YEAR, 1);
	    		}
	    		
	    		datelong = ccc.getTimeInMillis() + "";
	    	}
	      
	        
	        try{
	            firma = driver.identifyCustomer(tellijaKood,regnr);
	            conCod = getCustConvCodes(firma);
	          } catch (SQLException e) {
	           
	        	   }
	          if ((firma == null || firma.trim().length() == 0) && glnkood != null){
	        	  firma = driver.identifyCustomer(glnkood,regnr);
	          }
	          System.out.println("Firma Identified " + firma  + " " + tellijaKood + " " + regnr + " " + glnkood);
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED DESORDER XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + " tyyp: " + type  + "\n\n");
	            }
	    	
	    	
	    	
	    	Node read = getNode(root, "DocumentItem");
	    	
		    
	        
	    	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	         	toode = null;
	         	name = " ";
	         	ean = null;
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	      
	        
	        	 	try{
	       	              
	           	ean = getNode(rida, "GTIN").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	         System.out.println("toode "+ean);
	         
	      	}catch(Exception e ){
        	}
	      
	          	try{
	              	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();	 
	              	name = toode;
	          	}catch(Exception e ){
	          	}
	        int linenum = 0;  
	        try{
            	linenum = Integer.parseInt(getNode(rida, "LineItemNum").getFirstChild().getNodeValue());	          		
        	}catch(Exception e ){
        	}
	          	try{
	              	name = getNode(rida, "ItemDescription").getFirstChild().getNodeValue();	
	              	// System.out.println("prodname " + name);
	          	}catch(Exception e ){
	          	}
	          	
	          	String unit = " ";
	          	
	          	try{
	              	unit =  getNode(getNode(rida, "ItemUnitRecord"), "ItemUnit").getFirstChild().getNodeValue();	
	              	 
	          	}catch(Exception e ){
	          	}

	          	
	          	
	           String partii = null;
	           try{
                partii =  getNode(getNode(rida, "ItemReserve"), "LotNum").getFirstChild().getNodeValue();
		       	partii = partii + "|" + getCalLit(getNode(getNode(rida, "ItemReserve"),
		       			"BestBeforeMin").getFirstChild().getNodeValue()).getTime();
	           }catch(Exception e){   	   
	           }

	           String vat = "20";
	           
	           try{
	        	   String vv = getNode(getNode(rida, "VAT"), "VATRate").getFirstChild().getNodeValue();
	                vat =  Integer.parseInt(vv.substring(0,vv.indexOf("."))) + "";
			      
		           }catch(Exception e){
		        	   e.printStackTrace();
		           } 
		          // System.out.println("Rea KM " + vat);
		           
	          	double  kogus = Double.parseDouble(getNode(rida, "AmountOrdered").getFirstChild().getNodeValue()) ;
	        	double  linesum = 0;
	        	 try{
	        		 linesum = Double.parseDouble(getNode(rida, "ItemSum").getFirstChild().getNodeValue()) ;
		          }catch(Exception e){
		        	  
		          }
	        	 double discount = 0 ;
	          try{
	        	   discount = Double.parseDouble(getNode(getNode(rida, "Addition"),"AddRate").getFirstChild().getNodeValue()) ;
	          }catch(Exception e){
	        	  
	          }
	          	if(discount < 0){
	          		discount = discount * -1;
	          	}
	            try{
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {
	                	  e.printStackTrace();
	                  }


	   	           System.out.println("partii " + partii + " " + ean + " " + toode + " " + kogus + " " + prod + " " + vat);
	                  
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   	troubles.append("PRODUCT CANNOT BE RESOLVED DESORDER XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference + " " + name + " Code:" + toode + " EAN:" + ean + " amount: " + kogus + " tyyp: " + type  + "\n\n");
	                   prod =  " ";
	                  }
	             
	                int status = 44;
	             if(prod != null){
	            	 orders.add(firma, prod, name , kogus ,doknum, logistika, linesum, linenum ,datelong,vat,discount,paym,false,
	            			 "",status,unit,tarnekell,partii,ean,delyveryName,true,null,null);
	            	 
	            	orders.setInfo(reference); 
	            	
	 	            orders.setTeostaja(tegija);
	 	            orders.setCompName(type);
	 	            orders.setShipMode(markused);
	 	         
	 	            counter++;
	 	            	 
	             }
	            prod = null;
	          
	         
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }

  
  private Date parseDate(String value){
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt(value.substring(0, 4)));
    cal.set(Calendar.MONTH, Integer.parseInt(value.substring(5, 7))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value.substring(8, 10)));
    return new Date(cal.getTime().getTime());
  }
  
  private Node getNode(Node rt, String s){
    return GenericDOM.getNode(rt, s);
  }
  


 
  
  private void loadXMLDrogasOrder(Orders orders, File f,StringBuffer troubles) {
	  
	
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			
			  FileReader ff = new FileReader(f);
				BufferedReader br = new BufferedReader(ff);
				String line = null;
				String asi = null;
				while( (line = br.readLine()) != null){
					if(line.indexOf("<?xml version=\"1.0\"?>") != -1){
						asi  = "<?xml version=\"1.0\"?>\n" ;
					}else if(line.indexOf("<name>") == -1){
						asi  = asi + "\n" + line ;
					}			
				}
				br.close();
				ff.close();
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		System.out.println(asi);
		
		InputStream xml = new ByteArrayInputStream(asi.getBytes("UTF-8")); 
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder(); 
		Document doc = docBuilder.parse(xml); 
		doc.getDocumentElement().normalize(); 
		xml.close();  
		
		
	//	String handler = null;

	  //  Document doc = db.parse(new ByteArrayInputStream(asi.getBytes("UTF-8")),handler);
	    
	    Node root = getNode(doc, "order");
	  	    

	    String reference  = root.getAttributes().getNamedItem("reference").getNodeValue();
	    String delivery = null;
	    try{
		    delivery = getNode(root, "delivery-time").getFirstChild().getNodeValue();
		   delivery = delivery.substring(0,10);
	   }catch(Exception ee ){
		   
	   }
	   
	 
	    String fileName = f.getName();
	   
	    String tellijaKood = null;

	    try{
	    	tellijaKood =  "DR" +getNode(root, "location").getFirstChild().getNodeValue();
	    }catch (Exception e){    
	    }
	    
		 
	    
	    Vector conCod = null;
	   
	    String name = " ";
	    firma = "";
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    String datelong = "";
	    int counter = 0;
	    String doctype  = "";
	    
	    
	    	datelong = f.lastModified()+"";	
	    	if(delivery != null){
	    		datelong = this.getCalHTM(delivery);
	    	}else{
	    		Calendar ccc = Calendar.getInstance();
	    		if(ccc.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
	    			ccc.add(Calendar.DAY_OF_YEAR, 3);
	    		}else{
	    			ccc.add(Calendar.DAY_OF_YEAR, 1);
	    		}
	    		
	    		datelong = ccc.getTimeInMillis() + "";
	    	}
	      
	        
	        try{
	            firma = driver.identifyCustomer(tellijaKood,regnr);
	            conCod = getCustConvCodes(firma);
	          } catch (SQLException e) {
	           
	        	   }
	          
	          
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED DROGAS XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + tellijaKood + "\n\n");
	            }
	    	
	    	
	    	
	    	Node read = getNode(root, "details");
	    	
		    
	        
	    	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("line")){
	      
	        
	              
	           	toode = getNode(rida, "code").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	         System.out.println("toode "+toode);
	          	String prodName ="";
	          	
	          	try{
	              	prodName =" Name: " + getNode(rida, "name").getFirstChild().getNodeValue();	          		
	          	}catch(Exception e ){
	          	}
	         
	          	double  kogus = Double.parseDouble(getNode(rida, "quantity").getFirstChild().getNodeValue()) ;
	          	
	            ean = toode;
	            try{
	            prod = driver.identifyProduct(ean,toode);

	            try{
	           	  if  (driver instanceof SkaiteEugImportDriver ){
	     		       String   prod2 = driver.getKodirovka(ean, 90);  
	     		       if(prod2 != null){
	     		    	   prod = prod2;
	     		       }
	           	  }
	     	          System.out.println(" kodirovka "+ prod + " ean " + ean);
	     	       } catch (Exception e) {
	     	       e.printStackTrace();	
	     	       }
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED DROGAS XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference + " " + name + " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }
	                  boolean up = false;
	             //     System.out.println("conCod.size()" + conCod.size() + " prod:" + prod);
	                  if(conCod != null){
	                  	Enumeration enume = conCod.elements();
	                  	while(enume.hasMoreElements()){
	                  		ConvertData dd= (ConvertData)enume.nextElement();
	                  		//System.out.println("toode" + toode + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+ " " + dd.getConvert());
	                  		if(dd.getProdOwn().equals(prod)){
	                  			up = dd.isRoundup();
	                  			
	                  		}
	                  	}
	                  	
	                  }
	                  
	                
	                if(up){
	                	System.out.println("kogus" + kogus);
	                	kogus = Math.round(kogus + 0.49);
	                	System.out.println("kogusrr" + kogus);
	                }
	                int status = 90;
	               if(doctype.equals("R")){
	            	   status = 1 ;
	               }
	            orders.add(firma, prod, name , kogus ,reference, null, 0, 0,datelong,"0",0,0,false,"",status," ",null,"",ean,
	            		null,true,null,null);
	            orders.setInfo(reference);
	            
	            counter++;
	            prod = null;
	          
	         
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }


  
  
  private void loadRivileEIPOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String reference = null;
	    String info = null;
	    String filiaal = "";
	    
	      while ((line = br.readLine()) != null){
	    	
	  	    if(line.indexOf("<I06_DOK_DATA>") != -1){
	  	    	datelong = line.substring(line.indexOf("<I06_DOK_DATA>")+14,line.indexOf("</I06")).trim().substring(0,10);
	  	  	System.out.println("time1 " + datelong);
	  	    	datelong = this.getCalLit(datelong).getTime() + "";
	  	    	System.out.println("time2 " + datelong);
	  	    }
	    	 
	  	  if(line.indexOf("<I06_DOK_NR>") != -1){
	  	    	  reference = line.substring(line.lastIndexOf("<I06_DOK_NR>")+12,line.indexOf("</I06")).trim();
	  	    	System.out.println("reference " + reference);
	  	      }

	  	  if(line.indexOf("<I06_KODAS_KS>") != -1){
	  		tellijaKood = line.substring(line.lastIndexOf("<I06_KODAS_KS>")+14,line.indexOf("</I06")).trim();
	  	      }
	 
	  	  if(line.indexOf("<I06_KODAS_AK>") != -1){
	  		filiaal = line.substring(line.lastIndexOf("<I06_KODAS_AK>")+14,line.indexOf("</I06")).trim();
	  	    	break;
	  	      }

	  	  
	      }
	      tellijaKood = tellijaKood + "-" + filiaal;
	      
	      //System.out.println("firma tuvastamisele " + tellijaKood );
	     
	  	     try{
		      kood = tellijaKood;
		           System.out.println("Kood: "+ kood);
		          firma = driver.identifyCustomer(kood,tellijaRegnr);
		          System.out.println("'"+firma+"'");
		        } catch (SQLException e) {}
		       
		        if (firma == null || firma.trim().length() == 0){
		          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
		           troubles.append("Customer CANNOT BE RESOLVED TXT EIP format \n" + "Dok Nr: " + doknr + " " + reference+ "\n" +  "SHOPCODE " + kood + " by buyer\n" 
		       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");    		       
		        }
		        		     
    
	  	  String ol = null;
	    while( (ol = br.readLine()) != null){
	     
       
	     System.out.println("ol" + ol);
	      if(ol.indexOf("</I06>") != -1){
	    	   br.close();
	   	      fr.close();
	    	  return;
	      }
	      

	        String prod = null ;
            String ean = null;
            String yhik = null;
            double kogus = 0;
	          
	      while( (ol = br.readLine()) != null){
	    	  

	    	  if(ol.indexOf("<DI07_BAR_KODAS>") != -1){
	  	  		ean = ol.substring(ol.lastIndexOf("<DI07_BAR_KODAS>")+16,ol.indexOf("</DI07")).trim();
	  	  	    	System.out.println("ean  " + ean);
	  	  	      }
	    	  if(ol.indexOf("<I07_KIEKIS>") != -1){
		  	  		kogus =   Double.parseDouble(ol.substring(ol.lastIndexOf("<I07_KIEKIS>")+12,
		  	  				ol.indexOf("</I07")).trim());
		  	  		
		  	  	    	System.out.println("kogus " + kogus);
		  	  	      }
	    	  
	    	  if(ol.indexOf("<I07_KODAS_US_P>") != -1){
		  	  		yhik = ol.substring(ol.lastIndexOf("<I07_KODAS_US_P>")+16,ol.indexOf("</I07")).trim();
		  	  	    	System.out.println("yhik " + yhik);
		  	  	      }
	  	  
	    	  if(ol.indexOf("</I07>") != -1){
	    		     try{
	   	  	          prod = driver.identifyProduct(ean,null);
	   	  	       } catch (SQLException e) {
	   	  	       e.printStackTrace();	
	   	  	       }
	   	  	
	   	  	          try{
	   	  	         if  (driver instanceof SkaiteEugImportDriver ){
	   	  			       
	   	  			     String   prod2 = driver.getKodirovka(ean, 91);  
		     		       if(prod2 != null){
		     		    	   prod = prod2;
		     		       }
	   	  	        	  }
	   	  		          System.out.println("kodirovka "+ prod + " ean " + ean);
	   	  		       } catch (Exception e) {
	   	  		       e.printStackTrace();	
	   	  		       }
	   	  		
	   	  	       if (prod == null ){
	   	  	         addLog(parent.getString("noprodid") + "\n " + ean + " " + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");

	   	  		       if(prod == null || prod.trim().length() == 0 ){
	   	  		        	 troubles.append("PRODUCT CANNOT BE RESOLVED TXT EIP format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n" +  "SHOPCODE " + kood + " by buyer\n" 
	   	  		        			 + "\tLineNum: "  +  (counter+1) + " EAN:" + ean  + " amount: " + kogus + "\n\n");
	   	  		        	 
	   	  		        } 
	   	  	         prod = " ";
	   	  	       
	   	  	       }
	   	  	         
	   	  	          String doknr = null;
	   	  	          if(reference != null && reference.trim().length()>0){
	   	  	        	  doknr = reference;
	   	  	          }
	   	  	          orders.add(firma, prod, " " ,kogus, doknr, null, 0, 0,datelong,"0",0
	   	  	        		  ,0,false,"",91,yhik,null,"",ean,null,true,null,null);
	   	  	        counter++;
		    	  
	    	  }
	      }
	      
	      String nameCu = "";
            if(firma == null){
         	   nameCu = tellijaKood;
         	   orders.setCompName(nameCu);
            }
	           orders.setInfo(reference);
	           orders.setEmailBody(info);
	        }
	    br.close();
	      fr.close();
	    
	  
	  }


  
  private void loadIKIOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String reference = null;
	    String info = null;
	    
	    
	      line = br.readLine().trim();
	  	        if (line.indexOf("sakymas") == -1 && line.indexOf("Pas") == -1  ){
	  	          return;
	  	        }
	  	      line = br.readLine().trim();
	  	      line = br.readLine().trim();
	  	      try{
	  	    	datelong = this.getCalLit(line.substring(line.indexOf(":")+1,line.indexOf(":")+12).trim()).getTime() + "";  
	  	      }catch(Exception e){
	  	    	  
	  	      }
	  	      
	  	      if(line.length()>60){
	  	    	  reference = line.substring(line.lastIndexOf(":")+1).trim();
	  	    	System.out.println("reference " + reference);
	  	      }
	  	      

	  	      line = br.readLine().trim();
	  	      line = br.readLine().trim();
	  	  
	  	    tellijaKood = line.substring(line.lastIndexOf(":")+1).trim();
	  	      
	  	     try{
		      kood = tellijaKood;
		           System.out.println("Kood: "+ kood);
		          firma = driver.identifyCustomer(kood,tellijaRegnr);
		          System.out.println("'"+firma+"'");
		        } catch (SQLException e) {}
		       
		        try{
		        if (firma == null || firma.trim().length() == 0){
		        	//proovime 7 esimese kohaga otsida vastavust Eugesta erisoov
		        	if(kood.length()>7){
		        		kood = kood.substring(0,7);
			        	  System.out.println("Kood2: "+ kood);
				          firma = driver.identifyCustomer(kood,tellijaRegnr);
				          System.out.println("2' "+firma+"'"); 	
		        	}        
			        }
		        
		        
		        if (firma == null || firma.trim().length() == 0){
		        	//proovime  esimese tyhikuga kohaga otsida vastavust L�ti variant erisoov
		        	if(kood.indexOf(" ") != -1){
		        		kood = kood.substring(0,kood.indexOf(" "));
			        	  System.out.println("Kood3: "+ kood);
				          firma = driver.identifyCustomer(kood,tellijaRegnr);
				          System.out.println("3' "+firma+"'"); 	
		        	}        
			        }
		        
		        
		        } catch (SQLException e) {}
		        if (firma == null || firma.trim().length() == 0){
		          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
		           troubles.append("Customer CANNOT BE RESOLVED TXT IKI format \n" + "Dok Nr: " + doknr + " " + reference+ "\n" +  "SHOPCODE " + kood + " by buyer\n" 
		       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
		            		       
		        }
	  	      while ((line= br.readLine()) != null){
	  	    	  if(line.startsWith("----")){
	  	    		  break;
	  	    	  }
	  	    	  if(line.indexOf("Inform")!= -1){
	  	    		info = line.substring(line.lastIndexOf("Inform")+23).trim();
	  	    		System.out.println("axxx" + info);
	  	    	  }
	  	    	  
	  	    	 if(line.indexOf("Pristatymo data:")!= -1){
	  	    		 try{
	  	    			datelong = this.getCalLit(line.substring(line.lastIndexOf("Pristatymo data:")+16).trim()).getTime() + "";  
		  	    		 System.out.println("axxxd " + datelong + " " + line.substring(line.lastIndexOf("Pristatymo data:")+16).trim());	 
	  	    		 }catch(Exception rr){
	  	    			datelong = new Date().getTime() + "";
	  	    		 }
		  	    	 
	  	    	 }
	  	    	  
	  	      }
		     
		        
	  	    line= br.readLine();

		    // System.out.println("xxx" + line);
		        int amStart = 0;
		        if(line.indexOf("Kiekis") > -1){
		        	amStart = line.indexOf("Kiekis")-3;
		        }
		        if(line.indexOf("Iep.") > -1){
		        	amStart = line.indexOf("Iep.") + 5;
		        }
		        
		        int plus = 11;
		        
		        if (driver instanceof MovexLatImportDriver){//kasti koguse loeme sisse
		        	  if(line.indexOf("Iep.") > -1){
				        	amStart = line.indexOf("Iep.") -1;
				        	plus = 7;
				        }
		        }
		      //  System.out.println(amStart + " \n" +line);
		        
		    
	  	      
	  	    line = br.readLine();
	  	    if(!line.startsWith("----")){
		    	  line = br.readLine();
		      }
	  	   // System.out.println("line" + line);
	  	    
	  	  String ol = null;
	    while( (ol = br.readLine()) != null){
	      counter++;
         
	    // System.out.println("ol" + ol);
	      if(ol.startsWith("----")){
	    	   br.close();
	   	      fr.close();
	    	  return;
	      }
	      
	   
	        String prod = ol.substring(0,14).trim();
            String ean = ol.substring(0,14).trim();
	          String toode =ol.substring(0,14).trim();
	          String name = ol.substring(14,51).trim();
	         // System.out.println(toode +"|"+ean + "|" + name);
	          double kogus = Double.parseDouble(ol.substring(amStart,amStart+plus).trim());
		       System.out.println(firma+"'"+prod+"'"+kogus);
	          try{
	          prod = driver.identifyProduct(ean,toode);
	          //System.out.println(toode +"  "+ prod);
	       } catch (SQLException e) {
	       e.printStackTrace();	
	       }
	
	          try{
	        	  if  (driver instanceof SkaiteEugImportDriver ){
	        		  String   prod2 = driver.getKodirovka(ean, 93);  
	     		       if(prod2 != null){
	     		    	   prod = prod2;
	     		       }  
	        	  }
		          System.out.println(toode +" kodirovka "+ prod + " ean " + ean);
		       } catch (Exception e) {
		       e.printStackTrace();	
		       }
		
	       if (prod == null || prod.equals(toode)){
	         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");

		       if(prod == null || prod.trim().length() == 0 ){
		        	 troubles.append("PRODUCT CANNOT BE RESOLVED TXT IKI format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n" +  "SHOPCODE " + kood + " by buyer\n" 
		        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
		        	 
		        } 
	         prod = " ";
	       
	       }
	          
	          String doknr = null;
	          if(reference != null && reference.trim().length()>0){
	        	  doknr = reference;
	          }
	          orders.add(firma, prod, name ,kogus, doknr, null, 0, 0,datelong,"0",0,0,false,"",93," ",null,"",ean,null,true,null,null);
	         // orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit);
	          String nameCu = "";
              if(firma == null){
           	   nameCu = tellijaKood;
           	  orders.setCompName(nameCu);
              }
	          orders.setInfo(reference);
	          orders.setEmailBody(info);
	        
	          
	        }
	    br.close();
 	      fr.close();
	    
 	  
	  }

  
  private void loadPRIOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String reference = null;
	    String info = null;
	    
	    
	      line = br.readLine().trim();
	      line = br.readLine().trim(); 
	      line = br.readLine().trim();
	  	        if (line.indexOf("Tellija Prisma") == -1  ){
	  	          return;
	  	        }
	  	      
	  	      tellijaKood = line.substring(line.indexOf("Tellija Prisma")+15, line.indexOf("Tellija Prisma")+20 ).trim();
	  	    System.out.println("tellijaKood " + tellijaKood);
	  	        line = br.readLine().trim();
	  	        line = br.readLine().trim();
	  	        if(line.trim().equals("Tellija")){
	  	        	 line = br.readLine().trim();
	  	        }
	  	      System.out.println("line " + line);
		  	    	  reference = line.substring(line.lastIndexOf("Tellimuse nr ")+13).trim();
		  	    	  System.out.println("reference " + reference);
		  	      
		  	   line = br.readLine().trim();
		  	  line = br.readLine().trim();
	  	        datelong = line.substring(line.lastIndexOf(" ")).trim();
	  	      System.out.println("datelong " + datelong);
	  	      datelong = getCalTel(datelong);
	  	    System.out.println("datelong2 " + datelong);
	  
	  	   
	  	  
	  	 
	  	      
	  	     try{
		          kood = tellijaKood;
		           System.out.println("Kood: "+ kood);
		          firma = driver.identifyCustomer(kood.toUpperCase(),tellijaRegnr); //NB siin on spetsiaalselt suured t�hed 
		          System.out.println("'"+firma+"'");
		        } catch (SQLException e) {}
		       
		       
		        if (firma == null || firma.trim().length() == 0){
		          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
		           troubles.append("Customer CANNOT BE RESOLVED TXT Telema Prisma format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
		       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
		            		       
		        }
		        
		      
		        
	  	      while ((line= br.readLine()) != null){
	  	    	 
	  	    	  if (line.indexOf("tarneaeg M�rkused") != -1){
	  	    		  break;
	  	    	  }
	  	    	  
	  	    	  if(line.indexOf("Tellimuse m�rkused")!= -1){
	  	    		info = line.substring(line.lastIndexOf("Tellimuse m�rkused")+18).trim();
		  	    //	System.out.println("axxx'" + info + "'");
	  	    	  }
	  	    	//System.out.println(line);
	  	      }
	  	      
	  	   

	  	  String ol = null;
	    while( (ol = br.readLine()) != null){
	      counter++;
       
	     System.out.println("ol len :" + ol.trim().length() + " "+ ol);
	      if(ol.trim().length() < 10){
	    	   br.close();
	   	      fr.close();
	   	   System.out.println("ol tyhi :" + ol.trim().length() + " "+ ol);
	    	  return;
	      }
	      
	   
	      
              String ean = ol.trim().substring(ol.indexOf(" ")).trim();
              String name = ean.substring(ean.indexOf(" ")).trim() ;
                name = name.substring(0,name.lastIndexOf(" ")).trim();
              ean = ean.substring(0,ean.indexOf(" "));
            
            
              String prod = ean;
	  	      String toode = ean;
	         
	          System.out.println(toode +"|"+ean + "|" + name);
	         if(ean.startsWith("00000")){
	        	 ean = ean.substring(5);
	         }
	         if(ean.startsWith("00")){
	        	 ean = ean.substring(2);
	         }
	          double kogus = Double.parseDouble(ol.trim().substring(ol.trim().lastIndexOf(" ")).trim());
		       System.out.println(firma+"'"+toode+"'"+kogus);
	         
	          try{
	          prod = driver.identifyProduct(ean,null);
	          //System.out.println(toode +"  "+ prod);
	       } catch (SQLException e) {
	       e.printStackTrace();	
	       }
	
	      

	       if (prod == null || prod.equals(toode)){
	         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");

		       if(prod == null || prod.trim().length() == 0 ){
		        	 troubles.append("PRODUCT CANNOT BE RESOLVED TXT PRI format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
		        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
		        	 
		        } 
	         prod = " ";
	       
	       }
	       
	          String doknr = null;
	          if(reference != null && reference.trim().length()>0){
	        	  doknr = reference;
	          }
	          orders.add(firma, prod, name ,kogus, doknr, null, 0, 0,datelong,"0",0,0,false,"",95," ",null,"",ean,null,true,null,null);
	         // orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit);
	          String nameCu = "";
            if(firma == null){
         	   nameCu = tellijaKood;
         	  orders.setCompName(nameCu);
            }
	          orders.setInfo(reference);
	          orders.setEmailBody(info);
	        
	          
	        }
	       br.close();
	      fr.close(); 
	  }
  
 


  private void loadLogiaEdiOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    br.mark(100000);
	    String line2 = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String reference = null;
	    String info = null;
	    
	    
	      line2 = br.readLine().trim();
	  	        if (line2.indexOf("UNB+UNOC") == -1  ){
	  	          return;
	  	        }
	  	       String linn = ""; 

	 	  	 if(line2.lastIndexOf("'") == line2.indexOf("'")){
	 	  		
	 	  		 String lll = null;
	 	  	  while( (lll = br.readLine()) != null){
	 	  		  linn = linn + lll;
	 	  	  }
	 	  	 }else {
	 	  		 linn = line2;
	 	  	 }
	 	  	
	  	     
	  	    StringTokenizer st = new StringTokenizer(linn, "'",true);
	  	    int coun = 0;
	  	    
	  	    while ( st.hasMoreTokens()) {
	  	    	coun++;
	  	    	
	  	    String	line = st.nextToken();
	  	//  System.out.println("line" +line);
	  	  	if(line.startsWith("BGM")){
	  			reference = line.substring(line.indexOf("BGM+105+")+8); 
	  			if(reference.indexOf("+") != -1)
	  			reference = reference.substring(0,reference.indexOf("+"));
	  			System.out.println("reference " + reference);
	  		}

	  	  	
	  		
	  		if(line.startsWith("NAD+BY+")){
	  			
	  			tellijaKood = line.substring(line.indexOf("NAD+BY+")+7,line.indexOf(":")); 
	  			 System.out.println("tellijaKood " + tellijaKood);
	  		     try{
	 		          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
	 		          kood = tellijaRegnr ; 
	 		          } else {
	 		          kood = tellijaKood;
	 		          }
	 		           System.out.println("Kood: "+ kood);
	 		          firma = driver.identifyCustomer(kood.toUpperCase(),tellijaRegnr); //NB siin on spetsiaalselt suured t�hed 
	 		          System.out.println("'"+firma+"'");
	 		        } catch (Exception e) {}
	 		       
	 		       
	 		        if (firma == null || firma.trim().length() == 0){
	 		          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	 		           troubles.append("Customer CANNOT BE RESOLVED EDI Logia format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	 		       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
	 		            		       
	 		        }
	  			
	  		}
	  		
	  		
	  		
	  		if(line.startsWith("DTM+4")){
	  	      datelong = line.substring(line.indexOf("+4:")+3,line.indexOf("+4:")+11);		
	  	    System.out.println("datelong " + datelong);
	  	      datelong = getCalXML(datelong); 
	  	    System.out.println("datelong2 " + datelong);
	  		}
	  	  	 	    
	 	  	      
	 	  
	 		        

	 		       if(line.startsWith("LIN")){
	 		    	  counter++;
	 		         String ol = line;
	 			     System.out.println("ol len :" + ol.trim().length() + " "+ ol);
	 			      if(ol.trim().length() < 10){
	 			    	   br.close();
	 			   	      fr.close();
	 			   	   System.out.println("ol tyhi :" + ol.trim().length() + " "+ ol);
	 			    	  return;
	 			      }
	 			      
	 			     double  kogus = 0;
	 			    String name = "" ;
	 			    
	 			     String ean = ol.substring(line.lastIndexOf("+")+1,line.lastIndexOf(":"));
	 			   
	 			   while((ol = st.nextToken()) != null){
	 				//  System.out.println("kuniks" +ol);
	 				   if(ol.startsWith("IMD")){
	 					  name = ol.substring(0,ol.lastIndexOf(":")).trim();
	 					  name = name.substring(name.lastIndexOf(":")+1).trim(); 
	 				   }
	 				  if(ol.startsWith("QTY")){
		 				  kogus = (Double.parseDouble(ol.substring(ol.indexOf(":")+1,ol.lastIndexOf(":")).trim().replace(',','.')));  
		 				  break;
	 				  }  
	 			   }
	 			     
	 			     
	 			     
	 			     
	 		              
    	 		          
	     		            String prod = ean;
	 			  	        String toode = ean;
	 			            System.out.println(firma+"'"+toode+"'"+kogus + "'" + name);
	 			         
	 			          try{
	 			          prod = driver.identifyProduct(ean,null);
	 			       } catch (SQLException e) {
	 			       e.printStackTrace();	
	 			       }
	 			
	 			      

	 			       if (prod == null || prod.equals(toode)){
	 			         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");

	 				       if(prod == null || prod.trim().length() == 0 ){
	 				        	 troubles.append("PRODUCT CANNOT BE RESOLVED TXT PRI format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	 				        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
	 				        	 
	 				        } 
	 			         prod = " ";
	 			       
	 			       }
	 			       
	 			          String doknr = null;
	 			          if(reference != null && reference.trim().length()>0){
	 			        	  doknr = reference;
	 			          }
	 			          orders.add(firma, prod, name ,kogus, doknr, null, 0, 0,datelong,"0",0,0,false,"",99,
	 			        		  " ",null,"",ean,null,true,null,null);
	 			         // orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit);
	 			          String nameCu = "";
	 		          if(firma == null){
	 		       	   nameCu = tellijaKood;
	 		       	  orders.setCompName(nameCu);
	 		          }
	 			          orders.setInfo(reference);
	 			          orders.setEmailBody(info);
	 			        
	 		       }
	  	    	
	  	    	
	  	    }
	  	    	
	  	

	       br.close();
	      fr.close(); 
	  }



  
  
  
  private void loadLogiusBill(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line1 = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	     String makselong= "";
	    String tellijaRegnr = null;
	    String kood = null;
	    double price = 0;
	    String reference = null;
        String doknr =  null;

	   
	      while ( !(line1 = br.readLine()).startsWith("UNS1_#")) {
	    	   counter++;
	        


	    if(line1.startsWith("RFF_1#                  Z") ){
	            doknr =  line1.substring(27,40).trim();
	            System.out.println("doknr" + doknr);
	           
	        }

	    if(line1.startsWith("DTM_#                   13 ")){
	        String kuup = line1.substring(27,62).trim();
	        makselong  = this.getCalXML(kuup);
	        System.out.println(makselong);
	    }
	    if(line1.startsWith("BGM_#                   351")){
	        reference =  line1.substring(69,80).trim();	        
	        System.out.println("reference" + reference);
	    }
//	    
	    if(line1.startsWith("DTM_#                   3")){
	        String kuup = line1.substring(27,62).trim();	       
	        datelong  = this.getCalXML(kuup);
	      System.out.println(datelong + " " + kuup);
	    }


	    if(line1.startsWith("NAD_2#                  CN")){
	        kood = line1.substring(27,44).trim(); 	

		    System.out.println("Tellija Kood: " + kood);

	        try{
	         
	           System.out.println("Kood: "+ kood);
	          firma = driver.identifyCustomer(kood,tellijaRegnr);
	          System.out.println("'"+firma+"'");
	        } catch (SQLException e) {}
	        if (firma == null || firma.trim().length() == 0){
	          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	           troubles.append("Customer CANNOT BE RESOLVED Logius Bill format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
	          //  firma = kood;
	         // break;
	        }
	      }
	    }
	      
	      
	      
	      //---------------------------
//siit algavad read	      

	        while ( (line1 = br.readLine()) != null) {
	    
	        	if(line1.startsWith("LIN_21#")){
	        		  System.out.println("line 1 " + line1 );
	        			  String toode = line1.substring(35,70).trim();
	     	           System.out.println(toode + "  toode ");	        	        	        	    
                       String name = "nimi";
                       double kogus = 0;
		        	   String ean = "";
	     	             
	     	          while ( (line1 = br.readLine())!= null && !line1.startsWith("RFF_21,25#              ")) {
	     	        	  
	     	            // 	System.out.println(line1);
	     	        	 if(line1.startsWith("PIA_21#")){
	     	                     ean = line1.substring(27,62).trim();
	     	                 System.out.println("ean" + ean);
	     	                 
	     	             }

	     	             if(line1.startsWith("PRI_21,24#")){
	     	                      price = new Double(line1.substring(28,42).trim().replaceAll(" ","")).doubleValue();
	     	                 
	     	                     price = (price/10000);
	     	                    System.out.println(price);

	     	             }
	     	         
	     	            if(line1.startsWith("QTY_21#                 12")){
	     	                 kogus = new Double(line1.substring(27,44).trim()).doubleValue();
	     	            kogus = (kogus/1000);
	     	           System.out.println("kogus" + kogus);
	     	            
	     	         }
	     	            
      
	        	   }

     	        	  String partii = "";
	     	         br.mark(100000);
	     	         while ( (line1 = br.readLine())!= null ) {  	     	          	
	     	        	 //System.out.println("rida " +line1);
	     	        	 if( line1.startsWith("LIN_21#"))
	     	        		 break;
	     	        	 br.mark(100000);
	     	           } 
	     	          
	     	           
	     	    //    System.out.println("ridaxx " +line1 + "'" + line1.substring(32, 33));
	     	     
		        		

		        		  if(line1 != null && !line1.substring(32, 33).equals(" ")){
		        			  while ( (line1 = br.readLine())!= null){
		        				  if(line1.startsWith("RFF_21,25#              ZZZ")){		        				  		
		        				  partii = line1.substring(50);
		        				  System.out.println("partii " +partii);
		        			       }
		        				//  System.out.println("rida1 " +line1);
		        				  if(line1.startsWith("LIN_21#"))
		        					  break;
		        				  br.mark(10000);
		        		    }
		        		  }

		        		  if(line1 != null && !line1.substring(32, 33).equals(" ")){
		        			  while ( (line1 = br.readLine())!= null){
		        				  if(line1.startsWith("RFF_21,25#              ZZZ")){		        				  		
		        				  partii = line1.substring(50);
		        				  System.out.println("partii2 " +partii);
		        			       }
		        				//  System.out.println("rida2 " +line1);
		        				  if(line1.startsWith("LIN_21#"))
		        					  break;
		        				  br.mark(10000);
		        		    }
		        		  }
		        		  br.reset();
		     	      
	     		      System.out.println(toode + " " + partii + " " + kogus + " " + reference)     ;		          
	     		          orders.add(firma, toode, name ,kogus, doknr, reference, 0, 0,datelong,"0",price,
	     		        		  0,false,"",99," ",null,partii,ean,null,true,null,null);	    
	     		          orders.setInfo(reference);
	     	     
	     	          
	        	   }

  
	        }
	      
	    
	    br.close();
	    fr.close();
	  }

  
  private void loadCSVBill(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    double price = 0;
	    String reference = null;
	    while( (line = br.readLine()) != null){
	      counter++;
	      line = line.trim();
	System.out.println(line + " " + counter);
	      if (counter == 1){
	        if (!line.equals("saateleht")){
	          break;
	        }
	      } 
	       if (line.indexOf("tellija_kood:") > -1){
	        List list = parseLine(line, ",");
	        tellijaKood = (String)list.get(1);
	           System.out.println("Tellija Kood: "+tellijaKood);

	        try{
	          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
	          kood = tellijaRegnr ; } else {kood = tellijaKood;}
	           System.out.println("Kood: "+ kood);
	          firma = driver.identifyCustomer(kood,tellijaRegnr);
	          System.out.println("'"+firma+"'");
	        } catch (SQLException e) {}
	        if (firma == null || firma.trim().length() == 0){
	          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	           troubles.append("Customer CANNOT BE RESOLVED CSV format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
	            
	         // break;
	        }
	      } else if (line.startsWith("nr:")){
	      	 List list0 = parseLine(line, ",");
	      	 System.out.println(list0.toString());
	      	reference = (String)list0.get(1);
	      } else if (line.indexOf("tarne_kuupaev:") > -1){
	     	 List list0 = parseLine(line, ",");
	     	  datelong = (String)list0.get(1);
	      } else if(counter > 15){
	        String ol = null;
	        String prod = " ";
	        while( (ol = br.readLine()) != null){
	          System.out.println(ol);

	          if(ol.startsWith("read")){
	          	ol = br.readLine();
	          }
	          if(ol.startsWith("toote_kood")){
		          	ol = br.readLine();
		          }
	          if(ol.startsWith(",")){
	          	ol = " " + ol;
	          }
	          ol = ol.replaceAll(",",", ");
	        	List row = parseLine(ol, ",");
	          String toode = ((String)row.get(0)).trim();
	          String name = ((String)row.get(1)).trim();
	          System.out.println(toode + "  toode ");
	          String kog = ((String)row.get(3)).trim().replaceAll("�", "");
	          kog = makeNumeric(kog);
	          
	          String pri = ((String)row.get(5)).trim().replaceAll("�", "");
	          pri = makeNumeric(pri);
	          System.out.println(firma + "'" + prod + "'" + kog + "'" + pri);
	          double kogus = Double.parseDouble(kog);
	          price = Double.parseDouble(pri);
		       System.out.println(firma+"'"+prod+"'"+kogus + "'" + price);
		       String partii = ((String)row.get(9)).trim();
		       
	          //toote_kood,nimetus,yhik,kogus,km,yhiku_hind,ean_kood,summa,yhiku_jaehind,partii
	          //299,"Tulemasin Form mehhaaniline",t�kk,250.00,18,1.49,,372.50,,
	          try{
	          prod = driver.identifyProduct(null,toode);
	          System.out.println(toode +"  "+ prod + " " + partii);
	       } catch (SQLException e) {
	       e.printStackTrace();	
	       }

	       if (prod == null){	       
	         prod = " ";
	         if(prod.trim().length() == 0 ){
	        	 troubles.append("PRODUCT CANNOT BE RESOLVED CSV format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	        			 + "\tLineNum: "  +  (counter+1) + " " + name + " Code:" + toode + " amount: " + kogus + "\n\n");
	            }
	       }

	          if(!datelong.equals("")){
	          		datelong = getCalXML(datelong);
	          }
	          
	          orders.add(firma, prod, name ,kogus, null, null, 0, 0,datelong,"0",price,0,false,"",99," ",null,
	        		  partii,null,null,true,null,null);
	         // orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit);
	          
	          orders.setInfo(reference);
	      
	          
	        }
	      }
	    }
	    br.close();
	    fr.close();
	  }

  
  
  
  public  String makeNumeric(String s){
	  String ret = "";
      for (int i = 0 ; i < s.length(); i++){
        if (isNumeric(s.charAt(i))){
         ret = ret + s.charAt(i);
        }
      }
      return ret;
    }

    public static boolean isNumeric(char ch)
    {
        return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '.' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
    }

	  
    public  boolean isNumeric(String s){
  	  boolean ret = true;
        for (int i = 0 ; i < s.length(); i++){
          if (!isNumeric(s.charAt(i))){
            return false;
          }
        }
        return ret;
      }
  
  private void loadCSVKMPOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    FileReader fr = new FileReader(f);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    int counter = 0;
    String tellijaKood = null;
     firma = "";
    String datelong = "";
    String kood = null;
    String reference = null;
    br.readLine();
    while( (line = br.readLine()) != null){
      counter++;
      line = line.trim().replaceAll(";", " ;");
      
      System.out.println(line + " " + counter);
   
      List list0 = parseLine(line, ";");
      if(counter == 1){
     	  datelong = ((String)list0.get(1)).trim();
          System.out.println("date " + datelong);
          doknr = ((String)list0.get(4)).trim();
           try{
        	   kood =  ((String)list0.get(2)).trim();
              if(kood.length() == 1){
            	  kood = "00" + kood;
              }else if(kood.length() == 2){
            	  kood = "0" + kood;
              }
              kood = "KPM" + kood;
             firma = driver.identifyCustomer(kood,null);
         //     firma = "aaa";
             System.out.println("'"+firma+"'");        
           } catch (Exception e) {}
           
           if (firma == null || firma.trim().length() == 0){
              addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
              troubles.append("Customer CANNOT BE RESOLVED CSVKPM ORDER format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
          				 + "\t\tCode:" + tellijaKood + "\n\n");             
                break;
           }
           
      }
           
      

     // Data;Pristatymo data;Padalinys;Adresas;U�sakymo nr;Barkodas;Kodas;Prek�;Vnt;Kiekis

          String prod = null;
          String ean = ((String)list0.get(5)).trim();
          String toode = ((String)list0.get(6)).trim();
          String name = ((String)list0.get(7)).trim();
          String unit = ((String)list0.get(8)).trim();
          double kogus = Double.parseDouble(((String)list0.get(9)).trim());
          System.out.println(ean +"  "+name + " " + toode + " " + unit + " "+ kogus);

          try{
         prod = driver.identifyProduct(ean,toode);
          System.out.println(ean + " " +toode +"  "+ prod);
       } catch (Exception e) {
       e.printStackTrace();	
       }
      
       try{
    	   if  (driver instanceof SkaiteEugImportDriver ){
    		   String   prod2 = driver.getKodirovka(ean, 88);  
 		       if(prod2 != null){
 		    	   prod = prod2;
 		       } 
        	  }
  	          System.out.println(" kodirovka "+ prod + " ean " + ean);
  	       } catch (Exception e) {
  	       e.printStackTrace();	
  	       }
       
       System.out.println(firma+"'"+prod+"'"+kogus + "'" +  reference);
      
       if (prod == null){
         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
         //break;
         
         prod = " ";
         if(prod.trim().length() == 0 ){
        	 troubles.append("PRODUCT CANNOT BE RESOLVED CSVKPM format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
        }
       }

          if(!datelong.equals("")){
          		datelong = getCalLit(datelong).getTime() +"";
          }
          
          orders.add(firma, prod, name ,kogus, reference, "", 0, 0,datelong,"",0,0,false,"",88,unit,"","",ean,null,true,null,null);
          orders.setInfo(reference);   
    }
    br.close();
    fr.close();
  }
  
  private void loadCSVOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String ladu = null;
	    String time = null;
	    double summa = 0;
	    double summakm = 0;
	    String makseviis = null;
	   boolean isBill = false;
	    String reference = null;
	    String teostaja = "";
	    String valuuta = null;
	    double tagastus = 1;
	    while( (line = br.readLine()) != null){
	      counter++;
	      line = line.trim();
	System.out.println(line + " " + counter);
	      if (counter == 1){
	        if (line.equals("tellimus") || line.equals("kassamyyk") || line.equals("mmttellimus") || line.contains("order")){        
	        }else{
	        	 break;
	        }
	       
	        if (line.equals("kassamyyk")){
	           isBill = true;
	          }
	      } 
	      
	      if (counter == 2){
		        if (line.contains("tagastus")){ 
		        	tagastus = -1;
		        }
		       
		      } 
	      
	      if (line.indexOf("tellija.regnr:") > -1 || line.indexOf("client.regnr:") > -1){
	    	  System.out.println(line + "222 " + counter);
	        List list0 = parseLine(line, ",");
	        tellijaRegnr = (String)list0.get(1);
	        System.out.println("Regnr: "+tellijaRegnr);
	       /* 	4
	     
	        kuupaev:	20101107
	        summa_km:	867
	        summa:	722.5*/

	      } else if (line.indexOf("sihtladu:") > -1){
	       	 List list0 = parseLine(line, ",");
	       	ladu = ((String)list0.get(1)).trim();
	       }  else if (line.indexOf("makseviis:") > -1){
	         	 List list0 = parseLine(line, ",");
	            	makseviis = ((String)list0.get(1)).trim();
	            } else if (line.indexOf("arve_klient:") > -1){
	           List list = parseLine(line, ",");
	           tellijaKood = (String)list.get(1);
	              System.out.println("ArveKlient: "+tellijaKood);

	           try{
	             kood = tellijaKood;
	             firma = driver.identifyCustomer(kood,null);
	             System.out.println("'"+firma+"'");
	         
	           } catch (Exception e) {}
	           if (firma == null || firma.trim().length() == 0){
	             addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED CSV BILL format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	          				 + "\t\tCode:" + tellijaKood + "\n\n");             
	             break;
	           }
	         }else if (line.indexOf("tellija_kood:") > -1  ){
	 	        List list = parseLine(line, ",");
		        tellijaKood = (String)list.get(1);
		           System.out.println("Tellija Kood: "+tellijaKood);

		        try{
		          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
		          kood = tellijaRegnr ; } else {kood = tellijaKood;}
		           System.out.println("Kood: "+ kood);
		          firma = driver.identifyCustomer(kood,tellijaRegnr);
		         // firma = "qqqq";
		           System.out.println("'"+firma+"'");
		        } catch (Exception e) {}
		        if (firma == null || firma.trim().length() == 0){
		          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
		           troubles.append("Customer CANNOT BE RESOLVED CSV format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
		       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
		            
		          break;
		        }
		      }else if (line.indexOf("filiaal:") > -1 || line.indexOf("branch:") > -1  ){
	        List list = parseLine(line, ",");
	        tellijaKood = (String)list.get(1);
	           System.out.println("Tellija Kood: "+tellijaKood);

	        try{
	          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
	          kood = tellijaRegnr ; } else {kood = tellijaKood;}
	           System.out.println("Kood: "+ kood);
	          firma = driver.identifyCustomer(kood,tellijaRegnr);
	         // firma = "qqqq";
	           System.out.println("'"+firma+"'");
	        } catch (Exception e) {}
	        if (firma == null || firma.trim().length() == 0){
	          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	           troubles.append("Customer CANNOT BE RESOLVED CSV format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
	            
	          break;
	        }
	      } else if (line.indexOf("valuuta:") > -1){
	       	 List list0 = parseLine(line, ",");
	       	  valuuta = (String)list0.get(1);
	       	// double kogus = Double.parseDouble((String)row.get(4));
	      } else if (line.startsWith("nr:")  || line.startsWith("order.nr:") ){
	    	 // System.out.println("lll "+ line);
	      	 List list0 = parseLine(line, ",");
	      	  reference = (String)list0.get(1);
	      	// double kogus = Double.parseDouble((String)row.get(4));
	      } else if (line.indexOf("summa:") > -1){
	      	 List list0 = parseLine(line, ",");
	    	  summa = Double.parseDouble((String)list0.get(1));
	      } else if (line.indexOf("summakm:") > -1){
	      	 List list0 = parseLine(line, ",");
	   	      summakm = Double.parseDouble((String)list0.get(1));
	      } else if (line.indexOf("teostaja:") > -1){
	     	 List list0 = parseLine(line, ",");
	      	  teostaja = ((String)list0.get(1));
	      } else if (line.indexOf("tarne_kuupaev:") > -1 || line.indexOf("delivery.date:") > -1){
	     	 List list0 = parseLine(line, ",");
	     	  datelong = (String)list0.get(1);
	      }else if (line.indexOf("tarne_aeg:") > -1){
		     	 List list0 = parseLine(line, ",");
		     	  time = (String)list0.get(1);
		      } else if(counter > 10){
	        String ol = null;
	        String prod = " ";
	        while( (ol = br.readLine()) != null){
	        	//System.out.println( "xxx  "+ ol );
	         if(ol.trim().length() > 6){
	        	 if(ol.startsWith("koostaja")){
	 	          	ol = br.readLine();
	 	          }

	          if(ol.startsWith("read")){
	          	ol = br.readLine();
	          }
	          if(ol.startsWith("ean")){
	          	ol = br.readLine();
	          }

	         

	          if(ol.startsWith("toote_kood")){
	          	ol = br.readLine();
	          }
	          if(ol.startsWith(",")){
	          	ol = " " + ol;
	          }
	          ol = ol.replaceAll(",,", ", ,");
	          
	         // System.out.println(ol);
	       //   ean toote_kood	nimetus	yhik	kogus	km	yhiku_hind		summa	kmsum	partii
	        
	        	List row = parseLine(ol, ",");
	           String ean = ((String)row.get(0)).trim();
	          String toode = (String)row.get(1);
	          String name = (String)row.get(2);
	       //   System.out.println(toode +"  "+ean);

	          try{
	          prod = driver.identifyProduct(ean,toode);
	        //  System.out.println(ean + " " +toode +"  "+ prod);
	       } catch (SQLException e) {
	       e.printStackTrace();	
	       }
	       //ean,toote_kood,nimetus,yhik,kogus,km,yhiku_hind,summa,kmsum,partii
	       double kogus = 0;
	       try{
	    	  kogus = Double.parseDouble((String)row.get(4));
	       }catch(Exception e){
	    	  kogus = Double.parseDouble((String)row.get(3));
	       }
	     
	      
	       String unit = " ";
	       int vat = 20;
	       double price = 0;
	       double ridasumma = 0;
	       double ridakmsumma = 0;
	         if(isBill){
	        	 unit = (String)row.get(3);
	        	 vat = (int)Double.parseDouble((String)row.get(5));
	        	 price = Double.parseDouble((String)row.get(6));
	        	 ridasumma = Double.parseDouble((String)row.get(8));
	        	 ridakmsumma = Double.parseDouble((String)row.get(9));
	         }
	       if (prod == null){
	         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	         //break;
	         
	         prod = " ";
	         if(prod.trim().length() == 0 ){
	        	 troubles.append("PRODUCT CANNOT BE RESOLVED CSV format : \n" + "Dok Nr: " + doknr + " " + reference+ " CustomerCode: "  + firma +" ImportCode: " + tellijaKood + " Custname: " + getSelvNimi(tellijaKood) + "\n"
	        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
	        }
	       }

	          if(!datelong.equals("")){
	          		datelong = getCalXML(datelong);
	          }
	     
	          orders.add(firma, prod, name ,kogus * tagastus, reference, makseviis, ridasumma, ridakmsumma,datelong,
	        		  vat + "",price,0,isBill,teostaja,99,unit,ladu,"",ean,time,true,null,null);
	         // orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit);
	          orders.setSumma(summa);
	          orders.setKmsumma(summakm);
	          orders.setInfo(reference);   
	          orders.setCurrency(valuuta);
	          
	          System.out.println(firma+"'"+prod+"'"+kogus + "'" +  reference);
	          
	         }
	        }
	      }
	    }
	    br.close();
	    fr.close();
	    //System.out.println(troubles.toString());
	  //  System.exit(0);  
	  }
	  
  
  private void loadCSVKappOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	  
	   FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	         firma = "";
	    String datelong = "";
	    String ladu = null;
	    String teostaja = "";
	    
	    while( (line = br.readLine()) != null){
	      counter++;
	      line = line.trim();
	      System.out.println(line + " " + counter);
	     if(line.length() > 10){
	    	 line = line.replaceAll("\t", " \t");
	           List row = parseLine(line, "\t");
	           teostaja = ((String)row.get(3)).trim();
	           tellijaKood = ((String)row.get(4)).trim();
	           
	           ladu = tellijaKood;
	              System.out.println("Ladu Kust tehing tuleb : " + tellijaKood);
	              if(parent.getProps().getProperty("kapp." + tellijaKood) != null){
	            	  tellijaKood = parent.getProps().getProperty("kapp." + tellijaKood);
	              }

	           firma = tellijaKood;
	            double kordaja = 1;
	            if(((String)row.get(2)).trim().equals("Tagastamine")){
	            	kordaja = -1;
	            }
	         datelong = ((String)row.get(1)).trim();
	          String ean = ((String)row.get(5)).trim();
	          String toode = ((String)row.get(6)).trim();
	          String name = ((String)row.get(7)).trim();
	          String prod = toode;
	          
	          System.out.println(toode +"  "+ean);
	          if(toode.trim().length() == 0){

		          try{
		            prod = driver.identifyProduct(ean,null);
		         } catch (SQLException e) {
		            e.printStackTrace();	
		         }
		          
	          }
	          
   
	       
	       double kogus = 0;
	       try{
	    	  kogus = Double.parseDouble(((String)row.get(8)).trim());
	       }catch(Exception e){}
	       
	       if(!datelong.equals("")){
         		datelong = getCalLitTime(datelong).getTime() + "";
           }
	       System.out.println(firma+"'"+prod+"'"+kogus*kordaja + "'" + datelong );
	       String unit = " ";
	       int vat = 20;
	       
	  //  System.exit(0);
	        
	      
	orders.add(firma, prod, name ,kogus*kordaja , null, null, 0, 0,datelong,
	        		  vat + "",0,0,false,teostaja,100,unit,ladu,"",ean,null,true,teostaja,null);
	      }
	     }
	    
	    br.close();
	    fr.close();
	  }
	  

  private void loadDomBill(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    String date = null;
	    String paydate = null;
	    String cred = "";
	    String comment = "";
	    double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "invoices");
	  	    
        Node root2 = getNode(root, "invoice");
	    String dokNr  = root2.getAttributes().getNamedItem("reference").getNodeValue();
	    kogusumma = Double.parseDouble(getNode(root2, "checksum").getFirstChild().getNodeValue());
	    String reference = getNode(root2, "order").getFirstChild().getNodeValue();
	    String delivery = null;
	    try{
		    delivery = getNode(root2, "delivery-date").getFirstChild().getNodeValue();
		   delivery = delivery.substring(0,10);
	   }catch(Exception ee ){
		   
	   }
	  
	    try{
		    date = getNode(root2, "date").getFirstChild().getNodeValue();
		   date = date.substring(0,10);
		  
		    	String xxx = date;
		        if(!date.equals("")){
		      		date = getCalHTM(date);
		        }	
		    	
		    paydate = "";  //siia maksekuup�ev teha 
 		    if(!paydate.equals("")){
		    	 System.out.println("p�ev " + paydate);
		  		paydays = getPaydays(xxx,paydate);
		  		 System.out.println("p�evi " + paydays);
		    }
	   }catch(Exception ee ){
		   
	   }
	 
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	firma =  root.getAttributes().getNamedItem("supplier").getNodeValue();
	    	firma = driver.identifyCustomer(firma,regnr);
	    }catch (Exception e){    
	    }
	   	          
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED DOMINANTE XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }
	    	
	    	
	    	Node read = getNode(root2, "details");
	    	
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("line")){
	      
	        	  System.out.println("ref "+ dokNr + " " + delivery + " " + firma +
	        			  " " + date + " " + reference + " " + kogusumma);
	   		
	              
	           	toode = getNode(rida, "code").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName ="";
	         
	          	double  kogus = Double.parseDouble(getNode(rida, "quantity").getFirstChild().getNodeValue()) ;
	          	 System.out.println("toode "+toode + " " +  kogus);
	            ean = toode;
	            try{
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED DOMINANTE XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }

	               double price = Double.parseDouble(getNode(rida, "price").getFirstChild().getNodeValue());
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	               String batch = "";
	               if(getNode(rida, "excise") != null){
		               batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
	               }
	               
	               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch);
	               if(!firma.equals("")){
	             	
	               orders.setInfo(reference);   
	               orders.setSumma(kogusumma);
	               orders.setKmsumma(kmsumma);
	               orders.setShipMode(cred);
	               orders.setKm(km);
	               orders.setYm(ymmardus);
	               orders.setEmailBody(comment);       
	               orders.add(firma, prod, prodName ,kogus, dokNr, null, 0, disc,date,"0",
	            		   price,paydays,false,"",99,unit,cred,batch,ean,null,true,null,null);         
	               counter++;
	               }
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
			loadDomOrder( orders,  f, troubles);
			e1.printStackTrace();
		}
	       }
  
  
  private void loadDomOrder(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    String date = null;
	    String paydate = null;
	    String cred = "";
	    String comment = "";
	   double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	  	    
      Node root2 = getNode(doc, "order");
	    String dokNr  = root2.getAttributes().getNamedItem("OrderId").getNodeValue();
	  
	    String reference = getNode(root2, "CustomerRef").getFirstChild().getNodeValue();
	    String type = getNode(root2, "DeliveryType").getFirstChild().getNodeValue();
	    if(type == null){
	    	type = "";
	    }else{
	    	if(type.equals("C")){
	    		type =   "creditdocument";
	    	}
	    }
	    
	    String delivery = null;
	    try{
		    delivery = getNode(root2, "DeliveryDate").getFirstChild().getNodeValue();
		   delivery = delivery.substring(0,10);
	   }catch(Exception ee ){
		   
	   }
	  
	    try{
		    date = getNode(root2, "CreationDate").getFirstChild().getNodeValue();
		   date = date.substring(0,10);
		  
		    	String xxx = date;
		        if(!date.equals("")){
		      		date = getCalHTM(date);
		        }	
		    	
		    paydate = "";  //siia maksekuup�ev teha 
		    
	   }catch(Exception ee ){
		   
	   }
	 
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	    String prod = null;
	    String ean = null;
	    String toode = null;
	    String regnr = null;
	    int counter = 0;
	    try{
	    	firma =  getNode(root2, "Customer").getFirstChild().getNodeValue();
	    	firma = driver.identifyCustomer(firma,regnr);
	    }catch (Exception e){    
	    }
	   	          
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") + " " + reference + " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED DOMINANTE XML format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }
	    	
	    	
	    	Node read = getNode(root2, "details");
	    	
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("line")){
	      
	        	  System.out.println("ref "+ dokNr + " " + delivery + " " + firma +
	        			  " " + date + " " + reference + " " + kogusumma);
	   		
	              
	           	toode = getNode(rida, "code").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName ="";
	         
	          	double  kogus = Double.parseDouble(getNode(rida, "quantity").getFirstChild().getNodeValue()) ;
	          	 System.out.println("toode "+toode + " " +  kogus);
	            ean = toode;
	            try{
	            prod = driver.identifyProduct(ean,toode);
	                  } catch (SQLException e) {}
	                  if (prod == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	prod = " ";
	                	if(prod.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED DOMINANTE XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }

	               double price = Double.parseDouble(getNode(rida, "price").getFirstChild().getNodeValue());
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	               String batch = "";
	               if(getNode(rida, "excise") != null){
		               batch = getNode(rida, "excise").getFirstChild().getNodeValue(); 
	               }
	               
	               System.out.println(firma+"'"+prod+"'"+kogus + "'" +price + "'" + batch  + "'" + type);
	               if(!firma.equals("")){
	             	
	               orders.setInfo(reference);   
	               orders.setSumma(kogusumma);
	               orders.setKmsumma(kmsumma);
	              
	               orders.setKm(km);
	               orders.setYm(ymmardus);
	               orders.setEmailBody(comment);     
	               orders.setShipMode(type);
	             
	               orders.add(firma, prod, prodName ,kogus, dokNr, null, 0, disc,date,"0",price,paydays,false,"",99,
	            		   unit,cred,batch,ean,null,true,null,null);         
	               counter++;
	               }
	            
	            prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }


  
  public String getSelvNimi(String fil) {
		if (fil.equals("410")) {
			return "Punane Selver";
		}
		if (fil.equals("440")) {
			return "Kadaka Selver";
		}
		if (fil.equals("774")) {
			return "Kotka Selver";
		}
		if (fil.equals("591")) {
			return "Peetri Selver";
		}
		if (fil.equals("599")) {
			return "Kolde Selver";
		}
		if (fil.equals("897")) {
			return "Pärnu Delice";
		}
		if (fil.equals("743")) {
			return "Priisle Selver";
		}
		if (fil.equals("4402")) {
			return "Kadaka Selver Teine korrus";
		}
		if (fil.equals("450")) {
			return "Pirita Selver";
		}
		if (fil.equals("460")) {
			return "Järve Selver";
		}
		if (fil.equals("480")) {
			return "Tondi Selver";
		}
		if (fil.equals("490")) {
			return "Merimetsa Selver";
		}
		if (fil.equals("500")) {
			return "Mai Pärnu Selver";
		}
		if (fil.equals("505")) {
			return "Viljandi Selver";
		}
		if (fil.equals("515")) {
			return "Torupilli Selver";
		}
		if (fil.equals("510")) {
			return "Krooni Rakvere Selver";
		}
		if (fil.equals("520")) {
			return "Saaremaa Kuressaare Selver";
		}

		if (fil.equals("525")) {
			return "Võru Vilja Selver";
		}
		if (fil.equals("565")) {
			return "Tartu Veeriku Selver";
		}
		if (fil.equals("535")) {
			return "Tartu Ringtee Selver";
		}
		if (fil.equals("550")) {
			return "Valga Raja Selver";
		}
		if (fil.equals("530")) {
			return "Tartu Anne Selver";
		}
		if (fil.equals("555")) {
			return "Pelgulinna Selver";
		}
		if (fil.equals("560")) {
			return "Mustakivi Selver";
		}
		if (fil.equals("545")) {
			return "Pärnu Selver Suurej�e";
		}
		if (fil.equals("540")) {
			return "Tartu S�bra Selver";
		}
		if (fil.equals("570")) {
			return "Jõhvi Selver";
		}
		if (fil.equals("565")) {
			return "Tartu Veeriku Selver";
		}


		if (fil.equals("742")) {
			return "Kagukeskuse Selver";
		}
	
		if (fil.equals("577")) {
			return "Marienthali Selver";
		}
		
		if (fil.equals("576")) {
			return "Hiiumaa Selver";
		}
		if (fil.equals("578")) {
			return "P�lva Selver";
		}
		if (fil.equals("579")) {
			return "P�rnu �lej�e Selver";
		}
		if (fil.equals("580")) {
			return "Paide Selver";
		}
		if (fil.equals("581")) {
			return "Keila Selver";
		}
		if (fil.equals("582")) {
			return "Narva Soldino Selver";
		}
		if (fil.equals("583")) {
			return "Kakum�e Selver";
		}
		if (fil.equals("584")) {
			return "Haapsalu Rannarootsi Selver";
		}
		if (fil.equals("585")) {
			return "Saku Selver";
		}
		
		if (fil.equals("590")) {
			return "Centrumi Selver";
		}
		if (fil.equals("589")) {
			return "Aardla tartu Selver";
		}
		
		
		if (fil.equals("771")) {
			return "Balti Jaama Selver";
		}
		if (fil.equals("772")) {
			return "Pääsk�la Selver";
		}
		if (fil.equals("744")) {
			return "Tabasalu Selver";
		}
		
		if (fil.equals("571")) {
			return "Jaamam�isa Selver";
		}
		if (fil.equals("572")) {
			return "Jõgeva Selver";
		}
		if (fil.equals("573")) {
			return "Põltsamaa Selver";
		}
		if (fil.equals("574")) {
			return "Kerese Selver";
		}
		if (fil.equals("575")) {
			return "Kohtla-Järve Selver";
		}
		if (fil.equals("586")) {
			return "Rapla Selver";
		}
		if (fil.equals("588")) {
			return "Läänemere Selver";
		}
		if (fil.equals("595")) {
			return "Kärberi Selver";
		}
		if (fil.equals("592")) {
			return "Astri Selver";
		}
		if (fil.equals("593")) {
			return "Viimsi Selver";
		}
		if (fil.equals("594")) {
			return "Arsenali Selver";
		}
		
		if (fil.equals("596")) {
			return "Tähesaju Selver";
		}
		if (fil.equals("597")) {
			return "Maardu Selver";
		}
		

		if (fil.equals("782")) {
			return "VALDEKU SELVER ABC";
		}
		if (fil.equals("778")) {
			return "LIIVALAIA SELVER ABC";
		}
		if (fil.equals("784")) {
			return "LAULASMAA SELVER ABC";
		}
		if (fil.equals("777")) {
			return "KADRIORU SELVER ABC";
		}
		if (fil.equals("776")) {
			return "KREUTZWALDI SELVER ABC";
		}
		if (fil.equals("779")) {
			return "MARJA SELVER ABC";
		}
		if (fil.equals("780")) {
			return "RISTIKU SELVER ABC";
		}
		if (fil.equals("781")) {
			return "NÕMME SELVER ABC";
		}
		if (fil.equals("783")) {
			return "VESKITAMMI SELVER ABC";
		}
		
		if (fil.equals("785")) {
			return "ÜMERA SELVER ABC";
		}
		if (fil.equals("786")) {
			return "RAATUSE SELVER ABC";
		}
		if (fil.equals("787")) {
			return "RUKKILILLE SELVER ABC";
		}
		if (fil.equals("788")) {
			return "VANA-PÄRNU SELVER ABC";
		}
		if (fil.equals("789")) {
			return "RANNA SELVER ABC";
		}
		if (fil.equals("790")) {
			return "OJA SELVER ABC";
		}
		if (fil.equals("791")) {
			return "LAINE SELVER ABC";
		}
		if (fil.equals("895")) {
			return "VSOLARISE TOIDUPOOD";
		}
		if (fil.equals("896")) {
			return "VIIMSI DELICE";
		}
		if (fil.equals("897")) {
			return "Pärnu Keskuse Selver";
		}
		
		
		
		
		return " ";
	}

  private void loadNordBill(Orders orders, File f,StringBuffer troubles) {
	  
	    
	    String date = null;
	    String cred = "";
	    String comment = "";
	    double kogusumma = 0;
	    double kmsumma = 0;
	    double ymmardus = 0;
	    int paydays = 0;
	  
	  
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
		System.out.println(f.getAbsolutePath());
		
		FileInputStream in = getCleanInputStream(f);
		String handler = null;

	    Document doc = db.parse(in,handler);
	    
	    Node root = getNode(doc, "E-Document");
	  	Node root2 = getNode(root, "Document");
      //getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
      
	    String dokNr  = getNode(getNode(root2, "DocumentInfo"), "DocumentNum").getFirstChild().getNodeValue();
	    kogusumma = Double.parseDouble(getNode(getNode(root2, "DocumentSumGroup"), "DocumentSum").getFirstChild().getNodeValue());
	    
	   
	  
	    try{
		    date = getNode(getNode(root2, "DateInfo"), "DeliveryDateActual").getFirstChild().getNodeValue();
		    date = date.substring(0,10);  
	   }catch(Exception ee ){
		   
	   }
	 
	   try{
		  		paydays = Integer.parseInt(getNode(root2, "PaymentTerm").getFirstChild().getNodeValue());		  
	   }catch(Exception ee ){
		   
	   }
	    String fileName = f.getName();
	   
	    String firma = null;   
	     
	 
	    String regnr = null;
	    int counter = 0;
	    try{   
	    	firma =  getNode(getNode(getNode(root2,"DocumentParties" ),"DeliveryParty"), "PartyCode").getFirstChild().getNodeValue();
	    	firma = driver.identifyCustomer(firma,regnr);
	    }catch (Exception e){    
	    }
	   	          
	          System.out.println("Firma Identified " + firma );
	          
	          if (firma == null || firma.trim().length() == 0){
	              addLog(parent.getString("nocompid") +  " " + firma + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED NORDNET XML format \n" + "Dok Nr: " + doknr  + "\n"
	       				 + "\t\tRegNr:" + regnr + " Code:" + firma + "\n\n");
	            }
	    	
	    	
	    	Node read = getNode(root2, "DocumentItem");
	    	
	    	
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          Node rida = read.getChildNodes().item(i);
	          if (rida.getNodeName().equalsIgnoreCase("ItemEntry")){
	        	  
	       	    String ean = null;
	       	    String toode = null;
	       	    
	        	  String reference = "";
	        	  try{
	        		  reference = getNode(getNode(getNode(rida, "RefInfo"), "SourceDocument"),"SourceDocumentNum").getFirstChild().getNodeValue();
		  	        	  
	        	  }catch(Exception e){
	        			  
	        	  }
	        	  String custordnum = "";
	        	  try{
	        		  custordnum = getNode(rida, "CustomerRef").getFirstChild().getNodeValue();
		  	        	  
	        	  }catch(Exception e){
	        			  
	        	  }
	        	//  System.out.println("ref "+ dokNr + " " + firma +
	        		//	  " " + date + " " + reference + " " + kogusumma);
	   		
	              
	           	toode = getNode(rida, "SellerItemCode").getFirstChild().getNodeValue();//ol.getPartnerProdID();
	           
	          	String prodName ="";
	         
	          	double  kogus = Double.parseDouble(getNode(rida, "AmountDespatched").getFirstChild().getNodeValue()) ;
	         // 	 System.out.println("toode "+toode + " " +  kogus);
	            
	            try{
	         //    prod = driver.identifyProduct(ean,toode);
	                  } catch (Exception e) {}
	                  if (toode == null){
	                    addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	                   // break;
	                	toode = " ";
	                	if(toode.trim().length() == 0 ){
	                		troubles.append("PRODUCT CANNOT BE RESOLVED NORDNET XML format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	                				 + "\tLineNum: "  +  (counter+1) + " " + "\tOrderNr:" + reference +  " Code:" + toode + prodName + " amount: " + kogus + "\n\n");
	                   }
	                  }

	               double price = Double.parseDouble(getNode(rida, "ItemSum").getFirstChild().getNodeValue())/kogus;
	               double km = 0;
	               double disc = 0;
	               String unit = "";
	               String batch = "";
	               try{

		               if( getNode(getNode(getNode(rida, "ItemReserve"), "ItemReserveRecord"),"TaxStamps") != null){
			               batch =getNode(getNode(getNode(rida, "ItemReserve"), "ItemReserveRecord"),"TaxStamps").getFirstChild().getNodeValue(); 
		               }   
	               }catch(Exception ee){
	            	   
	               }
	               
	               System.out.println(firma+"'"+toode+"'"+kogus + "'" +price + "'" + batch+ "'" + reference+ "'" + batch);
	               if(!firma.equals("")){
	             	
	               orders.setInfo(reference); 
	               orders.setSumma(kogusumma);
	               orders.setKmsumma(kmsumma);
	               orders.setShipMode(cred);
	               orders.setCompName(custordnum);
	               orders.setKm(km);
	               orders.setYm(ymmardus);
	               orders.setEmailBody(comment);       
	               orders.add(firma, toode, prodName ,kogus, dokNr, null, 0, disc,date,"0",price,paydays,
	            		   false,"",99,unit,cred,batch,ean,null,true,null,null);         
	               counter++;
	               System.out.println(dokNr + "'" + reference + "' " + orders.getCompName());
	               }
	            
	           // prod = null;
	          
	       //  System.exit(0);
	          }
	         }
		} catch (Exception e1) {
		
			e1.printStackTrace();
		}
	       }
  
  private void loadViaBills(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    FileReader fr = new FileReader(f);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    int counter = 0;
    String tellijaKood = null;
     firma = "";
     String date = "";
     String paydate = "";
     String comment = "";
    String tellijaRegnr = null;
    String kood = null;
    String reference = null;
    String dokNr = null;
    int paydays = 0;
    
    line = br.readLine();
    line = line.replaceAll(",",", ");
    List list0 = parseLine(line, ",");
   
    double kogusumma = 0;
    double kmsumma = 0;
    double ymmardus = 0;
    
   //arve,"10731489","03",2006,"G 937165","Evered2","Evered2",20060217,,"",-3293.88,-592.89,-3886.75,"",0.02,"20060217",";TAGASTUS;SAATELEHT NR. 20;"
    //LIN,"580303","Martell VS 1,0l 40%","Pudel",-2.00,18,394.6800,8.00,"3219820000054",-789.36
    //arve,"10731489","45",2009,"AG000100","Altsee","Altsee",20090504,,"MT9002231",3966.40,713.95,4680.35,"",0.00,"20090511",""
    String cred = ((String)list0.get(0)).trim().replaceAll("\"","");
    tellijaRegnr = ((String)list0.get(1)).trim().replaceAll("\"","");
    tellijaKood = ((String)list0.get(5)).trim().replaceAll("\"","");
    dokNr = ((String)list0.get(4)).trim().replaceAll("\"","");
    if(dokNr.startsWith("VR")){
    	dokNr = "V" + dokNr.substring(2,dokNr.length());
    }
    if(dokNr.startsWith("VRK")){
    	dokNr = "V" + dokNr.substring(3,dokNr.length());
    }
    reference = ((String)list0.get(9)).trim().replaceAll("\""," ").trim();
    System.out.println("asi " +  ((String)list0.get(10)).trim() + " " + ((String)list0.get(7)).trim()
     + " " + ((String)list0.get(8)).trim()
      + " " + ((String)list0.get(9)).trim()
       + " " + ((String)list0.get(11)).trim() + " ��" + reference + " " + (String)list0.get(9));
    try{
    	 kogusumma = Double.parseDouble(((String)list0.get(10)).trim().replaceAll("\"",""));
    	 kmsumma = Double.parseDouble(((String)list0.get(11)).trim().replaceAll("\"",""));
    }catch(Exception e){
    	 kogusumma = Double.parseDouble(((String)list0.get(11)).trim().replaceAll("\"",""));
    	    kmsumma = Double.parseDouble(((String)list0.get(12)).trim().replaceAll("\"",""));
    }
   
    try{
    	ymmardus = Double.parseDouble(((String)list0.get(14)).trim().replaceAll("\"",""));
        	
    }catch (Exception e){
    	
    }
    try{
    	date = ((String)list0.get(7)).trim().replaceAll("\"","");
    	String xxx = date;
        if(!date.equals("")){
      		date = getCalXML(date);
        }	
    	
    paydate =  ((String)list0.get(15)).trim().replaceAll("\"","");
    if(!paydate.equals("")){
  		paydays = getPaydays(xxx,paydate);
    }}catch(Exception e){
    	
    }
    
    try{
    comment =  ((String)list0.get(16)).trim().replaceAll("\"","");
    // System.out.println("comment " + comment);
    if(comment != null && comment.indexOf("Tellimus") != -1){
    	reference = comment.substring(comment.indexOf("Tellimus") + 10).replace(';', ' ').trim();
    }
    if(comment.trim().length() < 0){
    	comment = " ";
    }}catch(Exception e){
    	
    }
    
    
    System.out.println("Hetkel " + tellijaRegnr + "'" + tellijaKood + "'" + dokNr + "'" + date + "'" + reference 
    	+ "'days " + paydays + " " + comment);
    
    try{
        if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
        kood = tellijaRegnr ; } else {kood = tellijaKood;}
         System.out.println("Kood: "+ kood);
         if(kood!= null && kood.trim().length() > 0){
        	 firma = driver.identifyCustomer(kood,tellijaRegnr);
         }
        System.out.println("'"+firma+"'");
      } catch (Exception e) {}
      if (firma == null || firma.trim().length() == 0){
        addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
        troubles.append("Customer CANNOT BE RESOLVED VIABills format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
  				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
       
      }
    
    
    while( (line = br.readLine()) != null){
      counter++;
      line = line.replaceAll(",",", ");
      line = line.trim();   
    if(line.length()>10){
    	//System.out.println(line + " " + counter);
        String prod = " ";
//LIN,"MBGSM53","Salitos Mojito, 5,0%, 0,33L + 1,0pant","Tk",24.00,20,12.9271,0.00,"4025127017966",310.25


        StringTokenizer st = new StringTokenizer(line, ",");
       st.nextToken();
       double price = 0;
       double km = 0;
       String toode = st.nextToken().trim().replaceAll("\"","");
       if("true".equals(parent.getProps().getProperty("prodgroupcodebefore"))){
    	 
    		   toode = toode.substring(2,toode.length());
    	   
       }else{
        toode = toode.substring(0,toode.length()-2);
       } 
       String name = st.nextToken().trim();
       if(!name.endsWith("\"")){
       	name =  name + st.nextToken().trim();
       } 
       if(!name.endsWith("\"")){
       	name =  name + st.nextToken().trim();
       }
       
       if(!name.endsWith("\"")){
       	name =  name + st.nextToken().trim();
       }
       if(!name.endsWith("\"")){
          	name =  name + st.nextToken().trim();
          }
       if(!name.endsWith("\"")){
          	name =  name + st.nextToken().trim();
          }

       if(!name.endsWith("\"")){
          	name =  name + st.nextToken().trim();
          }

       if(!name.endsWith("\"")){
          	name =  name + st.nextToken().trim();
          }
       name = name.trim().replaceAll("\"","");
       System.out.println(name);
       double disc = 0;
       String unit = st.nextToken().replaceAll("\"","");
       double kogus = Double.parseDouble( st.nextToken().trim().replaceAll("\"",""));
       km = Double.parseDouble( st.nextToken().trim().replaceAll("\"",""));
       st.nextToken();
       disc = Double.parseDouble( st.nextToken().trim().replaceAll("\"",""));
       
          String ean = st.nextToken().trim().replaceAll("\"","");
          double reasum = Double.parseDouble( st.nextToken().trim().replaceAll("\"",""));
          price = reasum/kogus;
          
          String batch = "";
          if(st.hasMoreTokens()){
          	 batch = st.nextToken().trim().replaceAll("\"","");
          }
         
          System.out.println(toode + "  " + ean + " " + name + " " + price + " " +batch);

          	prod = toode;
     
       if (prod == null){
         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
         prod = " ";
         if(prod.trim().length() == 0 ){
        	 troubles.append("PRODUCT CANNOT BE RESOLVED PDF format : \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
        }
       }
         
          System.out.println(firma+"'"+prod+"'"+kogus);
          if(!firma.equals("")){
        	
          orders.setInfo(reference); 
         
          orders.setSumma(kogusumma);
          orders.setKmsumma(kmsumma);
          orders.setShipMode(cred);
          orders.setKm(km);
          orders.setYm(ymmardus);
          orders.setEmailBody(comment); 
        //  orders.setInfo(comment);
          orders.add(firma, prod, name ,kogus, dokNr, null, 0, disc,date,"0",price,paydays,
        		  false,"",99,unit,cred,batch,ean,null,true,null,null);         
          }  
    }
    }
   br.close();
   fr.close();
  // System.exit(0);
  }


  
  
  

  private void loadCsvOmaBills(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	     String paymday = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String ladu = null;
	    int paydays = 0;
	    
	    String dokNr = null;
	    String time = null;
	    String custname = "";
	    double summa = 0;
	    double summakm = 0;
	    String makseviis = null;
	   boolean isBill = false;
	    String reference = null;
	    String teostaja = "";
	    String valuuta = "EUR";

        double kogusumma = 0;
        double kmsumma = 0;
        while( (line = br.readLine()) != null){
	      counter++;
	      line = line.trim();
	System.out.println(line + " " + counter);
	     
	
	   if (line.indexOf("tarnija_kood:") > -1){
	           List list = parseLine(line, ",");
	           tellijaKood = (String)list.get(1);
	              System.out.println("ArveKlient: "+tellijaKood);

	           try{
	             kood = tellijaKood;
	             firma = driver.identifyCustomer(kood,kood);
	             System.out.println("'"+firma+"'");
	         
	           } catch (Exception e) {}
	           if (firma == null || firma.trim().length() == 0){
	             addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED CSV OSTUARVE format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	          				 + "\t\tCode:" + tellijaKood + "\n\n");             
	             break;
	           }
	      } else if (line.startsWith("maksetahtaeg:")){
		      	 List list0 = parseLine(line, ",");
		      	paymday = (String)list0.get(1);
		      	if(!paymday.equals("")){
		      		paydays = getPaydays(datelong,paymday);
		      		System.out.println("SSSS paydays " + paydays);
		        }
		      } else if (line.startsWith("nr:")){
	      	 List list0 = parseLine(line, ",");
	      	  dokNr = (String)list0.get(1);
	      }  else if (line.indexOf("kuupaev:") > -1){
	     	 List list0 = parseLine(line, ",");
	     	  datelong = (String)list0.get(1);
	      } else if(counter > 16){
	        String ol = null;
	        String prod = " ";

	        
	        while( (ol = br.readLine()) != null){
	         if(ol.trim().length() > 6){
	          if(ol.startsWith("read")){
	          	ol = br.readLine();
	          }
	          if(ol.startsWith("ean")){
	          	ol = br.readLine();
	          }

	          if(ol.startsWith("toote_kood")){
	          	ol = br.readLine();
	          }

	          if(ol.startsWith(",")){
	          	ol = " " + ol;
	          }
	          ol = ol.replaceAll(",,", ", ,");
	          ol = ol.replaceAll("\"", " ");
	          
	          System.out.println("Siin " +ol);
	        
	        	List row = parseLine(ol, ",");
	           prod = (String)row.get(0);
	          String name = (String)row.get(1);
	      
	       double kogus = Double.parseDouble((String)row.get(3));
	       
	       
	        	String unit = (String)row.get(2);
	        	double vat = Double.parseDouble((String)row.get(4));
	        	double price = Double.parseDouble((String)row.get(5));
	            double reasumma = Double.parseDouble((String)row.get(7));
	        	String partii = (String)row.get(9);
	             
	        	if (prod == null){
	         addLog(parent.getString("noprodid") + "\n "  + prod + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	         //break;
	         
	         prod = " ";
	         if(prod.trim().length() == 0 ){
	        	 troubles.append("PRODUCT CANNOT BE RESOLVED CSV OSTUARVE format : \n" + "Dok Nr: " + doknr + " " + dokNr+ " CustomerCode: "  + firma +" ImportCode: " + tellijaKood + " Custname: " + custname + "\n"
	        			 + "\tLineNum: "  +  (counter+1) + " " + name  + " Code:" + prod + " amount: " + kogus + "\n\n");
	        }
	       }

	          if(!datelong.equals("")){
	          		datelong = getCalXML(datelong);
	          }
	          
	              
	          orders.setKm(vat);
	   String dokNr2 = dokNr;
	          try{
	        	  int a = Integer.parseInt(dokNr);
	        	  if (a >2000000){
	        		  dokNr2 = (a -1000000) + "";
	        	  }
	          }catch(Exception e){
	        	  
	          }
	          
	          orders.setInfo(dokNr2);
	          
	           kogusumma = kogusumma + reasumma;
	           kmsumma = kmsumma + reasumma * 0.2;
	           System.out.println(firma+"'"+prod+"'"+kogus + "'" +  reference + " - " + kmsumma + " " + kogusumma);
	    	         
	           orders.setSumma(kogusumma);
	           orders.setKmsumma(kmsumma);
	           orders.add(firma, prod, name ,kogus, dokNr, valuuta, vat, 0,datelong,"0",price,paydays,false,
		        		  f.lastModified()+ "",65,unit,"CSV OMA INVOICE format",partii,"",f.getName(),true,null,null);
		 
	          counter++;
              
	         }
	        }
	      }
	    }
	    

        
	    br.close();
	    fr.close();
	  }
	  

  
  
  
  
  private void loadCSVBills(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    FileReader fr = new FileReader(f);
	    BufferedReader br = new BufferedReader(fr);
	    String line = null;
	    int counter = 0;
	    String tellijaKood = null;
	     firma = "";
	     String datelong = "";
	    String tellijaRegnr = null;
	    String kood = null;
	    String ladu = null;

	    String dokNr = null;
	    String time = null;
	    String custname = "";
	    double summa = 0;
	    double summakm = 0;
	    String makseviis = null;
	   boolean isBill = false;
	    String reference = null;
	    String teostaja = "";
	    String valuuta = "EUR";
	    while( (line = br.readLine()) != null){
	      counter++;
	      line = line.trim();
	System.out.println(line + " " + counter);
	     
	
	   if (line.indexOf("tarnija_kood:") > -1){
	           List list = parseLine(line, ",");
	           tellijaKood = (String)list.get(1);
	              System.out.println("ArveKlient: "+tellijaKood);

	           try{
	             kood = tellijaKood;
	             firma = driver.identifyCustomer(kood,kood);
	             System.out.println("'"+firma+"'");
	         
	           } catch (Exception e) {}
	           if (firma == null || firma.trim().length() == 0){
	             addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
	              troubles.append("Customer CANNOT BE RESOLVED CSV OSTUARVE format \n" + "Dok Nr: " + doknr + " " + reference+ "\n"
	          				 + "\t\tCode:" + tellijaKood + "\n\n");             
	             break;
	           }
	      } else if (line.startsWith("tarnija.nimi:")){
		      	 List list0 = parseLine(line, ",");
		      	  custname = (String)list0.get(1);
		      } else if (line.startsWith("nr:")){
	      	 List list0 = parseLine(line, ",");
	      	  dokNr = (String)list0.get(1);
	      }  else if (line.indexOf("kuupaev:") > -1){
	     	 List list0 = parseLine(line, ",");
	     	  datelong = (String)list0.get(1);
	      } else if(counter > 16){
	        String ol = null;
	        String prod = " ";

	        
	        while( (ol = br.readLine()) != null){
	        	System.out.println( "xxx  "+ ol );
	         if(ol.trim().length() > 6){

	          if(ol.startsWith("read")){
	          	ol = br.readLine();
	          }
	          if(ol.startsWith("ean")){
	          	ol = br.readLine();
	          }

	          if(ol.startsWith("toote_kood")){
	          	ol = br.readLine();
	          }

	          if(ol.startsWith(",")){
	          	ol = " " + ol;
	          }
	          ol = ol.replaceAll(",,", ", ,");
	          
	          System.out.println(ol);
	        
	        	List row = parseLine(ol, ",");
	           String ean = ((String)row.get(1)).trim();
	          String toode = (String)row.get(0);
	          String name = (String)row.get(2);
	          System.out.println(toode +"  "+ean);
              try{
	          prod = driver.identifyProduct(ean,toode);
	          System.out.println(ean + " " +toode +"  "+ prod);
	       } catch (SQLException e) {
	       e.printStackTrace();	
	       }
	       
	       double kogus = 0;
	       String unit = "";
        	double vat = 0;
        	double price = 0;
      
	      try{
	    	   kogus = Double.parseDouble((String)row.get(4));
		        unit = (String)row.get(3);
	        	 vat = Double.parseDouble((String)row.get(6));
	        	 price = Double.parseDouble((String)row.get(5));
	      
	      }catch (Exception e){
	    	 // e.printStackTrace();
	    	  kogus = Double.parseDouble((String)row.get(5));
		        unit = (String)row.get(4);
	        	 vat = Double.parseDouble((String)row.get(7));
	        	 price = Double.parseDouble((String)row.get(6));
	      }
	  
	       System.out.println(firma+"'"+prod+"'"+kogus + "'" +  reference + "'" + price + "'" + unit );
	       
	        	
	        	if (prod == null){
	         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
	         //break;
	         
	         prod = " ";
	         if(prod.trim().length() == 0 ){
	        	 troubles.append("PRODUCT CANNOT BE RESOLVED CSV OSTUARVE format : \n" + "Dok Nr: " + doknr + " " + dokNr+ " CustomerCode: "  + firma +" ImportCode: " + tellijaKood + " Custname: " + custname + "\n"
	        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
	        }
	       }

	          if(!datelong.equals("")){
	          		datelong = getCalXML(datelong);
	          }
	          
	          orders.add(firma, prod, name ,kogus, dokNr, valuuta, vat, 0,datelong,"0",price,0,false,
	        		  f.lastModified()+ "",65,unit,"CSV Telema INVOICE format","","",f.getName(),true,null,null);
	          orders.setInfo(dokNr);   
	          orders.setKm(vat);
	          orders.setShipMode(ladu);
	          /*
	            	   orders.add(firma, prod, prodName ,kogus, dokNr, valuta, km, 0,date,"0",price,0,false,f.lastModified()+ "",
	            			   66,unit,"XML Edisoft INVOICE format","","",f.getName());
	  	             orders.setInfo(dokNr);  	            
	  	             orders.setKm(km);
	  	             orders.setShipMode(ladu);
	  	             counter++;
	               */
	          
	         }
	        }
	      }
	    }
	    br.close();
	    fr.close();
	  }
	  

  
  
  
  private void loadTelTXTOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    FileReader fr = new FileReader(f);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    int counter = 0;
    String tellijaKood = null;
    String dokNr = null;
    firma = "";
    String shipdate = " ";

    String tellijaRegnr = null;
    String kood = null;
    while( (line = br.readLine()) != null){
      line = line.trim();
      if (line.startsWith("nr:")){
      	dokNr = line.substring(3,line.length()).trim();
 
        }
       
      if (line.startsWith("tarne_kuupaev:")){
      	shipdate = getCalTel(line.substring(14,line.length()).trim());
      	this.addLog(line.substring(14,line.length()).trim());
      } 

      if (line.startsWith("tellija.regnr:")){
      	tellijaRegnr = line.substring(14,line.length()).trim();
      	        
     }
      
       if(line.startsWith("tellija_kood:")){ // siin oli enne filiaal 
            tellijaKood = line.substring(13,line.length()).trim();
            System.out.println(" Tellija kood " + tellijaKood);
        try{
          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
               kood = tellijaRegnr ; 
          } else {        	  
        	  kood = tellijaKood;
        	  }
          
          
          firma = driver.identifyCustomer(kood,tellijaRegnr);

        } catch (Exception e) {
        e.printStackTrace();
        }

        if (firma.trim().length() == 0){
          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
          troubles.append("Customer CANNOT BE RESOLVED Telema TXT format \n" + "Dok Nr: " + doknr + "\n"
    				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
         
        }
      
 
      } 
       if (line.startsWith("read")){
    	counter = 1;
    }
      
      if(counter  > 0){
        String ol = null;
        String prod = " ";
        ol = br.readLine();
        int kogusal = ol.indexOf("kogus");
        int eanal = ol.indexOf("ean_kood");
        System.out.println("koguse algus " + kogusal);
        
        ol = br.readLine();
        
        while( (ol = br.readLine()) != null){
          System.out.println(ol);     
          if(ol.startsWith("------") || ol.startsWith("====")){
			
          }else{
          	
          String ean = ol.substring(eanal,eanal + 13).trim();
          String toode = ol.substring(0,eanal).trim();
          String name = ol.substring(eanal + 13,eanal + 55).trim();
          System.out.println("toode "+toode +"  "+ean + " " + name);
          try{
          prod = driver.identifyProduct(ean,toode);
       } catch (SQLException e) {
       e.printStackTrace();	
       }

       double kogus = Double.parseDouble(ol.substring(kogusal,kogusal+7).trim().replaceAll(",",".").replaceAll(" ",""));
       if (prod == null){
         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
         //break;
         prod = " ";
         if(prod.trim().length() == 0 ){
        	 troubles.append("PRODUCT CANNOT BE RESOLVED Telema TXT format : \n" + "Dok Nr: " + doknr + "\n"
        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
        }
       }
          System.out.println(firma+"*"+ prod+"*"+ name +"*"+kogus+"*"+ dokNr  );
          orders.setInfo(dokNr);
          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",99," ",null,"",ean,null,true,null,null);
         // orders.setEmailBody(text);
        }
      }
    }
    }
    br.close();
    fr.close();
  }
  private void loadRautaKeskoTXTOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    FileReader fr = new FileReader(f);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    int counter = 0;
    String tellijaKood = null;
    String dokNr = null;
    firma = "";
    String shipdate = " ";
    String text = " ";
    String ref = null;
    while( (line = br.readLine()) != null){
      line = line.trim();
      System.out.println(line + "" + counter);
      if (line.startsWith("TELLIMUS")){
      	dokNr = line.substring(9,line.length()).trim();
        text = dokNr;
        System.out.println(text + " 666 ");
        }
       
      if (line.startsWith("KUUP�EV .:")){
      	shipdate = getCalRauta(line.substring(10,20).trim());
      	this.addLog(line.substring(10,20).trim());
      	
      } 

      if (line.startsWith("REF:")){
      	ref = " " +line.substring(5,line.length()).trim();
      	        
     }
      
       if(line.startsWith("AADRESS .:")){
            tellijaKood = line.substring(51,line.length()).trim();
            System.out.println(tellijaKood + " 7777 " );
        try{
      
          firma = driver.identifyCustomer("Rautakesko AS",null);

        } catch (Exception e) {
        e.printStackTrace();
        }

        if (firma.trim().length() == 0){
          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
          troubles.append("Customer CANNOT BE RESOLVED RautaKesko format \n" + "Dok Nr: " + doknr + "\n"
    				 + "\t\tRegNr: Code:" + tellijaKood + "\n\n");
         
          break;
        }
      
 
      } 
       if (line.startsWith("Kood")){
    	counter = 1;
    }
      
      if(counter  > 0){
        String ol = null;
        String prod = " ";
        ol = br.readLine();
        
        while( (ol = br.readLine()) != null ){
          System.out.println(ol);     
          if(ol.trim().length()>85){
		   	
         // String ean = ol.substring(11,25).trim();
          String toode = ol.substring(50,75).trim();
          String name = ol.substring(9,50).trim();
          System.out.println("toode"+toode  + " " + name);
          try{
          prod = driver.identifyProduct(toode,toode);
       } catch (SQLException e) {
       e.printStackTrace();	
       }
       double kogus = Double.parseDouble(ol.substring(77,85).trim().replaceAll(",",".").replaceAll(" ",""));
       if (prod == null){
         addLog(parent.getString("noprodid") + "\n  " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
         //break;
         prod = " ";
         if(prod.trim().length() == 0 ){
          	 troubles.append("PRODUCT CANNOT BE RESOLVED Rautakesko format : \n" + "Dok Nr: " + doknr + "\n"
          			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + toode + " amount: " + kogus + "\n\n");
        }
       }
         
          System.out.println(firma+"*"+ prod+"*"+ name +"*"+kogus+"*"+ dokNr  );
          orders.setInfo(ref);
          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",0," ",null,"",null,null,true,null,null);
         // orders.setEmailBody(text);
          }  
      }
        
    }
    }
    br.close();
    fr.close();
  }

 
  
  private void loadLevisReport(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    setEmailBody(orders,fileName);
    String dokNr = null;
    firma = "";
    String shipdate = " ";
   
	int a = 0;
    try{
    	FileInputStream ff = new FileInputStream(f);
		HSSFWorkbook workbook = new HSSFWorkbook((ff));
		HSSFSheet sheet = workbook.getSheet("Ladu");
		String type = ".inv";
		
		if(sheet == null){
			sheet = workbook.getSheet("M��k");
			type = ".sel";
		}
		
		
		for(int i = 0 ; i < sheet.getLastRowNum();i++){
			HSSFRow row = sheet.getRow(i);
			//System.out.println(row);
			if(row == null && a == 0){
				a = i;
				//System.out.println("99 " + i);
			}else{
				a = sheet.getLastRowNum();
			}
		}
		System.out.println("sees " + a);
		//Enumeration enum = sheet.getLastRowNum()
		for(int i = 1 ; i <= a;i++){
			HSSFRow row = sheet.getRow(i);
			Date cell = row.getCell((short)0).getDateCellValue() ;
	        	shipdate = cell.getTime() + "";    	
	        
	         
	        	
	        	try{
	        		 firma = (int)row.getCell((short)1).getNumericCellValue() + "" ;
			        
		        }catch (Exception e ){
		       	 firma = row.getCell((short)1).getStringCellValue() ;
		        }
	       // System.out.println( i + " " + firma);
	
	        
	        String toode =  row.getCell((short)2).getStringCellValue();
	       // System.out.println( i + " " + toode);    
	       
	        String suurus = "";
	        try{

	        	suurus =  row.getCell((short)3).getStringCellValue();	
		        }catch(Exception ee) {
		        	suurus =  (int)row.getCell((short)3).getNumericCellValue() + "";	
		        } 
	        //System.out.println( i + " " + suurus);
	        
	        String pikkus = "";
	        try{

		       pikkus =  row.getCell((short)4).getStringCellValue();	
	        }catch(Exception ee) {
	        	pikkus =  (int)row.getCell((short)4).getNumericCellValue() + "";	
	        } 
	        
	        
	        
	        double kogus =  (row.getCell((short)5).getNumericCellValue());
	        double hind = 0 ;
	        try{

		      hind =   (row.getCell((short)6).getNumericCellValue())/kogus;	
	        }catch (Exception e){
	        	
	        }
	        orders.setInfo(shipdate);
	        
	    	///System.out.println( i + " hind " + hind);
		          orders.add(firma, toode, pikkus ,kogus, dokNr, null, 0, 0,shipdate,"0",hind ,0,false,"",99,type,null,
		        		  suurus,null,null,true,null,null);
			}
			
		
		
	ff.close();
}catch(Exception e) {
	e.printStackTrace();
}

  }
  
  
  private void loadIdaXlsArve(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
        String type = null;
        
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				XSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
		
			for(int i = 1 ; i <= a;i++){
				XSSFRow row = sheet.getRow(i);
				
		    //Kui on alla 15 rea siis yhekordsed v�lja      
		        	if(i < 26){
		                 
		        		
		        	if(i == 1 ){
		        		try{
			                	 firma = driver.identifyCustomer("EDCO", "EDCO");
			            }catch (Exception e ){
				       	} 
			            } 
			       

		        	if(i == 2 ){
		        		try{
			              dokNr = row.getCell((short)4).getStringCellValue();  	
			            }catch (Exception e ){
			            	 dokNr = row.getCell((short)4).getNumericCellValue() + ""; 
				       	} 
			        
			        	try{
					        
			            XSSFRow row2 = sheet.getRow(a-1);
						
			          Date cell = row2.getCell((short)0).getDateCellValue() ;
			        	shipdate = cell.getTime() + "";    	

			          
			        	System.out.println("doknr " + dokNr + " " + shipdate );
			        	}catch (Exception e ){
				        	try{
						        
					            XSSFRow row2 = sheet.getRow(a);
								
					          Date cell = row2.getCell((short)0).getDateCellValue() ;
					        	shipdate = cell.getTime() + "";    	

					        	
					            System.out.println("doknr " + dokNr + " " + shipdate );
					        	}catch (Exception u ){
					            u.printStackTrace();
					        	}			        
			        	} 
			        } 
		        	
		        			
		        	}else{
		    
		    //    Siin juba ridade info	  
		        		try{
		        		
				        String toode = null ;
				        try{
				        toode = row.getCell((short)3).getStringCellValue().trim();
				        
				        
				        }catch (Exception e){
				        }
				        if(toode != null){
				        	
				        	
				       	 
				            
				        	

					        String nimi =  row.getCell((short)2).getStringCellValue().trim();
					        double kogus =  (row.getCell((short)8).getNumericCellValue());
					        double hind = 0 ;
					        try{
	                            hind =   (row.getCell((short)14).getNumericCellValue());	
					        }catch (Exception e){
					        }	
				        
					        
					        String origt = toode;
					        try{
				            	
					            toode = driver.identifyProduct(toode,toode);
					                  } catch (SQLException e) {}
					                  if (toode == null){
					                    addLog(parent.getString("noprodid") + "\n " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
					                  
					                    if(toode == null ){
					                		troubles.append("Puudu Toode : \n\tRea NR Arvel : "  +  (i+1) + "\n" + origt + "-" + kogus + "               "+nimi+ "\n\n");
					                     }
					                  }
					                  
				        if( toode.length() > 0 && hind > 0){
				        	System.out.println( i + " " + toode + " " + nimi + " " + kogus + " " + hind);    
						      orders.setInfo(shipdate);
				              orders.add(firma, toode, nimi ,kogus, dokNr, null, 0, 0,shipdate,"0",
				            		  hind ,0,false,"",99,type,null,null,null,null,true,null,null);
						   }
				        }
				    	
		        		}catch (Exception e){
		        			e.printStackTrace();
		        		}
		        		}
						

		        		
		        	}
		        
		    			
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}

	
	  }
	  
  
  private void loadDrogasOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	    int counter = 0;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	    int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("U�sakymas");
			
			
			for(int i = 9 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			for(int i = 0 ; i < a;i++){
				HSSFRow row = sheet.getRow(i);
			//	System.out.println(row);
			/*	if(i == 0){
					Date date = row.getCell((short)0).getDateCellValue();
					System.out.println(date);
				}*/
		        
				if(i == 4){
		        	String cell = row.getCell((short)0).getStringCellValue();
		        	dokNr = cell.substring(cell.indexOf("U�sakymas")+9,cell.length()).trim();    	
		        	System.out.println( i + " " + dokNr);
		          firma = dokNr.substring(0,3);
		          System.out.println( i + " Firma " + firma);
		        }
			
				if (i > 9 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
			        }catch (Exception e ){

				        ean = row.getCell((short)0).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				        System.out.println(ean);
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			        
			          	
			          String toode =  row.getCell((short)1).getStringCellValue();
			          String eans = ean;
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			         e.printStackTrace();	
			         }
			         double kogus =  (row.getCell((short)5).getNumericCellValue());
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			          	 troubles.append("PRODUCT CANNOT BE RESOLVED Drogas XLS format : \n" + "Dok Nr: " + doknr + "\n"
			          			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + toode + " amount: " + kogus + "\n\n");
			        
			        }
			       }
			       
					
					
			          System.out.println(i + "*"+firma+"*"+ toode+"*"+ name +"*"+kogus+"*"+ dokNr  );
			          orders.setInfo(dokNr);
			          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",99,
			        		  " ",null,"",ean,null,true,null,null);
				}
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}

	  }
  
  private void loadXLSMaxOrder(Orders orders, File f,StringBuffer troubles) throws Exception {
	    String fileName = f.getName();
	    
	    setEmailBody(orders,fileName);
		
	   
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
		String saatja = null;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);
				String cellf = "";
				if(i == 1){
					 cellf = row.getCell((short)2).getStringCellValue();
					firma = cellf;
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer("MA"+firma,null);
					if (firma == null ) {
						firma = driver.identifyCustomer(cellf,null);	
					}
					System.out.println("Firma2 " + firma + " "+ cellf);
					//System.exit(0);
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Maxima XLS format \n" + "Dok Nr: " + doknr + "\n" +  "SHOPCODE " + cellf + " by buyer\n" 
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
					
					     
					 
				}
				
				if(i == 2){
					 cellf = row.getCell((short)3).getStringCellValue();
					saatja = cellf.trim();
					System.out.println("Saatja " + saatja);	 
				}
		        
				if(i == 3){
					System.out.println(row);
		        	 String cell = "";
		        	try{
		        	  cell = 	row.getCell((short)1).getStringCellValue();
		        	}catch(Exception ee){
		        		cell = row.getCell((short)1).getNumericCellValue() + "";
		        	}
		        	dokNr = cell.trim();    
		        	//System.out.println(cell);
		        	//System.out.println( i + " " + dokNr);
		        	Date date = row.getCell((short)3).getDateCellValue();
					System.out.println("Date " + date);
					shipdate = date.getTime() + "";
				

					 if (firma == null || firma.trim().length() == 0){
					       doknr=dokNr;
					     }
					 
		        	//  format = bb.substring(bb.indexOf("SHOPCODE") + 8 , bb.toLowerCase().indexOf("by buyer") );
					   	
				}
				
				if (i > 5 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			        System.out.println("name " + row.getCell((short)3).getStringCellValue());
			        if(!row.getCell((short)3).getStringCellValue().equals("Pavadinimas")){
			        	
			        
			         try{

				        ean = row.getCell((short)2).getStringCellValue();	
				        System.out.println(ean);
			        }catch (Exception e ){

				        ean = row.getCell((short)2).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				        System.out.println(ean);
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			    
			          	
			          String toode = "";
			          try {

				        toode =   row.getCell((short)1).getStringCellValue();
			          } catch (Exception e ){
			        	 toode =  row.getCell((short)1).getNumericCellValue() + "";
			        	 toode = toode.substring(0,toode.indexOf("."));
			          }
			        	  
			        	  
			          String eans = ean;
			          String name =  row.getCell((short)3).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			         e.printStackTrace();	
			         }
			         
			         try{
			        	 if  (driver instanceof SkaiteEugImportDriver ){
			        		  String   prod2 = driver.getKodirovka(ean, 94);  
			     		       if(prod2 != null){
			     		    	   prod = prod2;
			     		       }
			        	  }
				          System.out.println(" kodirovka "+ prod + " ean " + ean);
				       } catch (Exception e) {
				       e.printStackTrace();	
				       }

						double kogus =  (row.getCell((short)4).getNumericCellValue());
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			         	 troubles.append("PRODUCT CANNOT BE RESOLVED Maxima XLS format : \n" + "Dok Nr: " + doknr + "\n"  +  "SHOPCODE " + cellf + " by buyer\n" 
			         			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + eans + " Code:" + toode + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
					
			          System.out.println(i + "*"+firma+"*"+ toode+"*"+ name +"*"+kogus+"*"+ dokNr  + "*" + shipdate );
			      //	System.exit(0);
			          orders.setInfo(dokNr);
			          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,
			        		  "",94," ",null,"",ean,null,true,null,null);
			          orders.setTeostaja(saatja);
			          counter++;
				}
			}
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
  
  private String identifyXLS(File f){
	  String result = null;
		int a = 0;
		FileInputStream ff  = null;
	    try{
	    	 ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			
			HSSFSheet sheet = workbook.getSheet("Uzsakymas");
		
			ff.close();	
			if(sheet != null){
						
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
			//System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			for(int i = 0 ; i < a;i++){
				HSSFRow row = sheet.getRow(i);				
				if(i == 0){
					String cell = row.getCell((short)0).getStringCellValue();
					firma = cell;
					//System.out.println("Firma Statoil " + firma);
					if(firma.toLowerCase().indexOf("statoil")!= -1)return "statoil";
				}			
		    	}
			}
			
			
			sheet = workbook.getSheetAt(0);
			if(sheet != null){				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 0){
						String cell = row.getCell((short)0).getStringCellValue();
						
						System.out.println("Firma Maxima LAT " + cell );
						if(cell.toLowerCase().indexOf("preču pasūtīšana")!= -1  ){
							return "maximalat";
						}
					}			
				}
			}
			
			 sheet = workbook.getSheetAt(0);
				if(sheet != null){
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
				//System.out.println("sees " + a);
				//Enumeration enum = sheet.getLastRowNum()
				try{
					for(int i = 0 ; i < a;i++){
						HSSFRow row = sheet.getRow(i);				
						if(i == 1){
							String cell = row.getCell((short)1).getStringCellValue();
							firma = cell;
							System.out.println("Firma StatoilLith " + firma);
							if(firma.toLowerCase().indexOf("statoil")!= -1)return "statoillit";
							if(firma.toLowerCase().indexOf("luktarna-l")!= -1)return "statoillit";
							if(firma.toLowerCase().indexOf("viada")!= -1)return "statoillit";
						}			
				    	}
				}catch(Exception e){
					e.printStackTrace();
				}
				
				}

			sheet = workbook.getSheet("MULTIORDER");
			if(sheet != null){
				return "MULTIORDER";
			}
			
			sheet = workbook.getSheet("CENTO");
			HSSFSheet sheet2 = workbook.getSheet("IKI");
			HSSFSheet sheet3 = workbook.getSheet("VPM");
			if(sheet != null || sheet2 != null || sheet3 != null){
				return "MULTIORDER2";
			}
			
			
			sheet = workbook.getSheetAt(0);
			
			if(sheet != null && sheet.getSheetName().indexOf("Laien") != -1){
				return "MULTIORDER3";
			}
			
			sheet = workbook.getSheet("Eesti Statoil");
			if(sheet != null){
				
				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
			//	System.out.println("sees " + a);
				//Enumeration enum = sheet.getLastRowNum()
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 1){
						String cell = null ;
						try{
							cell = row.getCell((short)1).getStringCellValue();	
						}catch (Exception ee){
							cell = row.getCell((short)2).getStringCellValue();
						}
						firma = cell;
					//	System.out.println("Firma  Esti Statoil " + firma);
						if(firma.toLowerCase().indexOf("eesti statoil")!= -1 ){
							return "eststatoil";
						}
					}
				}
			}
			
			
			
			sheet = workbook.getSheetAt(0);
			System.out.println("Firma  Esti Statoil " + firma);
			if(sheet != null){
				for(int i = 0 ; i < 7;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 3){
						try{
							String cell = row.getCell((short)0).getStringCellValue();
							System.out.println("Firma Lukoil Baltija " + cell);
							if(cell.toLowerCase().indexOf("lukoil baltija")!= -1  ){
								return "lukoil";
							}
							if(cell.toLowerCase().indexOf("viada")!= -1  ){
								return "lukoil";
							}
							
						}catch(Exception e){
							
						}
					}	
					
				
				
					if(i == 1){
						String cell = null ;
						try{
							cell = row.getCell((short)2).getStringCellValue();	
						}catch (Exception ee){try{
							cell = row.getCell((short)2).getRichStringCellValue().toString();
						}catch(Exception d){
							
						}
							
						}
						firma = cell;
							System.out.println("Firma rivona " + firma);
							if(firma != null && firma.toLowerCase().indexOf("pramon")!= -1 ){
								return "rivonax";
							}
						
					}
				}
			}
			
			
			
			sheet = workbook.getSheet("Sheet1");
			
			if(sheet != null){
				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
			//	System.out.println("sees " + a);
				//Enumeration enum = sheet.getLastRowNum()
				
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 3){
						try{
							String cell = row.getCell((short)0).getStringCellValue();
							
							System.out.println("Firma Lukoil Baltija " + cell);
							if(cell.toLowerCase().indexOf("lukoil baltija")!= -1  ){
								return "lukoil";
							}
						}catch(Exception s){
							
						}
						
					}			
				}
				
				
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 0){
						String cell = null ;
						try{
							cell = row.getCell((short)4).getStringCellValue();	
						}catch (Exception ee){
							try{
								cell = row.getCell((short)5).getStringCellValue();
										
							}catch (Exception r ){
								r.printStackTrace();
							}
							cell = row.getCell((short)5).getStringCellValue();
						}
						if(cell == null || cell.trim().length() == 0){
							cell = row.getCell((short)1).getStringCellValue();
						}
						firma = cell;
						
						System.out.println("Firma Sanitex " + firma);
						if(firma != null && firma.toLowerCase().indexOf("sanitex")!= -1){
							System.out.println("Firma Sanitex " + firma);
							return "sanitex";
						}
					}			
				}
				
				
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 0){
						String cell = null ;
						try{
							cell = row.getCell((short)3).getStringCellValue();	
						}catch (Exception ee){
							cell = row.getCell((short)2).getStringCellValue();
						}
						firma = cell;
						
						if(firma.toLowerCase().indexOf("carskoje")!= -1 || firma.toLowerCase().indexOf("charlie")!= -1 
								|| firma.toLowerCase().indexOf("manami")!= -1 	|| firma.toLowerCase().indexOf("katp")!= -1 || firma.toLowerCase().indexOf("pirki")!= -1
								 || firma.toLowerCase().indexOf("crepe")!= -1 || firma.toLowerCase().indexOf("bistro")!= -1){
							System.out.println("Firma  katpedele " + firma);
							return "katpedele";
						}
					}			
				}
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 0){
						String cell = null ;
						try{
							cell = row.getCell((short)1).getStringCellValue();	
						}catch (Exception ee){
							cell = row.getCell((short)2).getStringCellValue();
						}
						firma = cell;
						
						if(firma.toLowerCase().indexOf("eiginta")!= -1){
							System.out.println("Firma EIGINTA " + firma);
							return "eiginta";
						}
					}			
				}
				
				
			}
			sheet = workbook.getSheetAt(0);
			
			System.out.println("Sheet " + sheet);
			if(sheet != null){
				
				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
				//	System.out.println(row);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
			//	System.out.println("sees " + a);
				//Enumeration enum = sheet.getLastRowNum()
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 0){
						String cell = row.getCell((short)0).getStringCellValue();
						firma = cell;
						System.out.println("Firma Rivona " + firma);
						if(firma.toLowerCase().indexOf("preki� u�sakymas")!= -1  ){
							return "rivona";
						}
					}			
				}
				
				
			}
			sheet = workbook.getSheet("Ataskaita");
			if(sheet != null){
				return "assorti";
			}
			sheet = workbook.getSheet("U�sakymas");
			if(sheet != null){				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 3){
						String cell = row.getCell((short)2).getStringCellValue();
						
						System.out.println("Firma Maxima " + cell);
						if(cell.toLowerCase().indexOf("prist.data:")!= -1  ){
							return "maxima";
						}
					}			
				}
			}
			
			
			
			
			
			sheet = workbook.getSheetAt(0);
			
			//System.out.println("Firma Lukoil Baltija1 " + sheet.toString());
			if(sheet != null){
				//System.out.println("Firma Lukoil Baltija2 " + sheet.toString());
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					if(row == null && a == 0){
						a = i;
					}else{
						a = sheet.getLastRowNum();
					}
				}
			//	System.out.println("Firma Lukoil Baltija " );
				for(int i = 0 ; i < a;i++){
					HSSFRow row = sheet.getRow(i);				
					if(i == 3){
						String cell = row.getCell((short)0).getStringCellValue();
						
						System.out.println("Firma Lukoil Baltija " + cell);
						if(cell.toLowerCase().indexOf("lukoil baltija")!= -1  ){
							return "lukoil";
						}
					}			
				}
				
				
			}
			
	}catch(Exception e) {
		//.printStackTrace();
		System.out.println("viga v�i uus excel");
		try{
			
			
			 ff = new FileInputStream(f);
		
				XSSFWorkbook workbook = new XSSFWorkbook(ff); 
				XSSFSheet sheet = workbook.getSheetAt(0); 
				ff.close();	
				if(sheet != null){
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						XSSFRow row = sheet.getRow(i);
						//System.out.println(row);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}
					
					for(int i = 0 ; i < a;i++){
						XSSFRow row = sheet.getRow(i);	
						
					
						
						if(i == 0){
							try{
							String cell = row.getCell((short)0).getStringCellValue();						
							if(cell.toLowerCase().indexOf("orkla")!= -1  ){
								System.out.println("Firma ORKLA " + cell);
								return  "orkla";
							}
							
							if(cell.toLowerCase().indexOf("kalevi")!= -1 &&   cell.toLowerCase().indexOf("pda")!= -1){
								System.out.println("Firma KALEVEAN " + cell);
								return  "kaleveanpda";
							}
							if(cell.toLowerCase().indexOf("kalevi")!= -1  ){
								System.out.println("Firma KALEV " + cell);
								return  "kalev";
							}

							if(cell.toLowerCase().indexOf("prike")!= -1  ){
								System.out.println("Firma Prike " + cell);
								return  "prike";
							}
							}catch(Exception r){
								r.printStackTrace();
							}
							
						}
						
						
						if(i == 0){
							try{
							String cell = row.getCell((short)0).getStringCellValue();						
							if(cell.toLowerCase().indexOf("altia")!= -1  ){
								System.out.println("Firma Altia " + cell);
								return  "altia";
							}
							}catch(Exception r){
								r.printStackTrace();
							}
						}
				
							
						
						
						if(i == 0){
							String cell = null ;
							try{
								cell = row.getCell((short)4).getStringCellValue();	
							}catch (Exception ee){
								try{
									cell = row.getCell((short)5).getStringCellValue();
											
								}catch (Exception r ){
									r.printStackTrace();
								}
							}
							if(cell == null || cell.trim().length() == 0){
								cell = row.getCell((short)1).getStringCellValue();
							}
							firma = cell;
							
							System.out.println("Firma Sanitex " + firma);
							if(firma != null && firma.toLowerCase().indexOf("sanitex")!= -1){
								System.out.println("Firma Sanitex " + firma);
								return "sanitexuus";
							}
						}
						
						
						
						
						if(i == 3){
							try{
							String cell = row.getCell((short)0).getStringCellValue();						
							if(cell.toLowerCase().indexOf("lukoil baltija")!= -1  ){
								System.out.println("Firma Lukoil Baltija " + cell);
								result =  "lukoil";
							}
							}catch(Exception r){
								r.printStackTrace();
							}
						}	
						if(i == 6){
							try{
								String cell = row.getCell((short)0).getStringCellValue();
								System.out.println("Stockman  " + cell);
								if(cell.toLowerCase().indexOf("stockman")!= -1  ){
									return "stockman";
								}	
							}catch(Exception r){
								r.printStackTrace();
							}
						}
					}	
				}
			
				
				
				if(sheet != null){
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						XSSFRow row = sheet.getRow(i);
						//System.out.println(row);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}
					//System.out.println("sees " + a);
					//Enumeration enum = sheet.getLastRowNum()
					try{
						for(int i = 0 ; i < a;i++){
							XSSFRow row = sheet.getRow(i);				
							if(i == 1){
								String cell = row.getCell((short)1).getStringCellValue();
								firma = cell;
							//	System.out.println("Firma StatoilLith " + firma);
								if(firma.toLowerCase().indexOf("statoil")!= -1)return "statoillit";
							}			
					    	}
					}catch(Exception y){
						y.printStackTrace();
					}
					
					}
			
			
			
			
		}catch(Exception aa){
			aa.printStackTrace();
		//	return "";
		}
		
	}
	  
	  
	  
	System.out.println("result " + result);
	  
	  return result ;
  }
  
  
  
  private String identifyXML(File f, StringBuffer troubles){
	  String result = "telemavana";

	  
	  


	   try{
		   	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		   	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
		   	    //	System.out.println("Laima Kalevile " + f.getAbsolutePath());
		   	    	
		   	    	FileInputStream in = getCleanInputStream(f);
		   	    	String handler = null;
		   	        Document doc = db.parse(in,handler);
		   	        in.close();
		   	        Node rootmain = getNode(doc, "Data");
		   		    getNode(rootmain, "tplJournalSpec");
		   	           return "laimaaxa";
		   		     
		   		}catch(Exception e) {
		   			//e.printStackTrace();
		   			//result = null; // kui ei ole normaalne dokument
		   		}
	  
	  
	  
	  

	   try{
		   	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		   	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
		   	    	System.out.println("Gruste " + f.getAbsolutePath());
		   	    	
		   	    	FileInputStream in = getCleanInputStream(f);
		   	    	String handler = null;
		   	        Document doc = db.parse(in,handler);
		   	        in.close();
		   	        Node rootmain = getNode(doc, "Uzsakymas");
		   		    getNode(rootmain, "UzsakymoNumeris");
		   	           return "gruste";
		   		     
		   		}catch(Exception e) {
		   			//e.printStackTrace();
		   			//result = null; // kui ei ole normaalne dokument
		   		}
	  
	  
	  
	    try{
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
	    	System.out.println(f.getAbsolutePath());
	    	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        in.close();
	        Node rootmain = getNode(doc, "E-Document");
	        Node dok =   getNode(rootmain, "Document");
	        Node type = getNode(dok, "DocumentType");
	        System.out.println("vaartus " + type.getFirstChild().getNodeValue());
	         if(type.getFirstChild().getNodeValue().equals("order") || type.getFirstChild().getNodeValue().equals("recadv")
	        		 || type.getFirstChild().getNodeValue().equals("retann")
	        		 || type.getFirstChild().getNodeValue().equals("ORDER") || type.getFirstChild().getNodeValue().equals("RECADV")
	        		 ){
	        	  return "telemauus";
	 		 }
	         if(type.getFirstChild().getNodeValue().equals("desorder")){
	        	  return "telemauusdesorder";
	 		 }
	       
		}catch(Exception e) {
			//e.printStackTrace();
			if(e.toString().indexOf("SAXParseException")!= -1){
				troubles.append("BAD XML FILE FORMAT ERROR \nFile :" + f.getName() + "\n" +e.toString());
				//System.out.println(troubles.toString());
			}
		}
		
		
		  try{
	   	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
	   	    	System.out.println(f.getAbsolutePath());
	   	    	
	   	    	FileInputStream in = getCleanInputStream(f);
	   	    	String handler = null;
	   	        Document doc = db.parse(in,handler);
	   	        in.close();
	   	        Node rootmain = getNode(doc, "Document-Invoice");
	   		    getNode(rootmain, "Invoice-Header");
	   	           return "ediinv";
	   		     
	   		}catch(Exception e) {
	   			//e.printStackTrace();
	   			//result = null; // kui ei ole normaalne dokument
	   		}
		
		   try{
			   	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			   	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
			   	    	System.out.println(f.getAbsolutePath());
			   	    	
			   	    	FileInputStream in = getCleanInputStream(f);
			   	    	String handler = null;
			   	        Document doc = db.parse(in,handler);
			   	        in.close();
			   	        doc.getFirstChild();
			   	        Node rootmain = doc.getFirstChild();//getNode(doc, "Document-Order");
			   	        System.out.println(doc.getFirstChild().getNodeName());
			   	        if(doc.getFirstChild().getNodeName().equals("tellimus")){
			   	        	return "telemavana";
			   	        }
			   	     if(doc.getFirstChild().getNodeName().equals("tagastus")){
			   	        	return "telemavana";
			   	        }
			   		    getNode(rootmain, "Document-Header");
			   	           return "edisoft";
			   		     
			   		}catch(Exception e) {
			   			//e.printStackTrace();
			   		}
		

					   try{
						   	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						   	        DocumentBuilder db = dbf.newDocumentBuilder();	    	
						   	    	System.out.println(f.getAbsolutePath());
						   	    	
						   	    	FileInputStream in = getCleanInputStream(f);
						   	    	String handler = null;
						   	        Document doc = db.parse(in,handler);
						   	        in.close();
						   	        Node rootmain = getNode(doc, "Document-ReceivingAdvice");
						   		    getNode(rootmain, "ReceivingAdvice-Header");
						   	           return "edirec";
						   		     
						   		}catch(Exception e) {
						   			//e.printStackTrace();
						   			//result = null; // kui ei ole normaalne dokument
						   		}

									   				
						   	
	  return result ;
  }
  
  
  
  
  
  
  
  

  private String identifyCSV(File f){
	  String result = "CSV";
		
	    try{
	        BufferedReader in = new BufferedReader(
	                new FileReader(f));
            String fline = in.readLine() ;
            String fline2 = in.readLine() ;
            System.out.println(fline);
	           if(fline.indexOf("Data;Pris")!= -1 ){
	        	   in.close();
	        	   return "KPM";	        	   
	           }
	           
	           if(fline.indexOf("ljastamine")!= -1 ||  fline.indexOf("Tagastamine") != -1){
	        	   in.close();
	        	   return "KAPP";	        	   
	           }
	           
	           if(fline.indexOf("order")!= -1 ||  fline2.indexOf("client.regnr") != -1){
	        	   in.close();
	        	   return "CSVENG";	        	   
	           }
	           
	           
	           in.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	  return result ;
  }
  
  
  private String identifyHTM(File f){
	  String result = "telema";
		
	    try{
	        BufferedReader in = new BufferedReader(
	                new FileReader(f));
            String fline = in.readLine() ;
            System.out.println(fline);
            fline = in.readLine() ;
            fline = in.readLine() ;
	           if(fline.indexOf("Pas")!= -1 || fline.startsWith("U�sakymas") || fline.startsWith("Pas") ){
	        	   in.close();
	        	   return "RXL";	        	   
	           }
	           in.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	  return result ;
  }
  
  
  
  
  
  private String identifyPurFile(File f){
	  StringBuffer troubles = new StringBuffer();
	  String result = "TXT";
		if(f.getName().toLowerCase().endsWith("pdf")){
			result = "PDF";
		}
		if(f.getName().toLowerCase().endsWith("csv")){
			result = "CSV";
		}
		
		if(f.getName().toLowerCase().endsWith("xml")){
			result = identifyXML(f, troubles);
			if(result.equals("telemavana")){
				result = "XML";
			}
		}
		
		if(troubles.length()>5){
      	  String nnn = f.getAbsolutePath();
      	  if(parent.getProps().getProperty("infopath") != null){
      		  nnn =  parent.getProps().getProperty("infopath") + f.getName()   ;
      	  }
      	  this.sendTroubleEmail(troubles , nnn);
      	  System.out.println("Sending Trobles " + f.exists() + " " + nnn);
        }
		
	  return result ;
  }
  
  private String identifyTXT(File f){
	  String result = "ABC";
		
	    try{
	        BufferedReader in = new BufferedReader(
	                new FileReader(f));
            String fline = in.readLine() ;
            if(fline != null) {
            	
            
            if(fline.trim().startsWith("45") && fline.trim().length() == 10  ){
	        	   in.close();
	        	   return "SAP";	        	   
	           } 
            if(fline.trim().startsWith("Z") && (fline.trim().length() == 8 || fline.trim().length() == 7 || fline.trim().length() == 6 )
            		&& !fline.trim().startsWith("ZI")){
	        	   in.close();
	        	   return "SAP";	        	   
	           } 
            
            if(fline.trim().startsWith("SLD") && (fline.trim().length() == 10 || fline.trim().length() == 11 || fline.trim().length() == 6 )
            		&& !fline.trim().startsWith("ZI")){
	        	   in.close();
	        	   return "SAP";	        	   
	           }
	           if(!fline.startsWith("Pas1") && (fline.startsWith("Pas") || (fline.indexOf("sakymas") > -1 && fline.indexOf("sakymas") < 10))){
	        	   in.close();
	        	   return "IKI";	        	   
	           }
	           if(fline.startsWith("UNB+UNOC")  ){
	        	   in.close();
	        	   return "EDI";	        	   
	           } 
	           if(fline.trim().startsWith("<I06>")  ){
	        	   in.close();
	        	   return "EIP";	        	   
	           } 
	          // System.out.println("'" + fline + "'");
	        
	           fline = in.readLine() ;
	           fline = in.readLine() ;
	         //  System.out.println("'" + fline + "'");
	           if(fline != null && fline.startsWith("Tellija Prisma")  ){
	        	   in.close();
	        	   return "PRI";	        	   
	           }
            }else {
        		f.renameTo(new File(parent.getProps().getProperty("imp.read") + "error" + File.separator  + f.getName()));            					
      			
            	//System.exit(0);
            }
	           in.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	  return result ;
  }
  
  
  
  private void loadXLSMaxLatOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
		//	System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);
				
		
				
				if(i == 1){
					String cell = null ;
					try{
						cell =  row.getCell((short)2).getStringCellValue().trim();
					}catch(Exception aa ){
						cell = (Math.round( row.getCell((short)1).getNumericCellValue()) + "").trim();
					}
					
					firma = cell;
					System.out.println("Firma " + firma);
					
					 firma = driver.identifyCustomer(firma,null);				     
					
					 System.out.println("Firma pärast " + firma);

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED MaximaLat XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					     }
				}
				
				
				if(i == 3){
					Date date =  null;
					try{
						date = row.getCell((short)3).getDateCellValue();	
					}catch(Exception e ){
						System.out.println(row.getCell((short)3).getStringCellValue());
						date = getCalLit(row.getCell((short)3).getStringCellValue());
					}
					
					
					
					
					Calendar cal = Calendar.getInstance();
					
					
					cal.setTime(date);
					System.out.println(date);
				
					
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = row.getCell((short)1).getStringCellValue();    
		        	System.out.println( i + " " + dokNr);
		        
				}
				

				
				
				
				if (i > 5 && i < a+2){
					
			        String toode = null;
			     
			         try{
                             toode = row.getCell((short)2).getStringCellValue().trim();	
				
			        }catch (Exception e ){

			        	toode = row.getCell((short)2).getNumericCellValue() + "";
				        if(toode.indexOf("E") != -1){
				        	toode = toode.substring(0,toode.indexOf("E") );
				        	toode = toode.substring(0,1) + toode.substring(2,toode.length()).trim();
				        }
				        
				        if(toode.indexOf(".") != -1){
				        	toode = toode.substring(0,toode.indexOf(".") ).trim();
				        }
				      
			        }
			    

					double kogus = 0;
					try{
						kogus = (row.getCell((short)4).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)4).getStringCellValue()).replaceAll(",", "."));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        	
			          String name =  row.getCell((short)3).getStringCellValue();
			      System.out.println(name + " " + kogus + " " + toode );
			     String prod = null;
			          try{
			            prod = driver.identifyProduct(toode,toode);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }
			          System.out.println(name + " " + kogus + " " + toode + " " + prod );
						
				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + prod + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + prod + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED MaximaLAT XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + toode + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+firma + "*" + prod.toUpperCase() +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod.toUpperCase(), name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,
					        		  0,false,"",99," ",null,"",null,null,true,null,null);
					          counter++;
			       	  }
			    }
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
/*	try{
		f.delete();
	}catch(Exception e ){
		e.printStackTrace();
	}*/
	
	   // System.exit(0);

	  }
  
  
  private void loadXLSKatpedeleOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Sheet1");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
		//	System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);
				
		
				
				if(i == 0){
					String cell = null ;
					try{
						cell = Math.round( row.getCell((short)2).getNumericCellValue()) + "";
					}catch(Exception aa ){
						cell = Math.round( row.getCell((short)1).getNumericCellValue()) + "";
					}
					
					firma = cell;
					System.out.println("Firma " + firma);
					
					 firma = driver.identifyCustomer(firma,null);				     
					
		        	

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED KatpedeleXLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					     }
				}
				
				if(i == 2){
					Date date =  null;
					try{
						date = row.getCell((short)3).getDateCellValue();	
					}catch(Exception e ){
						System.out.println(row.getCell((short)3).getStringCellValue());
						date = getCalLit(row.getCell((short)3).getStringCellValue());
					}
					
					
					
					
					Calendar cal = Calendar.getInstance();
					
					
					cal.setTime(date);
					//System.out.println(date);
				
					
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = row.getCell((short)1).getStringCellValue();    
		        	//System.out.println( i + " " + dokNr);
				}
				

				   
				
				if (i > 4 && i < a+2){
					
			        String prod = null;
			     
			         try{
                             prod = row.getCell((short)0).getStringCellValue();	
				
			        }catch (Exception e ){

				        prod = row.getCell((short)0).getNumericCellValue() + "";
				        if(prod.indexOf("E") != -1){
				        	prod = prod.substring(0,prod.indexOf("E") );
				            prod = prod.substring(0,1) + prod.substring(2,prod.length());
				        }
				        
				        if(prod.indexOf(".") != -1){
				        	prod = prod.substring(0,prod.indexOf(".") );
				        }
				      
			        }
			    

					double kogus = 0;
					try{
						kogus = (row.getCell((short)3).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)3).getStringCellValue()).replaceAll(",", "."));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        	
			          String name =  row.getCell((short)1).getStringCellValue();
			      System.out.println(name + " " + kogus + " " + prod );
			     
			        /*  try{
			            prod = driver.identifyProduct(prod,prod);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }
*/
				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + prod + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + prod + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Katpedele XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + prod + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+firma + "*" + prod.toUpperCase() +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod.toUpperCase(), name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,
					        		  0,false,"",99," ",null,"",null,null,true,null,null);
					          counter++;
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
/*	try{
		f.delete();
	}catch(Exception e ){
		e.printStackTrace();
	}*/
	

	  }
	
  private void loadXLSPrikeOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Eesti Statoil");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a;i++){
				HSSFRow row = sheet.getRow(i);
				
			
				if(i == 1){				
				
				}
				
				if(i == 3){
					String cell = row.getCell((short)01).getStringCellValue();
					firma = cell;
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
					
		        //	System.out.println( i + " " + dokNr);
		        	

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED StatoilXLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
				if(i == 4){				
					Date date = row.getCell((short)1).getDateCellValue();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					System.out.println(date);
				
					
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = shipdate.substring(5);    
				}
				   
				
				if (i > 8 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)2).getStringCellValue();	
				//        System.out.println(ean);
			        }catch (Exception e ){

				        ean = row.getCell((short)2).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //    System.out.println(ean);
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			    
			          	
			         /* String toode = "";
			          try {

				        toode =   row.getCell((short)1).getStringCellValue();
			          } catch (Exception e ){
			        	 toode =  row.getCell((short)1).getNumericCellValue() + "";
			        	 toode = toode.substring(0,toode.indexOf("."));
			          }*/
					double kogus = 0;
					try{
						kogus = (row.getCell((short)6).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)6).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)1).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Statoil XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",
					        		  99," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }

  private void loadXLSPrikeOmaOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	    String comment = " ";
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			Workbook workbook = new XSSFWorkbook((ff));
			Sheet sheet = workbook.getSheetAt(0);
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				XSSFRow row = (XSSFRow) sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
	
			Calendar cal = Calendar.getInstance();
			dokNr = (cal.getTime().getTime() + "").substring(5);
        
			for(int i = 0 ; i < a;i++){
				XSSFRow row = (XSSFRow) sheet.getRow(i);
				//System.out.println(rosheet.getRow(i);
					
				if(i == 1){
					String cell = row.getCell((short)1).getStringCellValue();
					firma = cell;
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
					
		        //	System.out.println( i + " " + dokNr);
		        	

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Prike XLSX format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        
				}
				
				if(i == 2){	
					//Kommentaar
					comment = row.getCell((short)1).getStringCellValue();
					if(comment.trim().length() == 0 ) {
						comment = " ";
					}
					
				}
				   
				
				if (i > 3 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				//        System.out.println(ean);
			        }catch (Exception e ){

				        ean = row.getCell((short)1).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //    System.out.println(ean);
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			    
			          	
			          String toode = "";
			          try {

				        toode =   row.getCell((short)2).getStringCellValue();
			          } catch (Exception e ){
			        	 toode =  row.getCell((short)2).getNumericCellValue() + "";
			        	 toode = toode.substring(0,toode.indexOf("."));
			          }
					double kogus = 0;
					try{
						kogus = (row.getCell((short)7).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue()));
						}catch (Exception ee){
							//ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)3).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,toode);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Prike XLSX : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(comment);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",
					        		  200," ",null,"",ean,null,true,null,null);
					          //orders.setEmailBody(comment); 
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	    //System.exit(0);
	  }

  private void loadXLSLukoilOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
		//	System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a+1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 1){				
					
					Calendar cal = Calendar.getInstance();
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						hours = 72;
					}
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = shipdate.substring(5);    
				}
				
				if(i == 6){
					String cell = row.getCell((short)0).getStringCellValue();
					firma = cell;
					if(firma != null && firma.indexOf("kodas:") != -1){
						firma = firma.substring(firma.indexOf("kodas:")+6).trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Lukoil XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 11 && i < a+2){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
							
						}
				       
			        }
			       
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)4).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)4).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}
					
					
			//		 System.out.println("EAN TEST "  + ean + " " + kogus);
					
			        	  if(kogus != 0 && ean.trim().length() > 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			     
			          try{
			           prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

			         try{
				      	   if  (driver instanceof SkaiteEugImportDriver ){
				      		   String   prod2 = driver.getKodirovka(ean, 97);  
				   		       if(prod2 != null){
				   		    	   prod = prod2;
				   		       }
				        	  }
				  	          System.out.println(" kodirovka "+ prod + " ean " + ean);
				  	       } catch (Exception e) {
				  	       e.printStackTrace();	
				  	       }
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Lukoil XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,
					        		  "0",0,0,false,"",97," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }


  
  private void loadXLSAltiaOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a+1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 1){				
					
					Calendar cal = Calendar.getInstance();
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						hours = 72;
					}
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	    
				}
				
				if(i == 11){
					dokNr = shipdate.substring(5);
					try{
						 dokNr = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 dokNr = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					dokNr = dokNr.replace('\\', '-').replace('/', '-');
				}
				
				if(i == 4){
					String cell = "";
					try{
						cell = row.getCell((short)0).getStringCellValue();
					}catch(Exception e){
						cell = ((int) row.getCell((short)0).getNumericCellValue()) + "";
					}
					
					firma = cell;
					if(firma != null ){
						firma = firma.trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Altia XLSX format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 14 && i < 600){
					
			        String prod = null;
			        String ean = "";
			        try{

			        	prod = row.getCell((short)0).getStringCellValue();	
				      // System.out.println(ean);
			        }catch (Exception e ){
						try{
							prod = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(prod.indexOf("E") != -1){
						    	 prod = prod.substring(0,prod.indexOf("E") );
						     }
						 //   
						     prod = prod.substring(0,1) + prod.substring(2,prod.length());
						}catch(Exception rr){
							
						}
				       
			        }
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)1).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
						}
				    }
			       
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)3).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)3).getStringCellValue()));
						}catch (Exception ee){
							//ee.printStackTrace();
						}
					}
					
					
			//		 System.out.println("EAN TEST "  + ean + " " + kogus);
					
			        	  if(kogus != 0 && ean.trim().length() > 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			     
			          try{
			           prod = driver.identifyProduct(eans,prod);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

			       
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Altia XLSX : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + ean +  " Code:" + prod + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,
					        		  "0",0,0,false,"",97," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }


  
  private void loadXLSOrklaOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a+1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 5){				
					Date dd = null;
					try{
						  dd  = row.getCell((short)3).getDateCellValue();	
					}catch(Exception e){
						e.printStackTrace();
					}
					Calendar cal = Calendar.getInstance();
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						hours = 72;
					}
					cal.add(Calendar.HOUR, hours);
					
					System.out.println("EXCELDATE " + dd);
					if(dd != null){
						cal.setTime(dd);
					}
					
					shipdate = cal.getTime().getTime() + "";
		        	    
				}
				
				if(i == 3){
					Calendar cal = Calendar.getInstance();
					dokNr =  (cal.getTime().getTime() + "").substring(5);
					try{
						 dokNr = row.getCell((short)3).getStringCellValue();	
					}catch(Exception e){
						try{
							 dokNr = row.getCell((short)3).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					dokNr = dokNr.replace('\\', '-').replace('/', '-');
				}
				
				if(i == 4){
					String cell = "";
					try{
						cell = row.getCell((short)0).getStringCellValue();
					}catch(Exception e){
						cell = ((int) row.getCell((short)0).getNumericCellValue()) + "";
					}
					
					firma = cell;
					if(firma != null ){
						firma = firma.trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Orkla XLSX format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 14 && i < 600){
					
			        String prod = null;
			        String ean = "";
			        try{

			        	prod = row.getCell((short)0).getStringCellValue();	
				      // System.out.println(ean);
			        }catch (Exception e ){
						try{
							prod = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(prod.indexOf("E") != -1){
						    	 prod = prod.substring(0,prod.indexOf("E") );
						     }
						 //   
						     prod = prod.substring(0,1) + prod.substring(2,prod.length());
						}catch(Exception rr){
							
						}
				       
			        }
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)1).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
						}
				    }
			       
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)3).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)3).getStringCellValue()));
						}catch (Exception ee){
							//ee.printStackTrace();
						}
					}
					
					
			//		 System.out.println("EAN TEST "  + ean + " " + kogus);
					
			        	  if(kogus != 0 && ean.trim().length() > 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			     
			          try{
			           prod = driver.identifyProduct(eans,prod);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

			       
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Orkla XLSX : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + ean +  " Code:" + prod + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",
					        		  97," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }

  
 
  
  
  private void loadXLSKalevPDAEANOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	    String comment = null;
	    String ladu = null;
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			
			
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			
			
			int hours = 24;
			
			for(int i = 0 ; i < a+1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 3){				
					Date dd = null;
					
					try{
						  dd  = row.getCell((short)1).getDateCellValue();	
					 } catch (Exception e) {
						e.printStackTrace();
					 }
					
					Calendar cal = Calendar.getInstance();
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						hours = 72;
					}
					cal.add(Calendar.HOUR, hours);
					
					//System.out.println("EXCELDATE " + dd);
					if(dd != null){
						cal.setTime(dd);
					}
					
					shipdate = cal.getTime().getTime() + "";
		        	    
				}
				
				if(i == 2){
					Calendar cal = Calendar.getInstance();
					dokNr =  (cal.getTime().getTime() + "").substring(5);
					try{
						 dokNr = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 dokNr = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					dokNr = dokNr.replace('\\', '-').replace('/', '-');
					//System.out.println("doknr : " + dokNr);
					
				}
				
				if(i == 4){
					try{
						 ladu = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 ladu = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					//System.out.println("ladu " + ladu);
					
				}
				
				if(i == 5){
				
					try{
						 comment = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 comment = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					
					//System.out.println("COMMENT " + comment);
					
				}
				
				
				if(i == 1){
					String cell = "";
					try{
						cell = row.getCell((short)1).getStringCellValue();
					}catch(Exception e){
						cell = ((int) row.getCell((short)1).getNumericCellValue()) + "";
					}
					
					firma = cell;
					if(firma != null){
						firma = firma.trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Kalev XLSX format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 7 && i < 600){
				//	System.out.println("edasi " + i );
			        String prod = null;
			        String ean = "";
			        try{

			        	prod = row.getCell((short)0).getStringCellValue();	
				      // System.out.println(ean);
			        }catch (Exception e ){
						try{
							prod = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(prod.indexOf("E") != -1){
						    	 prod = prod.substring(0,prod.indexOf("E") );
						     }
						 //   
						     prod = prod.substring(0,1) + prod.substring(2,prod.length());
						}catch(Exception rr){
							
						}
				       
			        }
			        
			    //	System.out.println("prod  " + prod);
					
			        
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)1).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
						}
				    }
			       
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)3).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)3).getStringCellValue()));
						}catch (Exception ee){
							//ee.printStackTrace();
						}
					}
					
					
			//		 System.out.println("EAN TEST "  + ean + " " + kogus);
					
			        	
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			        String oldpro = prod;
			     
			          try{
			           prod = driver.identifyProduct(eans,prod);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }
			         
			          if (prod == null){
			          try{
		            	  if (driver instanceof KalevImportDriver){
		                   String  prod1 =  ((KalevImportDriver)driver).identifyProduct3(ean, prod);
		                   if(prod1 != null && prod1.trim().length() > 0){
		                	   prod=prod1;
		                   }
		                    } 	  
		                }catch(Exception e ){
		                	
		                }
			          }
			       
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT HAS NOT BEING RECOGNIZED Kalev XLSX : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + eans +  " Code:" + oldpro + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       if(kogus != 0 && prod!= null &&  prod.trim().length() > 0){
			       
			       prod = oldpro;
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",
					        		  78," ",ladu,"",ean,null,true,null,null);
					          orders.setEmailBody(comment); 
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
  
  
  private void loadXLSKalevOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	    String comment = null;
	    String ladu = null;
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			
			
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			
			
			int hours = 24;
			
			for(int i = 0 ; i < a+1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 3){				
					Date dd = null;
					
					try{
						  dd  = row.getCell((short)1).getDateCellValue();	
					 } catch (Exception e) {
						e.printStackTrace();
					 }
					
					Calendar cal = Calendar.getInstance();
					if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
						hours = 72;
					}
					cal.add(Calendar.HOUR, hours);
					
					//System.out.println("EXCELDATE " + dd);
					if(dd != null){
						cal.setTime(dd);
					}
					
					shipdate = cal.getTime().getTime() + "";
		        	    
				}
				
				if(i == 2){
					Calendar cal = Calendar.getInstance();
					dokNr =  (cal.getTime().getTime() + "").substring(5);
					try{
						 dokNr = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 dokNr = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					dokNr = dokNr.replace('\\', '-').replace('/', '-');
					//System.out.println("doknr : " + dokNr);
					
				}
				
				if(i == 4){
					try{
						 ladu = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 ladu = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					//System.out.println("ladu " + ladu);
					
				}
				
				if(i == 5){
				
					try{
						 comment = row.getCell((short)1).getStringCellValue();	
					}catch(Exception e){
						try{
							 comment = row.getCell((short)1).getNumericCellValue() + "";	
						}catch(Exception r){
						}
					}
					
					//System.out.println("COMMENT " + comment);
					
				}
				
				
				if(i == 1){
					String cell = "";
					try{
						cell = row.getCell((short)1).getStringCellValue();
					}catch(Exception e){
						cell = ((int) row.getCell((short)1).getNumericCellValue()) + "";
					}
					
					firma = cell;
					if(firma != null){
						firma = firma.trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Kalev XLSX format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 7 && i < 600){
				//	System.out.println("edasi " + i );
			        String prod = null;
			        String ean = "";
			        try{

			        	prod = row.getCell((short)0).getStringCellValue();	
				      // System.out.println(ean);
			        }catch (Exception e ){
						try{
							prod = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(prod.indexOf("E") != -1){
						    	 prod = prod.substring(0,prod.indexOf("E") );
						     }
						 //   
						     prod = prod.substring(0,1) + prod.substring(2,prod.length());
						}catch(Exception rr){
							
						}
				       
			        }
			        
			    //	System.out.println("prod  " + prod);
					
			        
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)1).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
						}
				    }
			       
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)3).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)3).getStringCellValue()));
						}catch (Exception ee){
							//ee.printStackTrace();
						}
					}
					
					
			//		 System.out.println("EAN TEST "  + ean + " " + kogus);
					
			        	
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			        String oldpro = prod;
			     
			          try{
			           prod = driver.identifyProduct(eans,prod);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }
			         
			          if (prod == null){
			          try{
		            	  if (driver instanceof KalevImportDriver){
		                   String  prod1 =  ((KalevImportDriver)driver).identifyProduct2(ean, prod);
		                   if(prod1 != null && prod1.trim().length() > 0){
		                	   prod=prod1;
		                   }
		                    } 	  
		                }catch(Exception e ){
		                	
		                }
			          }
			       
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT HAS NO PDA TUNNUS Kalev XLSX : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + eans +  " Code:" + oldpro + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       if(kogus != 0 && prod!= null &&  prod.trim().length() > 0){
			       
			       prod = oldpro;
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,"",
					        		  77," ",ladu,"",ean,null,true,null,null);
					          orders.setEmailBody(comment); 
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }

  
  
  
  private void loadXLSStockmanOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	
	    	XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
		
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a +1;i++){
				Row row = sheet.getRow(i);
				
			
				if(i == 8){				
					Date dd = row.getCell((short)3).getDateCellValue();
	        		System.out.println(dd.toString());
	        		shipdate = dd.getTime() + "";
	        		try{
	        			dokNr = (row.getCell((short)4).getStringCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							dokNr =(row.getCell((short)4).getNumericCellValue()) + "";
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}
		            
				
					firma = "stockman";
					firma = driver.identifyCustomer(firma,null);				     
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Stockman XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				
			
				   
				
				if (i > 35 && i < a+3){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
						try{
							 ean = row.getCell((short)0).getNumericCellValue() + "";
						     
						     if(ean.indexOf("E") != -1){
						     	ean = ean.substring(0,ean.indexOf("E") );
						     }
						 //   
						     ean = ean.substring(0,1) + ean.substring(2,ean.length());
						}catch(Exception rr){
							
						}
				       
			        }
			        System.out.println(ean);
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)4).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)4).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0 && ean.trim().length() > 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			     
			          try{
			           prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }
			         
			         try{
				      	   if  (driver instanceof SkaiteEugImportDriver ){
				      		   String   prod2 = driver.getKodirovka(ean, 97);  
				   		       if(prod2 != null){
				   		    	   prod = prod2;
				   		       }
				        	  }
				  	          System.out.println(" kodirovka "+ prod + " ean " + ean);
				  	       } catch (Exception e) {
				  	       e.printStackTrace();	
				  	       }
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Stockman XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,
					        		  "",97," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }



  
  
  private void loadXLSRivonaXOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
			
		
			System.out.println("sees " + a);
	
			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);		
			
				  if(i == 0){
						Date dd = row.getCell((short)8).getDateCellValue();
		        		if(dd == null){
		        			dd = new Date();
		        		}
		        		shipdate = dd.getTime() + "";
		        		System.out.println(dd.toString());   
			        }
				  if(i == 1){
						dokNr = shipdate.substring(8);   
			        }
		
				  if(i == 6){
					String cell = null;
					try{
						cell = row.getCell((short)2).getStringCellValue();
					}catch (Exception r){
						cell = ((int) row.getCell((short)2).getNumericCellValue()) + "";
					}
					
					
					firma = cell;
					
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED RivonaX XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
				}
				
	         

				if (i > 12 && i < a){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)1).getStringCellValue();	
				       
			        }catch (Exception e ){

				        ean = row.getCell((short)1).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //   
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			       // System.out.println(ean);
			    
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)7).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
					
					double price = 0;
					try{
						price = (row.getCell((short)6).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							price = Double.parseDouble((row.getCell((short)6).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}						
					}	  
					
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			       if(eans.length() == 12){
			    	   try{
			              	  EAN13CheckDigit ee = new EAN13CheckDigit();
			                    eans = eans + ee.calculate(eans);
			                }catch(Exception e){
			              	  
			                }
			       }
			         try{
			           prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

			         
				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED RIVONA X XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean +
			        			    " amount: " + kogus + " price" + price + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",price,0,false,
					        		  "",55," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
	  


  
  private void loadXLSEigintaOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Sheet1");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
			
		
			System.out.println("sees " + a);
	
			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);		
				
				if(i == 1){
					
					String cell = row.getCell((short)1).getStringCellValue();
					firma = cell;
					if(firma != null && firma.indexOf("Padalinys:") != -1){
						firma = firma.substring(firma.indexOf("Padalinys:")+10).trim();
					}
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
				}
				

				if(i == 4){
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED Eiginta XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
				//	Data: 2011.05.30, Tiekimo data: 2011.06.02
					String datestr = row.getCell((short)1).getStringCellValue();
					if(datestr.indexOf("Tiekimo data:") != -1){
						datestr = datestr.substring(datestr.indexOf("Tiekimo data:") + 13).trim();					
					}
					Calendar cal = Calendar.getInstance();
					cal.setTime(this.getCalLit(datestr));
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = shipdate.substring(5);   
		        	System.out.println(datestr);
		        	//System.exit(0);
				}
				
				if (i > 5 && i < a+2){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
				       
			        }catch (Exception e ){

				        ean = row.getCell((short)0).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //   
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			        System.out.println(ean);
			          	
			      
					double kogus = 0;
					try{
						kogus = (row.getCell((short)2).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)2).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)1).getStringCellValue();
			      
			       if(eans.length() == 12){
			    	   try{
			              	  EAN13CheckDigit ee = new EAN13CheckDigit();
			                    eans = eans + ee.calculate(eans);
			                }catch(Exception e){
			              	  
			                }
			       }
			         try{
			           prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

			         
				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED EIGINITA XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",
					        		  0,0,false,"",92," ",null,"",ean,null,true,null,null);
					 
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
	  
  
	  

  private void loadXLSAssortiOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);

	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    String shipdate = " ";
	   
	    String tellijaRegnr = null;
	    String kood = null;
	    int counter = 0;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Ataskaita");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
			
		
			System.out.println("sees " + a);

			for(int i = 0 ; i < a+1;i++){
				HSSFRow row = sheet.getRow(i);		
				
				if(i == 3){
					
					String cell = row.getCell((short)0).getStringCellValue();
					firma = cell;
					if(firma != null && firma.indexOf("ADRESAS:") != -1){
						firma = firma.substring(firma.indexOf("ADRESAS:")+8).trim();
					}
					System.out.println("Firma '" + firma + "'");
					
					firma = driver.identifyCustomer(firma,null);				     
				}
				if(i == 10){
					
					dokNr = row.getCell((short)0).getStringCellValue();
					if(dokNr != null && dokNr.length()>20){
						dokNr = dokNr.substring(19).trim();
					}
					System.out.println("doknr " + dokNr);
					
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED ASSORTI XLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
				}

				if(i == 14){
					
				//	Data: 2011.05.30, Tiekimo data: 2011.06.02
					String datestr = row.getCell((short)9).getStringCellValue();
					if(datestr.indexOf("Tiekimo data:") != -1){
						datestr = datestr.substring(datestr.indexOf("Tiekimo data:") + 13).trim();					
					}
				
					Calendar cal = Calendar.getInstance();
					cal.setTime(this.getCalLit(datestr));
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = shipdate.substring(5);   
		        	System.out.println(datestr);
		       
				}
				
				if (i > 17 && i < a+2){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
				       
			        }catch (Exception e ){
                      try{
				        ean = row.getCell((short)0).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //   
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
                        }catch (Exception uu ){
                                     }
			        }
			        System.out.println("EAN " + ean);
			          	
			      if(!ean.equals("")){
			    	  
					double kogus = 0;
					try{
						
						kogus = (row.getCell((short)6).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)6).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
					 System.out.println("kogus " + kogus);
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)2).getStringCellValue();
			      
			       if(eans.length() == 12){
			    	   try{
			              	  EAN13CheckDigit ee = new EAN13CheckDigit();
			                    eans = eans + ee.calculate(eans);
			                }catch(Exception e){
			              	  
			                }
			       }
			         try{
			           prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }


			         try{
			      	   if  (driver instanceof SkaiteEugImportDriver ){
			      		   String   prod2 = driver.getKodirovka(ean, 83);  
			   		       if(prod2 != null){
			   		    	   prod = prod2;
			   		       }
			        	  }
			  	          System.out.println(" kodirovka "+ prod + " ean " + ean);
			  	       } catch (Exception e) {
			  	       e.printStackTrace();	
			  	       }
				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED ASSORTI XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,
					        		  "",83," ",null,"",ean,null,true,null,null);
					 
			       	  }
			      }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
	  
  
  
  
  private void loadXLSSanitexOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	//sanitex multi orderite lugemiseks 
	  String fileName = f.getName();
	    setEmailBody(orders,fileName);
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Sheet1");
			
			
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			
			//1. otsime mitu telli on ja mis reast mi reani kestab 
			
			Vector tellid = new Vector();
		
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				try{
					
					 String eraldaja = row.getCell((short)1).getStringCellValue();
					 if(eraldaja.trim().equals("Sanitex") ){						
						 tellid.add(i + "");	
						}
				}catch(Exception e){
					//e.printStackTrace();
				}
			}
			
			
		
			for(int i = 0 ; i < tellid.size() ;i++){
				
				int algus = Integer.parseInt((String)tellid.elementAt(i));
				int lopp = a+1;
				try{
					lopp = Integer.parseInt((String)tellid.elementAt(i+1));
				}catch(Exception g ){
				//	g.printStackTrace();
				}
				
				
				System.out.println("l�pp " + algus + " " + lopp);
				HSSFRow row = sheet.getRow(algus + 3);
				
			 Date date = row.getCell((short)6).getDateCellValue();			 
				Calendar cal = Calendar.getInstance();
				dokNr = ( cal.getTime().getTime() + "").substring(5);
				cal.setTime(date);						
				shipdate = cal.getTime().getTime() + "";
	        	 
	        
	        String partner = "";
	        try{
	        	 row = sheet.getRow(algus + 4);
	        	partner =  row.getCell((short)6).getStringCellValue();
	        	partner = partner.trim();
	        	if(partner.length() > 6){
	        		partner = partner.substring(2,5);
	        	}
	        	
	        	//07EUGCP1111129N1
	        	System.out.println("partner " +partner);
	        }catch(Exception r){
	        	//r.printStackTrace();
	        }
	       
	        
	      
	        
	        
	        row = sheet.getRow(algus + 10);
	        String cell = "";
	        try{
		    	cell =  row.getCell((short)3).getStringCellValue();	
		    	cell = cell.trim();
		    	if(cell.length() > 20){
		    		cell = cell.substring(0,18);
		    	}
	        }catch(Exception w){
	        	
	        }
			firma = cell;
			System.out.println("Firma " + firma + " " + date + " " + partner);
		  firma = driver.identifyCustomer(firma,null);				     
			//  System.out.println();
			 if (firma == null || firma.trim().length() == 0){
			       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
			       doknr=dokNr;
			       troubles.append("Customer CANNOT BE RESOLVED SanitexXLS format \n" + "Dok Nr: " + doknr + "\n"
							 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
			     
			     }
        	
			 String ean = "";
			 String prod = null;
			 
			 for(int d = algus+14 ; d < lopp-1; d++){
				  row = sheet.getRow(d);
				
				   
			       
			        
			     		double kogus = 0;
						try{
							kogus = (row.getCell((short)7).getNumericCellValue());
						}catch (Exception e){
							e.printStackTrace();
							try{
								kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue()));
							}catch (Exception ee){
								ee.printStackTrace();
								try{
									kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue().replace(',', '.')));
								}catch (Exception ef){
									ef.printStackTrace();
									System.out.println("assaa " + (row.getCell((short)7).getStringCellValue()));
								}
							}
							
						}	
			        	if(kogus != 0){
			        		 try{
			                ean = row.getCell((short)2).getStringCellValue();	
						
						        }catch (Exception e ){

							        ean = row.getCell((short)2).getNumericCellValue() + "";
							      
							        if(ean.indexOf("E") != -1){
							        	ean = ean.substring(0,ean.indexOf("E") );
							        }
							    //   
							        ean = ean.substring(0,1) + ean.substring(2,ean.length());
						        }
						        String name =  row.getCell((short)1).getStringCellValue();    
						        System.out.println(ean + " " + kogus + " " + name) ;

						          try{
						            prod = driver.identifyProduct(ean,ean);
						         } catch (SQLException e) {
						            e.printStackTrace();	
						         }

							
						       if (prod == null){
						         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
						         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
						         //break;
						         prod = " ";
						         if(prod.trim().length() == 0 ){
						        	 troubles.append("PRODUCT CANNOT BE RESOLVED Sanitex XLS : \n" + "Dok Nr: " + doknr + "\n"
						        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
						     		
						        }
						       }
						       
									System.out.println(i + "*"+firma+"*"+ ean +"*"+ name +"*"+kogus+"*"+ dokNr  );
								      //	System.exit(0);
								          orders.setInfo(dokNr);
								          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,
								        		  "0",0,0,false,"",99," ",null,"",ean,null,true,null,null);
								  counter++;
								  orders.setTeostaja(partner);
			        	}
			 }    
			 			
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }

  
  private void loadXLSXSanitexOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
		//sanitex multi orderite lugemiseks 
		  String fileName = f.getName();
		    setEmailBody(orders,fileName);
		
		    String tellijaKood = null;
		    String dokNr = null;
		    firma = "";
		    int counter = 0;
		    String shipdate = " ";
		    String tellijaRegnr = null;
		    String kood = null;
			int a = 0;
		    try{
		    	FileInputStream ff = new FileInputStream(f);
			
		    	XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheet("Sheet1");
				
				
				
				
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					XSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
						//System.out.println("99 " + i);
					}else{
						a = sheet.getLastRowNum();
					}
				}
				System.out.println("sees " + a);
				//Enumeration enum = sheet.getLastRowNum()
				
				//1. otsime mitu telli on ja mis reast mi reani kestab 
				
				Vector tellid = new Vector();
			
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					XSSFRow row = sheet.getRow(i);
					try{
						
						 String eraldaja = row.getCell((short)1).getStringCellValue();
						 if(eraldaja.trim().equals("Sanitex") ){						
							 tellid.add(i + "");	
							}
					}catch(Exception e){
						//e.printStackTrace();
					}
				}
				
				
			
				for(int i = 0 ; i < tellid.size() ;i++){
					
					int algus = Integer.parseInt((String)tellid.elementAt(i));
					int lopp = a+1;
					try{
						lopp = Integer.parseInt((String)tellid.elementAt(i+1));
					}catch(Exception g ){
					//	g.printStackTrace();
					}
					
					
					System.out.println("l�pp " + algus + " " + lopp);
					XSSFRow row = sheet.getRow(algus + 3);
					
				 Date date = row.getCell((short)6).getDateCellValue();			 
					Calendar cal = Calendar.getInstance();
					dokNr = ( cal.getTime().getTime() + "").substring(5);
					cal.setTime(date);						
					shipdate = cal.getTime().getTime() + "";
		        	 
		        
		        String partner = "";
		        try{
		        	 row = sheet.getRow(algus + 4);
		        	partner =  row.getCell((short)6).getStringCellValue();
		        	partner = partner.trim();
		        	if(partner.length() > 6){
		        		partner = partner.substring(2,5);
		        	}
		        	
		        	//07EUGCP1111129N1
		        	System.out.println("partner " +partner);
		        }catch(Exception r){
		        	//r.printStackTrace();
		        }
		       
		        
		      
		        
		        
		        row = sheet.getRow(algus + 10);
		        String cell = "";
		        try{
			    	cell =  row.getCell((short)3).getStringCellValue();	
			    	cell = cell.trim();
			    	if(cell.length() > 20){
			    		cell = cell.substring(0,18);
			    	}
		        }catch(Exception w){
		        	
		        }
				firma = cell;
				System.out.println("Firma " + firma + " " + date + " " + partner);
			  firma = driver.identifyCustomer(firma,null);				     
				//  System.out.println();
				 if (firma == null || firma.trim().length() == 0){
				       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
				       doknr=dokNr;
				       troubles.append("Customer CANNOT BE RESOLVED SanitexXLS format \n" + "Dok Nr: " + doknr + "\n"
								 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
				     
				     }
	        	
				 String ean = "";
				 String prod = null;
				 
				 for(int d = algus+14 ; d < lopp-1; d++){
					  row = sheet.getRow(d);
					
					   
				       
				        
				     		double kogus = 0;
							try{
								kogus = (row.getCell((short)7).getNumericCellValue());
							}catch (Exception e){
							//	e.printStackTrace();
								try{
									kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue()));
								}catch (Exception ee){
									try{
										kogus = Double.parseDouble((row.getCell((short)7).getStringCellValue().replace(',', '.')));
									}catch (Exception ef){
										ef.printStackTrace();
										System.out.println("assaa " + (row.getCell((short)7).getStringCellValue()));
									}
								}
								
							}	
				        	if(kogus != 0){
				        		 try{
				                ean = row.getCell((short)2).getStringCellValue();	
							
							        }catch (Exception e ){

								        ean = row.getCell((short)2).getNumericCellValue() + "";
								      
								        if(ean.indexOf("E") != -1){
								        	ean = ean.substring(0,ean.indexOf("E") );
								        }
								    //   
								        ean = ean.substring(0,1) + ean.substring(2,ean.length());
							        }
							        String name =  row.getCell((short)1).getStringCellValue();    
							        System.out.println(ean + " " + kogus + " " + name) ;

							          try{
							            prod = driver.identifyProduct(ean,ean);
							         } catch (SQLException e) {
							            e.printStackTrace();	
							         }

								
							       if (prod == null){
							         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
							         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
							         //break;
							         prod = " ";
							         if(prod.trim().length() == 0 ){
							        	 troubles.append("PRODUCT CANNOT BE RESOLVED Sanitex XLS : \n" + "Dok Nr: " + doknr + "\n"
							        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
							     		
							        }
							       }
							       
										System.out.println(i + "*"+firma+"*"+ ean +"*"+ name +"*"+kogus+"*"+ dokNr  );
									      //	System.exit(0);
									          orders.setInfo(dokNr);
									          orders.add(firma, prod, name ,kogus, dokNr, null, 0,
									        		  0,shipdate,"0",0,0,false,"",99," ",null,"",ean,null,true,null,null);
									  counter++;
									  orders.setTeostaja(partner);
				        	}
				 }    
				 			
				}
				
			ff.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		//f.delete();

		  }

  
  
  
  
  private void loadXLSStatoilLITOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	   
		
		
	   
			
			XSSFWorkbook workbook1 = null;
	    	HSSFWorkbook workbook2 = null;
	    	Sheet sheet = null;
	    	try{
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook2 = new HSSFWorkbook(ff);
	    		 sheet = (HSSFSheet) workbook2.getSheetAt(0);
	    		 ff.close();
	    	}catch (Exception u){
	    		FileInputStream ff = new FileInputStream(f);
	    		 workbook1 = new XSSFWorkbook(ff);
	    		 sheet = (XSSFSheet)workbook1.getSheetAt(0);
	    		 ff.close();
	    	}
	    	 
			
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				Row row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			
			
			for(int i = 0 ; i < a+1 ; i++){
				Row row = sheet.getRow(i);
				
				if(i == 0){
					String cell = row.getCell((short)1).getStringCellValue();
					firma = cell;
					
					firma = driver.identifyCustomer(firma,null);				     
					
					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED StatoilXLSLIT format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }		        	
				    }
				
   Date dd = null;
				   if(i == 3){
					   String asi =  "";
					   try{
						   asi =  (row.getCell((short)1).getStringCellValue());
					   }catch(Exception e){
						   asi =  ((int)(row.getCell((short)1).getNumericCellValue())+ "");
						   asi = asi.replace('.', ' ').replaceAll(" ", "");
						   
						   try{
							  dd =  row.getCell((short)1).getDateCellValue(); 
						   }catch(Exception h){
							   h.printStackTrace();
						   }
					   }
					   System.out.println("asi " + asi +" "+ dd );
					    

if(dd != null){
	shipdate = dd.getTime()+ "";
}else{
	   
shipdate = getCalXML(asi);

}
					   
					   
                       }
				
                     if(i == 4){
                    	 try{
                    		 dokNr = row.getCell((short)1).getStringCellValue(); 
                    	 } catch(Exception e){
                    		 dokNr = (int)row.getCell((short)1).getNumericCellValue() + "";
                    		 
                    	 }
                    	 
					     System.out.println("Firma " + firma + " " + dokNr + " " +shipdate );
					    
			            }
                //     System.out.println("Firma " + i + " " + a );
					    
                     
				if (i > 5 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			       // System.out.println("eeee" +ean);
			         try{
			        	 row.getCell((short)2).setCellType(Cell.CELL_TYPE_STRING);
				        ean = row.getCell((short)2).getStringCellValue();	
				       // System.out.println(ean);
			        }catch (Exception e ){
//e.printStackTrace();
				        ean = row.getCell((short)2).getNumericCellValue() + "";
				  
				       // System.out.println("ean " + ean);
					    
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			         //System.out.println("ean " + ean);
					    
			        
					double kogus = 0;
					try{
						kogus = (row.getCell((short)4).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)4).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)3).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED Statoil XLS LIT: \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,
					        		  "",99," ",null,"",ean,null,true,null,null);
					  counter++;
			       	  }
			    }
				
			}
			//System.exit(0);
	
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }
  
  

  private void loadXLSStatoilOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Uzsakymas");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			int hours = 24;
			for(int i = 0 ; i < a;i++){
				HSSFRow row = sheet.getRow(i);
				
			
				if(i == 1){				
				try{
					hours = Integer.parseInt(Math.round(row.getCell((short)2).getNumericCellValue())+ "");
				}catch (Exception e ){
					e.printStackTrace();
					try{
						hours = Integer.parseInt(row.getCell((short)2).getStringCellValue());
					}catch (Exception ee){
					ee.printStackTrace();	
					}
					
				}
				}
				
				if(i == 2){
					String cell =Math.round( row.getCell((short)0).getNumericCellValue()) + "";
					firma = cell;
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);				     
					Date date = row.getCell((short)2).getDateCellValue();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					System.out.println(date);
				
					
					cal.add(Calendar.HOUR, hours);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = shipdate.substring(5);    
		        //	System.out.println( i + " " + dokNr);
		        	

					 if (firma == null || firma.trim().length() == 0){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED StatoilXLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }
		        	
				}
				

				   
				
				if (i > 3 && i < a+1){
					
			        String prod = null;
			        String ean = "";
			         try{

				        ean = row.getCell((short)0).getStringCellValue();	
				//        System.out.println(ean);
			        }catch (Exception e ){

				        ean = row.getCell((short)0).getNumericCellValue() + "";
				      
				        if(ean.indexOf("E") != -1){
				        	ean = ean.substring(0,ean.indexOf("E") );
				        }
				    //    System.out.println(ean);
				        ean = ean.substring(0,1) + ean.substring(2,ean.length());
			        }
			    
			          	
			         /* String toode = "";
			          try {

				        toode =   row.getCell((short)1).getStringCellValue();
			          } catch (Exception e ){
			        	 toode =  row.getCell((short)1).getNumericCellValue() + "";
			        	 toode = toode.substring(0,toode.indexOf("."));
			          }*/
					double kogus = 0;
					try{
						kogus = (row.getCell((short)2).getNumericCellValue());
					}catch (Exception e){
					//	e.printStackTrace();
						try{
							kogus = Double.parseDouble((row.getCell((short)2).getStringCellValue()));
						}catch (Exception ee){
							ee.printStackTrace();
						}
						
					}	  
			        	  if(kogus != 0){
			        		
			          String eans = ean;
			          if(eans.indexOf(".") != -1){
			        	  eans = eans.substring(0,eans.indexOf("."));
			          }
			          String name =  row.getCell((short)1).getStringCellValue();
			      
			     
			          try{
			            prod = driver.identifyProduct(eans,eans);
			         } catch (SQLException e) {
			            e.printStackTrace();	
			         }

				
			       if (prod == null){
			         addLog(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         System.out.println(parent.getString("noprodid") + "\n " + ean + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
			         //break;
			         prod = " ";
			         if(prod.trim().length() == 0 ){
			        	 troubles.append("PRODUCT CANNOT BE RESOLVED StatoilEug XLS : \n" + "Dok Nr: " + doknr + "\n"
			        			 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:"  + " Code:" + ean + " amount: " + kogus + "\n\n");
			     		 doknr=dokNr;
			        }
			       }
			       
						System.out.println(i + "*"+firma+"*"+ eans +"*"+ name +"*"+kogus+"*"+ dokNr  );
					      //	System.exit(0);
					          orders.setInfo(dokNr);
					          orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0,false,
					        		  "",99," ",null,"",ean,null,true,null,null);
					  counter++;
			       	  }
			    }
				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	//f.delete();

	  }



  

  
  
  private void loadXLSSelverFileOrder(Orders orders, File[] list,StringBuffer troubles) throws IOException{
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    Date shipDate = null;
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
		TreeMap allOrders = new TreeMap();
		StringBuffer toemail = new StringBuffer();
		String filen = "";
		//  1. Peame lugema k�ik tellimused MAP-i k�ikidest failidest ja siis mapi l�bi itereerima ja sealt tellimusd moodustama
		//siia mapi paneme kye Selveri filiaali ja Value on Vector toodetega kus kyljes kogused 
	    try{ 
	    	for (int e = 0; e < list.length ; e++){
	    	
	    
	        File f = list[e];
	        if(f.isFile()){
	        filen = f.getName();
	    	FileInputStream ff = new FileInputStream(f);
	    	HSSFWorkbook workbook =  null;
	    	HSSFSheet sheet = null;
	    	XSSFWorkbook workbookX = null;
	    	XSSFSheet sheetX = null;
	    	boolean old = true;
	    	try{
	    		 workbook = new HSSFWorkbook((ff));
				 sheet = workbook.getSheetAt(0);
	    	}catch(Exception r){
	    		 ff = new FileInputStream(f);
	    		 workbookX = new XSSFWorkbook(ff); 
				 sheetX = workbookX.getSheetAt(0); 
				 old = false;
		 	}
	    	
	    	
	    	
			
		
			if(sheet != null){
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
						//System.out.println("99 " + i);
					}else{
						a = sheet.getLastRowNum();
					}
				}
			}else{
				for(int i = 0 ; i < sheetX.getLastRowNum();i++){
					XSSFRow row = sheetX.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
						//System.out.println("99 " + i);
					}else{
						a = sheetX.getLastRowNum();
					}
				}
			}
			
			
			//Enumeration enum = sheet.getLastRowNum()
			
			try{
				shipDate = sheetX.getRow(3).getCell(2).getDateCellValue();
			}catch(Exception i){
				
				try{
				String aa = sheetX.getRow(3).getCell(2).getStringCellValue();
				shipDate = this.parseDate(aa);
				}catch(Exception ii){
					
				}
				//i.printStackTrace();
			}
			System.out.println("sees " + a + " shipDate : " + shipDate);
			
			//System.exit(0);
			TreeMap custs = new TreeMap();
			
			if(old){
				for(int i = 0 ; i < a +1;i++){
					HSSFRow row = sheet.getRow(i);
					
				
					if(i == 3){				
				        
				        	 int rl = row.getLastCellNum();

				        	 for(int u = 5 ; u < rl ; u++){
				        		 String pr = null;
				       try{		
					        pr = row.getCell((short)u).getStringCellValue();	
					     //  System.out.println(u + "  " + pr);
				        }catch (Exception z ){

					        pr = row.getCell((short)u).getNumericCellValue() + "";
					      
					        if(pr.indexOf("E") != -1){
					        	pr = pr.substring(0,pr.indexOf("E") );
					        }
					        if(pr.indexOf(".") != -1)
					        pr = pr.substring(0,pr.indexOf("."));
				        }
				        
				        if(pr != null && !pr.equals("")){
				          	custs.put(pr, u+"");
				        }
				        
			        	 }
	 System.out.println("Selverite asukohad tabelis :" + custs);
					   	 
					}
					
					if(i > 5){			
						String toode = "";
						String nimi = "";
						String ean = "";
						System.out.println("EAN 0 " + ean + " " + toode);
						
						
						
						try{
						    ean = row.getCell((short)1).getStringCellValue() ;
						}catch(Exception r ){
							ean = Math.round( row.getCell((short)1).getNumericCellValue()) + "";
						}
						
						
						try{
						    toode = row.getCell((short)2).getStringCellValue() ;
					   	}catch(Exception r ){
						try{
							toode = Math.round( row.getCell((short)2).getNumericCellValue()) + "";
						}catch(Exception t){
							toode = ean;
						}
						}
						if(toode.length() == 0){
							toode = ean;
						}
						
						if(toode.startsWith("#MU")){
							toode = ean;
						}
						try{
						    nimi = row.getCell((short)3).getStringCellValue().trim() ;
						}catch(Exception r ){
							nimi = Math.round( row.getCell((short)3).getNumericCellValue()) + "";
						}
						System.out.println("EAN " + ean + " " + toode);
						
						 //Siin k�ime k�ik kliendid l�bi ja kysime koguse kylge 
	                    Iterator itera = custs.keySet().iterator();
	                    while(itera.hasNext()){
	                  	  String fil = (String)itera.next();
	                  	  int index = Integer.parseInt((String)custs.get(fil));
							double kogus = 0;
							try{
								kogus = (row.getCell((short)index).getNumericCellValue());
								
							}catch (Exception p){
							//	e.printStackTrace();
								try{
									kogus = Double.parseDouble((row.getCell((short)index).getStringCellValue()));
								}catch (Exception ee){
									ee.printStackTrace();
								}
								
							}	  
					        	  if(kogus != 0 & toode.trim().length() != 0){

							System.out.println(i+ " *" + fil  +"*"+kogus+"*"+ toode);
							
							 if(allOrders.get(fil) == null){
								 //Kui pole selle poel veel midagi 
								 Product tt = new Product(toode);  
								 tt.setName(nimi);
								 tt.setStock(kogus);
								 tt.setEAN(ean);
								 HashMap vvv =  new HashMap();
								 vvv.put(tt.getID(),tt);
								 allOrders.put(fil, vvv);
						     	 }else{
						     		 
						     		 HashMap vvv = (HashMap)allOrders.get(fil);
						     		 //kui poel on aga seda toodet pole 
						     		 if(vvv.get(toode) == null){
						     			 Product tt = new Product(toode); 
						     			 tt.setEAN(ean);
										 tt.setName(nimi);
										 tt.setStock(kogus);
										 vvv.put(toode,tt);
										 allOrders.put(fil, vvv);
						     		 }else{
						     			 //kui on k�ik ja peame lihtsalt kogust suurendama 
						     			Product pp = (Product)vvv.get(toode);
						     			pp.setStock(pp.getStock() + kogus);
						     			vvv.put(toode,pp);
										 allOrders.put(fil, vvv);
						     		}
							      }
					            }
					          }
				            }
	                      }
			}else{
				//uue exceli formaadi asi 
			
			for(int i = 0 ; i < a +1;i++){
				XSSFRow row = sheetX.getRow(i);
				
			
				if(i == 3){				
			        
			        	 int rl = row.getLastCellNum();

			        	 for(int u = 5 ; u < rl ; u++){
			        		 String pr = null;
			       try{		
				        pr = row.getCell((short)u).getStringCellValue();	
				     //  System.out.println(u + "  " + pr);
			        }catch (Exception z ){

				        pr = row.getCell((short)u).getNumericCellValue() + "";
				      
				        if(pr.indexOf("E") != -1){
				        	pr = pr.substring(0,pr.indexOf("E") );
				        }
				        if(pr.indexOf(".") != -1)
				        pr = pr.substring(0,pr.indexOf("."));
			        }
			        
			        if(pr != null && !pr.equals("")){
			          	custs.put(pr, u+"");
			        }
			        
		        	 }
 System.out.println("Selverite asukohad tabelis :" + custs);
				   	 
				}
				
				if(i > 5){			
					String toode = "";
					String nimi = "";
					String ean = "";
					System.out.println("EAN 0 " + ean + " " + toode);
					try{
					    toode = row.getCell((short)2).getStringCellValue() ;
					}catch(Exception r ){
						toode = Math.round( row.getCell((short)2).getNumericCellValue()) + "";
					}
					
					
					try{
					    ean = row.getCell((short)1).getStringCellValue() ;
					}catch(Exception r ){
						ean = Math.round( row.getCell((short)1).getNumericCellValue()) + "";
					}
					
					
					
					if(toode.startsWith("#MU")){
						toode = ean;
					}
					System.out.println("EAN " + ean + " " + toode);
					try{
					    nimi = row.getCell((short)3).getStringCellValue().trim() ;
					}catch(Exception r ){
						nimi = Math.round( row.getCell((short)3).getNumericCellValue()) + "";
					}
					
					 //Siin k�ime k�ik kliendid l�bi ja kysime koguse kylge 
                    Iterator itera = custs.keySet().iterator();
                    while(itera.hasNext()){
                  	  String fil = (String)itera.next();
                  	  int index = Integer.parseInt((String)custs.get(fil));
						double kogus = 0;
						try{
							kogus = (row.getCell((short)index).getNumericCellValue());
							
						}catch (Exception p){
						//	e.printStackTrace();
							try{
								kogus = Double.parseDouble((row.getCell((short)index).getStringCellValue()));
							}catch (Exception ee){
								ee.printStackTrace();
							}
							
						}	  
				        	  if(kogus != 0 & toode.trim().length() != 0){

						//	System.out.println(i+ " *" + fil  +"*"+kogus+"*"+ toode);
						
						 if(allOrders.get(fil) == null){
							 //Kui pole selle poel veel midagi 
							 Product tt = new Product(toode);  
							 tt.setName(nimi);
							 tt.setStock(kogus);
							 tt.setEAN(ean);
							 HashMap vvv =  new HashMap();
							 vvv.put(tt.getID(),tt);
							 allOrders.put(fil, vvv);
					     	 }else{
					     		 
					     		 HashMap vvv = (HashMap)allOrders.get(fil);
					     		 //kui poel on aga seda toodet pole 
					     		 if(vvv.get(toode) == null){
					     			 Product tt = new Product(toode); 
					     			 tt.setEAN(ean);
									 tt.setName(nimi);
									 tt.setStock(kogus);
									 vvv.put(toode,tt);
									 allOrders.put(fil, vvv);
					     		 }else{
					     			 //kui on k�ik ja peame lihtsalt kogust suurendama 
					     			Product pp = (Product)vvv.get(toode);
					     			pp.setStock(pp.getStock() + kogus);
					     			vvv.put(toode,pp);
									 allOrders.put(fil, vvv);
					     		}
						      }
				            }
				          }
			            }
                      }
			       }
			
		ff.close();
		
		  try{  
			  filen = f.getName();
			  File fff = new File(parent.getProps().getProperty("imp.read") + "done");
				if(!fff.exists()){
					fff.mkdir();
				}
				
	        	 f.renameTo(new File(driver.getImportFile() + "\\done\\" + f.getName()));
	   	   }catch(Exception r ){
	   		   r.printStackTrace();
	   		 f.delete();
	        }
	        }
	      
	       
	       
	    	}
	}catch(Exception e) {
		e.printStackTrace();
	}
	System.out.println("valmis objekt " + allOrders.toString() );
	importOrdersFromHash(allOrders,troubles,filen,shipDate);
  
	

	  }

  
  
  private void importOrdersFromHash(TreeMap ords, StringBuffer troubles , String filen, Date shipDate){
	  
	  StringBuffer toemail = new StringBuffer();
	  Iterator itera = ords.keySet().iterator();
	  Calendar cal = Calendar.getInstance();
	  if(shipDate != null){
		  cal.setTime(shipDate);
	  }
	  int cou = 0;
	  long num = cal.getTimeInMillis();
	  while(itera.hasNext()){
		  String fil = (String)itera.next();
		  HashMap asjad = (HashMap)ords.get(fil);
		 
		  cou = cou +1;
		
			String shipdate = cal.getTime().getTime() + "";
		 Orders orders = new Orders("HOUSE",false);
		
		 Iterator ite = asjad.keySet().iterator();
		 double kogus = 0;
		
		 while (ite.hasNext()){
			
		    Product pp = (Product)asjad.get((String)ite.next());
		      orders.setInfo((num + cou) + "");
		      orders.add(fil, pp.getID(), pp.getName() ,pp.getStock(), (num + cou) + "", null, 0, 0,shipdate,
		    		  "0",0,0,false,"",77," ",null,"",pp.getEAN(),null,true,null,null);
	          kogus = kogus + pp.getStock();
		 }
		 
		 
		 toemail.append(" Nr: " + (num + cou) + "" + "   Selver : " + fil + " " + this.getSelvNimi(fil) +  "   Ridu : " + asjad.size() + "   Kogus kokku : "  + (int)kogus + "\n\n");
			 

				    try{
	                  	importOrder(orders,troubles); 	
				    }catch(Exception eee){
				    	eee.printStackTrace();
				    }
		 
		 
		  
	  }
	  this.sendReportEmail(toemail,filen);
  }
  
  
  
  
  private void loadXLSMULTIOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("MULTIORDER");
			
			
			for(int i = 0 ; i < sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
				//System.out.println(row);
				if(row == null && a == 0){
					a = i;
					//System.out.println("99 " + i);
				}else{
					a = sheet.getLastRowNum();
				}
			}
			System.out.println("sees " + a);
			//Enumeration enum = sheet.getLastRowNum()
			
			TreeMap prods = new TreeMap();
			
			
			for(int i = 0 ; i <= a;i++){
				HSSFRow row = sheet.getRow(i);
				
			
				if(i == 2){				
			        
			        	 int rl = row.getLastCellNum();

			        	 for(int u = 5 ; u < rl ; u++){
			        		 String pr = null;
			       try{		
				        pr = row.getCell((short)u).getStringCellValue();	
				       System.out.println(u + "  " + pr);
			        }catch (Exception e ){

				        pr = row.getCell((short)u).getNumericCellValue() + "";
				      
				        if(pr.indexOf("E") != -1){
				        	pr = pr.substring(0,pr.indexOf("E") );
				        }
				        if(pr.indexOf(".") != -1)
				        pr = pr.substring(0,pr.indexOf("."));
			        }
			        
			        if(pr != null){
			          	prods.put(pr, u+"");
			        }
			        
		        	 }
   System.out.println(prods);
						//	System.exit(0);    	 
				}
				
				if(i > 3){
					 orders = new Orders("aaa", false);
					String cell = "";
					try{
					    cell = row.getCell((short)1).getStringCellValue() ;
					}catch(Exception r ){
						cell = Math.round( row.getCell((short)0).getNumericCellValue()) + "";
					}
					
					firma = "MA" + cell; // maxima variant
					
					System.out.println("Firma " + firma);
					firma = driver.identifyCustomer(firma,null);
					if(firma == null){
						try{
						    cell = row.getCell((short)0).getStringCellValue() ;
						}catch(Exception r ){
							cell = Math.round( row.getCell((short)0).getNumericCellValue()) + "";
						}
						
						
						firma = driver.identifyCustomer(cell,null);
						
					}
					System.out.println("Firma peale " + firma + " " + cell);
					Date date = row.getCell((short)3).getDateCellValue();
					Calendar cal = Calendar.getInstance();
					String num = cal.getTimeInMillis() + "";
					cal.setTime(date);
					System.out.println(date);
					shipdate = cal.getTime().getTime() + "";
		        	dokNr = "70" + num.substring(6) + "" + i;    
		        //	System.out.println( i + " " + dokNr);
		        	

					 if (firma == null || firma.trim().length()  == 2){
					       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
					       troubles.append("Customer CANNOT BE RESOLVED MULTIXLS format \n" + "Dok Nr: " + doknr + "\n"
									 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
					       doknr=dokNr;
					     }

					 //Siin k�ime k�ik tooded l�bi ja kysime koguse kylge 
                      Iterator itera = prods.keySet().iterator();
                      while(itera.hasNext()){
                    	  String prod = (String)itera.next();
                    	  int index = Integer.parseInt((String)prods.get(prod));
  						double kogus = 0;
  						try{
  							kogus = (row.getCell((short)index).getNumericCellValue());
  							
  						}catch (Exception e){
  						//	e.printStackTrace();
  							try{
  								kogus = Double.parseDouble((row.getCell((short)index).getStringCellValue()));
  							}catch (Exception ee){
  								ee.printStackTrace();
  							}
  							
  						}	  
  				        	  if(kogus != 0){

  							System.out.println(i + "*"+firma+"*"+ prod  +"*"+kogus+"*"+ dokNr);
  						          
  						          orders.setInfo(dokNr);
  						          orders.add(firma, prod, "noname" ,kogus, dokNr, null, 0, 0,shipdate,"0",
  						        		  0,0,false,"",82," ",null,"",null,null,true,null,null);
  						  counter++;
  				       	  }
  
                      }
				       				    try{
				       	                  	importOrder(orders,troubles); 	
				       				    }catch(Exception eee){
				       				    	eee.printStackTrace();
				       				    }
					 
				}
				

				
			}
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	 f.delete();

	  }

  
  private void loadXLSMULTISHEETOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
	    String fileName = f.getName();
	    setEmailBody(orders,fileName);
	
	    String tellijaKood = null;
	    String dokNr = null;
	    firma = "";
	    int counter = 0;
	    String shipdate = " ";
	    String opdate = " ";
	    String tellijaRegnr = null;
	    String kood = null;
		String teostaja = null;

	    int a = 0;
	    try{
	    	FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			int nu = workbook.getNumberOfSheets();
			System.out.println("SheetArv " + nu );
			for(int b = 0; b < nu; b++){
				HSSFSheet sheet = workbook.getSheetAt(b);
				System.out.println("SheetArv " + b );
				for(int i = 0 ; i < sheet.getLastRowNum();i++){
					HSSFRow row = sheet.getRow(i);
					//System.out.println(row);
					if(row == null && a == 0){
						a = i;
						//System.out.println("99 " + i);
					}else{
						a = sheet.getLastRowNum();
					}
				}
				System.out.println("SheetNimi " + sheet.getSheetName() + " sees "  + a);
				//Enumeration enum = sheet.getLastRowNum()
				
				TreeMap prods = new TreeMap();
				
				if(!sheet.getSheetName().equals("Sheet2") && !sheet.getSheetName().equals("Sheet3")){
				//loema ainult neid sheete kus on kvaliteet peal 	
				
				for(int i = 0 ; i <= a;i++){
					HSSFRow row = sheet.getRow(i);
	if(i == 2){
		if(b == 0 ){//esimeselt lehelt loeme sisse partneri koodi 
			teostaja =  row.getCell((short)4).getStringCellValue();
			System.out.println("Teostaja " + teostaja );
		}
	}
				        
				
					if(i == 3){				
				        
							 int rl = row.getLastCellNum();

				        	 for(int u = 6 ; u < rl ; u++){
				        		 String pr = null;
				       try{		
					        pr = row.getCell((short)u).getStringCellValue();	
					       System.out.println(u + " rida " + pr + " " + rl);
				        }catch (Exception e ){

					        pr = row.getCell((short)u).getNumericCellValue() + "";
					        System.out.println(u + " rida " + pr + " " + rl);
					        if(pr.indexOf("E") != -1){
					        	pr = pr.substring(0,pr.indexOf("E") );
					        }
					        if(pr.indexOf(".") != -1)
					        pr = pr.substring(0,pr.indexOf(".")) ;
				        }
				        
				        if(pr != null && pr.length() > 0){
				        	 System.out.println(u + " ridafinal " + pr + " " + rl);
				          	prods.put(pr, u+"");
				        }else{
				        	 troubles.append("Product. CANNOT BE RESOLVED MultiSheetXLS format \n" + "Line No: " + doknr + "\n"
									 + u +  "\n\n");
				        }
				        
			        	 }
	 System.out.println(prods);
							//	System.exit(0);    	 
					}
					
					if(i > 5){
						 orders = new Orders("F126", false);
						String cell = "";
						try{
						    cell = row.getCell((short)2).getStringCellValue() ;
						}catch(Exception r ){
						///System.out.println("hetkel i " + i + " " +);
							//	r.printStackTrace();
							cell = Math.round( row.getCell((short)2).getNumericCellValue()) + "";
						}
						
						firma =  cell; 
						System.out.println("Firma " + firma);
						firma = driver.identifyCustomer(firma,null);	
						System.out.println("Firma peale " + firma);
						Calendar cal = Calendar.getInstance();
						String num = cal.getTimeInMillis() + "";
						try{
							Date dateop = row.getCell((short)0).getDateCellValue();
							Date date = row.getCell((short)1).getDateCellValue();
							cal.setTime(date);
							System.out.println(date);
							shipdate = cal.getTime().getTime() + "";
							
							cal.setTime(dateop);
							opdate = cal.getTime().getTime() + "";
						}catch(Exception r ){
						//	r.printStackTrace();
						}
						dokNr = "70" + num.substring(7);    
			        //	System.out.println( i + " " + dokNr);
			        	

						 if (firma == null || firma.trim().length()  == 2){
						       addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
						       troubles.append("Customer CANNOT BE RESOLVED MultiSheetXLS format \n" + "Dok Nr: " + doknr + "\n"
										 + "\t\tRegNr:" + tellijaRegnr + " Code:" + kood + " Number:" + dokNr + "\n\n");
						       doknr=dokNr;
						     }

						 //Siin k�ime k�ik tooded l�bi ja kysime koguse kylge 
	                    Iterator itera = prods.keySet().iterator();
	                    while(itera.hasNext()){
	                  	  String prod = (String)itera.next();
	                  	  int index = Integer.parseInt((String)prods.get(prod));
							double kogus = 0;
							try{
								kogus = (row.getCell((short)index).getNumericCellValue());
								
							}catch (Exception e){
							//	e.printStackTrace();
								try{
									kogus = Double.parseDouble((row.getCell((short)index).getStringCellValue()));
								}catch (Exception ee){
									ee.printStackTrace();
								}
								
							}	  
					        	  if(kogus != 0){

								System.out.println(i + "*"+firma+"*"+ prod  +"*"+kogus+"*"+ dokNr);
							          
							          orders.setInfo(dokNr);
							          orders.add(firma, prod, "noname" ,kogus, dokNr, null, 0, 0,shipdate,"0",0,0
							        		  ,false,"",82," ",null,"",null,opdate,true,null,null);
							          orders.setTeostaja(teostaja);
							          
							          counter++;
					       	  }

	                    }
					       				    try{
					       	                  	importOrder(orders,troubles); 	
					       				    }catch(Exception eee){
					       				    	eee.printStackTrace();
					       				    }
						 
					}
					

					
				}
				
			}
			}
		
			
		ff.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
	 f.delete();

	  }


  
  
  private Vector getConversionCodes() {
  	Vector fil = new Vector();
    File file = new File( "abifailid" +
                         File.separator + "ImpProdConvert.txt");

    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      String custid = null;
      String prodorder = null;
      String prodown = null;
      double convert = 0;
      boolean roundup = false;

      while ( (line = reader.readLine()) != null) {
       if(line.trim().length() != 0 ){
       //  System.out.println(line);
      StringTokenizer st = new StringTokenizer(line, ";");
      custid = st.nextToken();
      prodorder = st.nextToken();
      prodown = st.nextToken();
      convert = Double.parseDouble(st.nextToken());
      int ii = Integer.parseInt(st.nextToken());
      if(ii == 1){
      	roundup  = true;	
      }
      ConvertData com = new ConvertData();
      com.setCustid(custid);
      com.setProdOrder(prodorder);
      com.setProdOwn(prodown);
      com.setRoundup(roundup);
      com.setConvert(convert);
      fil.add(com);

       }
     }
    }
    catch (IOException e) {
  // e.printStackTrace();
    }

    return fil;
   }

  private Vector getCustConvCodes(String custid){
  	Vector vec = new Vector();
  	Enumeration enume = this.convCodes.elements();
  	while(enume.hasMoreElements()){
  		ConvertData dd = (ConvertData)enume.nextElement();
  		if (dd.getCustid().equals(custid)){
  			vec.add(dd);
//  			System.out.println("dd" + dd.toString());
  		}
  	}
  	
  	return vec;
  }
  

  private void loadSAPOrder(Orders orders, File f,StringBuffer troubles) throws IOException{
    String fileName = f.getName();
    FileReader fr = new FileReader(f);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    int counter = 0;
    String tellijaKood = null;
    String dokNr = null;
     firma = "";
    String shipdate = " ";
  
    String tellijaRegnr = null;
    String kood = null;
    Vector conCod = null;
    while( (line = br.readLine()) != null){
      counter++;
      line = line.trim();
      System.out.println(line);
      if (counter == 1){
        dokNr = line;
      } else if(counter == 3){
    	  System.out.println("kuup�eva otsing " + line);
      	shipdate = getCal(line);
      } else if(counter == 5){
        tellijaKood = line;
        if(!"true".equals(parent.getProps().getProperty("sap.nomaxima"))){
        	if(tellijaKood.trim().equals("2") || tellijaKood.trim().equals("1") || tellijaKood.trim().equals("TA01") ){//Riia maxima case 2 ja leedu oma 1
            	counter++;
            	tellijaKood = br.readLine().trim();
            }	
        }else{
        	if(tellijaKood.trim().equals("2") || tellijaKood.trim().equals("1") || tellijaKood.trim().equals("TA01") ){
        		//Riia maxima case 2 ja leedu oma 1
        		tellijaKood = tellijaKood + br.readLine().trim();
        		counter++;
        	}
                
        	}
            
        
        try{
          if(tellijaRegnr != null && (tellijaKood.trim().equals("null") || tellijaKood.trim().equals(""))){
          kood = tellijaRegnr ; } else {kood = tellijaKood;}
          firma = driver.identifyCustomer(kood,tellijaRegnr);
          conCod = getCustConvCodes(firma);
        } catch (Exception e) {
         e.printStackTrace();
        }
        if (firma == null ||firma.trim().length() == 0){
          addLog(parent.getString("nocompid") + " " + tellijaKood + "\n" + fileName + "\n" + parent.getString("fixcompid") +"\n" );
          troubles.append("Customer CANNOT BE RESOLVED SAP TXT format \n"+ "Dok Nr: " + doknr + "\n" 
    				 + "\t\tRegNr:" + tellijaRegnr + " Code:" + tellijaKood + "\n\n");
         
        }
      } else if(counter > 7 ){
        String ol = null;
        String prod = " ";
        while( (ol = br.readLine()) != null ){
        //	System.out.println(ol);
                if(ol.trim().length()>0){
                
            ol = ol.replaceAll("\t\t","| | ");

            ol = ol.replaceAll("\t","| ");
            if(ol.startsWith("|")){
            	ol = " " + ol;
            }
          List row = parseLine(ol, "|");
          System.out.println(ol);

          String ean = (String)row.get(3);
          String toode = (String)row.get(4);
          String name = (String)row.get(11);
          String unit = (String)row.get(6);
          System.out.println("toode'"+toode +"'  '"+ean + "' " + name + " '" + unit);

          try{
        	  String vahetoode = toode;
        	  if((tellijaKood.equals("680") || tellijaKood.equals("520") ||  tellijaKood.equals("250")) && (firma.equals("520") ||firma.equals("680") || firma.equals("250")) ){
        		  vahetoode = vahetoode + "|LAT";
        	  }
          prod = driver.identifyProduct(ean,vahetoode);
          System.out.println(toode +"  "+ vahetoode);
       } catch (Exception e) {
       e.printStackTrace();	
       addLog(e.toString());
       }
       

       try{
    	   if  (driver instanceof SkaiteEugImportDriver ){
    		   String   prod2 = driver.getKodirovka(ean, 98);  
 		       if(prod2 != null){
 		    	   prod = prod2;
 		       }
      	  }
	          System.out.println(" kodirovka "+ prod + " ean " + ean);
	       } catch (Exception e) {
	       e.printStackTrace();	
	       }
       double kogus = Double.parseDouble((String)row.get(5));
       if (prod == null ){
         addLog(parent.getString("noprodid") + "\n " + ean + " " + toode + "\n" + fileName + "\n" + parent.getString("fixprodid") +"\n\n");
         //break;
         prod = " ";
         if(prod.trim().length() == 0 ){
     		 troubles.append("PRODUCT CANNOT BE RESOLVED SAP TXT format : \n" + "Dok Nr: " + doknr + "\n"
     				 + "\tLineNum: "  +  (counter+1) + " " + name +" EAN:" + ean + " Code:" + toode + " amount: " + kogus + "\n\n");
        }
       }
         
          
          //Vector conCod = null;
          // conCod = getCustConvCodes(firma);
          double convert = 1 ;
          boolean up = false;
          //System.out.println("conCod.size()" + conCod.size());
          if(conCod != null){
          	Enumeration enume = conCod.elements();
          	while(enume.hasMoreElements()){
          		ConvertData dd= (ConvertData)enume.nextElement();
          		System.out.println("toode" + toode + " own " + dd.getProdOwn() + "'   " +dd.isRoundup()+ " " + dd.getConvert());
          		if(dd.getProdOwn().equals(prod)){
          			convert = dd.getConvert();
          			up = dd.isRoundup();
          		}
          	}
          	
          }
          
          kogus = kogus * convert;
          if(up){
          //	System.out.println("kogus" + kogus);
          	kogus = Math.round(kogus + 0.49);
          	//System.out.println("kogusrr" + kogus);
          }

          double price = 0;
          if("true".equals(parent.getProps().getProperty("sap.readprice"))){
        	  try{
        		  double xxxx = 1;
        		  try{
        			 xxxx = Double.parseDouble((String)row.get(8)); 
        		  }catch(Exception k){
        			  
        		  }
        		  price = Double.parseDouble((String)row.get(7))/xxxx;
        	  }catch(Exception r){
        		  r.printStackTrace();
        	  }
      	  }
          
           System.out.println("Sisestamine ees " + firma+"*"+ prod+"*"+ name +"*"+kogus+"*"+ dokNr + "*" + shipdate );
          if(ean == null || ean.trim().length() == 0 || (!ean.equals("100004003")  && !ean.equals("100004004")  && !ean.equals("100004005")  && 
							!ean.equals("100004001")  && !ean.equals("100004002")  && !ean.equals("100004007")  && !ean.equals("100004006")
							&& !ean.equals("100004010")&& !ean.equals("100004021")&& !ean.equals("100004012") && !ean.equals("100004011")
							 && !ean.equals("100004029") && !ean.equals("100004024"))){//100004024 100004021 
        	  if (prod != null && !prod.equals("NOPANT")){
        		
          	 orders.add(firma, prod, name ,kogus, dokNr, null, 0, 0,shipdate,"0",price,0,false,"",98,unit,null,"",ean,null,true,null,null);
          	orders.setInfo(dokNr);
          	orders.setCompName(tellijaKood); //Filiaali tunnuse paneme firmanime koha peale
          	orders.setShipMode(tellijaKood);
          System.out.println("Sisestamine " + firma+"*"+ prod+"*"+ name +"*"+kogus+"*"+ dokNr + " || " + tellijaKood );
          
        	  }
          }
          // orders.setEmailBody(text);
        }
       }
      }
    }
    br.close();
    fr.close();
  }

  private File unPgp(File file){
	  String fn = file.getAbsolutePath();
	  System.out.println("Pakin lahti PGP-d" + new Date().toString());
	  log.info("Pakin lahti PGP-d" + new Date().toString());
	  String imp = file.getParent();
	  
	  String localPath = (new File("")).getAbsolutePath();
	  long ll = new Date().getTime();
	  String phrasefile = "drogas.txt" ;
	  if(parent.getProps().getProperty("pgp.passphrasefile") != null){
		  phrasefile = parent.getProps().getProperty("pgp.passphrasefile");
	  }
	  
	  
	//C:\earve\gnupg\gpg.exe  --passphrase-file C:\earve\gnupg\drogas.txt --load-extension Lib\idea -o C:\earve\aaa.txt --decrypt  C:\earve\Order.xml.asc  
	  String[] cmd = {"cmd", "/C", localPath + "\\gnupg\\gpg.exe --yes --passphrase-file " + localPath
			     + "\\" + phrasefile + " --load-extension Lib\\idea -o \"" + imp + "\\" +ll +"Order.xml\" --decrypt \""  + fn + "\""};

	  System.out.println(cmd[0] + " " + cmd[1] + " " + cmd[2]);
	  Runtime rt = Runtime.getRuntime();
	 try{

		  Process p = rt.exec(cmd);	 
		  try {
			    Thread.sleep(2000);                 //2000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			p.destroy();
		//  p.waitFor();
		  
	 }catch (Exception e){
		 e.printStackTrace();
	 } 
	 
	  File aa = new File(imp + "\\"  + ll + "Order.xml");
	  File f = new File(fn);
	  if(aa.exists()){
		  f.delete();  
		  System.out.println("UnPacked PGP " + file.getName() +  " at " + new Date());
	  }else{
		  f.renameTo(new File(imp + "\\error\\" + file.getName()));
		  System.out.println("!!!! NOT UnPacked PGP " + file.getName() +  " at " + new Date());
	  }
	  return new File ( imp + "\\"  + ll + "Order.xml");
  }

  private File unZip(File file){
    try{
        File unzipped = null;
        ZipFile zipFile = new ZipFile(file.getAbsolutePath());

        ZipInputStream zin =
            new ZipInputStream(new FileInputStream(file.getAbsolutePath()));
        ZipEntry       entry;

        while ((entry = zin.getNextEntry()) != null) {
          BufferedReader in =
              new BufferedReader(new InputStreamReader(zin));
          PrintWriter    out =
              new PrintWriter(new FileOutputStream(driver.getImportFile() + entry.getName()));
          unzipped = new File(driver.getImportFile() + entry.getName());
          String	   line;

          while ((line = in.readLine()) != null) {
            out.println(line);
          }

          out.close();
          zin.closeEntry();
        }

        zin.close();
        zipFile.close();
        file.delete();
        file = unzipped;
      } catch (Exception e){
      }
      
      return file;
  }
  
  public static String deCode(String res ){
	   
	   while(res.indexOf("11111xx") != -1){
	   	int aa = res.indexOf("11111xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   }
	   while(res.indexOf("11112xx") != -1){
	   	
	   	int aa = res.indexOf("11112xx");
	   //	System.out.println(aa + " " +res);
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11113xx") != -1){
	   	int aa = res.indexOf("11113xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11114xx") != -1){
	   	int aa = res.indexOf("11114xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11115xx") != -1){
	   	int aa = res.indexOf("11115xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11116xx") != -1){
	   	int aa = res.indexOf("11116xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11117xx") != -1){
	   	int aa = res.indexOf("11117xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11118xx") != -1){
	   	int aa = res.indexOf("11118xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11119xx") != -1){
     	   	int aa = res.indexOf("11119xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
  	   while(res.indexOf("11120xx") != -1){
     	   	int aa = res.indexOf("11120xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
  	   while(res.indexOf("11121xx") != -1){
     	   	int aa = res.indexOf("11121xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
  	   while(res.indexOf("11122xx") != -1){
     	   	int aa = res.indexOf("11122xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
	   
	  
	
	   while(res.indexOf("11123xx") != -1){
  	   	int aa = res.indexOf("11123xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11124xx") != -1){
  	   	int aa = res.indexOf("11124xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11127xx") != -1){
  	   	int aa = res.indexOf("11127xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11128xx") != -1){
  	   	int aa = res.indexOf("11128xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11129xx") != -1){
  	   	int aa = res.indexOf("11129xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11130xx") != -1){
     	   	int aa = res.indexOf("11130xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
	   while(res.indexOf("11131xx") != -1){
    	   	int aa = res.indexOf("11131xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11132xx") != -1){
    	   	int aa = res.indexOf("11132xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11133xx") != -1){
    	   	int aa = res.indexOf("11133xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11134xx") != -1){
    	   	int aa = res.indexOf("11134xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11135xx") != -1){
    	   	int aa = res.indexOf("11135xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11136xx") != -1){
    	   	int aa = res.indexOf("11136xx");
    	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
    	   } 
	   while(res.indexOf("11137xx") != -1){
   	   	int aa = res.indexOf("11137xx");
   	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
   	   } 
	   while(res.indexOf("11138xx") != -1){
   	   	int aa = res.indexOf("11138xx");
   	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
   	   } 
	   while(res.indexOf("11139xx") != -1){
   	   	int aa = res.indexOf("11139xx");
   	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
   	   } 
	   while(res.indexOf("11140xx") != -1){
   	   	int aa = res.indexOf("11140xx");
   	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
   	   } 
	   while(res.indexOf("11141xx") != -1){
  	   	int aa = res.indexOf("11141xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11142xx") != -1){
  	   	int aa = res.indexOf("11142xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11143xx") != -1){
  	   	int aa = res.indexOf("11143xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11144xx") != -1){
  	   	int aa = res.indexOf("11144xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11145xx") != -1){
  	   	int aa = res.indexOf("11145xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11146xx") != -1){
  	   	int aa = res.indexOf("11146xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11147xx") != -1){
  	   	int aa = res.indexOf("11147xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   } 
	   while(res.indexOf("11148xx") != -1){
  	   	int aa = res.indexOf("11148xx");
  	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
  	   }    
	   while(res.indexOf("11125xx") != -1){
     	   	int aa = res.indexOf("11125xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   } 
   	   while(res.indexOf("11126xx") != -1){
     	   	int aa = res.indexOf("11126xx");
     	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
     	   }  

   	   while(res.indexOf("11150xx") != -1){
        	   	int aa = res.indexOf("11150xx");
        	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
        	   }  
	return res;
}


  public static List parseLine(String lin, String separator){
    List list = new ArrayList();
    StringTokenizer st = new StringTokenizer(lin, separator);
    while(st.hasMoreElements()){
      list.add(st.nextToken().trim());
    }
    return list;
  }

  private void loadABCOrder(Orders orders, File dokFile,StringBuffer troubles){
    String fileName = dokFile.getName();
     File file = new File(fileName);
      file = dokFile;
    try {
      setEmailBody(orders,fileName);

      try{
        File unzipped = null;
        ZipFile zipFile = new ZipFile(file);

        ZipInputStream zin =
            new ZipInputStream(new FileInputStream(file));
        ZipEntry       entry;

        while ((entry = zin.getNextEntry()) != null) {
          BufferedReader in =
              new BufferedReader(new InputStreamReader(zin));
          PrintWriter    out =
              new PrintWriter(new FileOutputStream(driver.getImportFile() + entry.getName()));
          unzipped = new File(driver.getImportFile() + entry.getName());
          String	   line;
          while ((line = in.readLine()) != null) {
            out.println(line);
          }
          out.close();
          zin.closeEntry();
        }

        zin.close();
        zipFile.close();
        file.delete();
        file = unzipped;
      } catch (ZipException e){
      }
      
      if("true".equals(parent.getProps().getProperty("order.encode.ansi"))){
    	  try{
    		 File encoded = null;
    	        Charset win = Charset.forName("cp1251");
    	        Charset utf = Charset.forName("utf-8");
     	       
    	        	InputStream in = new FileInputStream(file);
    	            Reader reader = new InputStreamReader(in,utf);
    	            OutputStream out = new FileOutputStream( driver.getImportFile() + "x" + fileName);
    	            Writer writer = new OutputStreamWriter(out,win);
    	           // reader.read();//laseme bom minna 
    	         
    	          char[] buffer = new char[1024];
    	          int read;
    	          while ((read = reader.read(buffer)) != -1) {
    	            writer.write(buffer, 0, read);
    	          }
    	         reader.close();
    	         writer.close();
    	         File d = new File(driver.getImportFile() + "x" + fileName);
    	         
    	         file.delete();
    	         d.renameTo(new File(driver.getImportFile() + fileName));
    	 

      } catch (Exception e){
    	  e.printStackTrace();
      }
      }
      
      
      FileReader ff = new FileReader(file);
      BufferedReader in = new BufferedReader(ff);
 
      String line;
      String num = "";
      String agcode = null;
      int linesonorder = 0;
      int count = 0;
      while((line = in.readLine()) != null){
     if("true".equals(parent.getProps().getProperty("fixlines"))){
    	    	
    	         
      //  System.out.println("line:" + line);
         in.mark(200);
         String line2 = in.readLine();
         if(line2 != null && line2.trim().length() < 10){
        	 line = line + line2;
        	 
        	 line2 = in.readLine();
             if(line2 != null && line2.trim().length() < 10){
               	 line = line + line2;
                    	 
             }
          }else{
        	 in.reset();
         }
         System.out.println("lineend :" + line);
         
    	     }
      	String text = "";
      	line = line.replaceAll(";;", "; ;");
      //	line = decode(line);
      	count ++;
        StringTokenizer st =  new StringTokenizer(line, String.valueOf(ImportPanel.SEPARATOR));
        if (st.hasMoreTokens()){
          String firma = st.nextToken();
          if (st.hasMoreTokens()){
            String toode = st.nextToken();
            if (st.hasMoreTokens()){
              double kogus = 0;
              try{
                kogus = Double.parseDouble(st.nextToken());	  
              }catch(Exception e){
            	  e.printStackTrace();
              }
            
              String dokNr = null;
              String dealer = null;
              double bonus = -1;
              double discount = 0;
              String name = " ";
              String datelong = null;
              String pl = "0";
              double price = 0;
              double inner = 0;
              int paymterm = 0;
              int status = 0;
              String text2 = "";
              String unit = "";
           
              String warehouse = null;
              String shipmode = "";
              String partii = " ";
              String shiptime = null;
              String reference = null;
              String agordnum = null;
              String selActCode = null;
              boolean shipDateUsed = false;
              String docType = null;
              String photoname = null;
              String signaname = null;
              String contactid = null;
              String fullDeliveryOnly = null;
              
              if (st.hasMoreTokens()){
                dokNr = st.nextToken();
                if (st.hasMoreTokens()){
                  dealer = st.nextToken();
                  if (st.hasMoreTokens()){
                    bonus = Double.parseDouble(st.nextToken());
                    if (st.hasMoreTokens()){
                    	try{
                    		 discount = Double.parseDouble(st.nextToken());	
                    	}catch (Exception e){
                    	}
                     
                      if (st.hasMoreTokens()) {
                        datelong = (st.nextToken()).trim();
                         if (st.hasMoreTokens()) {
                            pl = (st.nextToken());
                            if (st.hasMoreTokens()) {
                               price = Double.parseDouble(st.nextToken());
                               if (st.hasMoreTokens()) {
                            	   String token = st.nextToken();
                            	//   System.out.println("tok " + token);
                            	   try{
                            		   paymterm = Integer.parseInt(token);
                                       	   
                            	   }catch(Exception e){
                            		   
                            	   }
                                 if (st.hasMoreTokens()) {
                                   agcode = st.nextToken();
                                   if (st.hasMoreTokens()) {
                                	   token = st.nextToken();
                                	   
                                	   try{
                                		   status = Integer.parseInt(token);
                                           	   
                                	   }catch(Exception e){
                                		   
                                	   }
                                     if (st.hasMoreTokens()) {
                                       unit = st.nextToken();
                                       if (st.hasMoreTokens()) {
                                        text = st.nextToken();
                                         if (st.hasMoreTokens()) {
                                         //	st.nextToken();
                                         	text2  = st.nextToken();//laseme teksti tyhja minna  
                                         	if (st.hasMoreTokens()) {
                                            warehouse = st.nextToken();
                                        }
                                        	if (st.hasMoreTokens()) {
                                             partii = st.nextToken();
                                            }
                                        	if (st.hasMoreTokens()) {
                                               agordnum =  st.nextToken();
                                              // System.out.println("agordnum" + agordnum);
                                            }	
                                        	if (st.hasMoreTokens()) {
                                                shipmode = st.nextToken();
                                               // System.out.println("shipmode" + shipmode);
                                            }
                                        	if (st.hasMoreTokens()) {
                                                shiptime = st.nextToken();
         	
                                    			//System.out.println("shiptime" + shiptime);
                                            }
                                        	if (st.hasMoreTokens()) {
                                                reference = st.nextToken();
                                            	
                                    			//System.out.println("reference" + reference);
                                            }
                                        	if (st.hasMoreTokens()) {
                                        		try{
                                        			String aa = st.nextToken();
                                        		//	System.out.println("aa" + aa);
                                        			linesonorder = Integer.parseInt(aa);
                                        		}catch(Exception ee){
                                        			ee.printStackTrace();
                                        	 }
                                           }
                                        	if (st.hasMoreTokens()) {
                                        		try{
                                        	           inner  = Double.parseDouble(st.nextToken());
                                                }catch(Exception k){
                                        			//k.printStackTrace();
                                        		}
                                           }
                                        	
                                        	if (st.hasMoreTokens()) {
                                        		try{
                                        	           int ss  = Integer.parseInt(st.nextToken());
                                        	           if(ss == 1){
                                        	        	   shipDateUsed = true;
                                        	           }
                                                }catch(Exception k){
                                        			//k.printStackTrace();
                                        		}
                                           }
                                        	
                                        	if (st.hasMoreTokens()) {
                                        			selActCode = st.nextToken() ;
                                           }

                                        	if (st.hasMoreTokens()) {
                                        		docType = st.nextToken() ;
                                           }

                                        	if (st.hasMoreTokens()) {
                                        		photoname = st.nextToken() ;
                                           }

                                        	if (st.hasMoreTokens()) {
                                        		signaname = st.nextToken() ;
                                           }
                                        	if (st.hasMoreTokens()) {
                                        		contactid = st.nextToken() ;
                                           }
                                        	if (st.hasMoreTokens()) {
                                        		fullDeliveryOnly = st.nextToken() ;
                                           }
                                         }
                                       }
                                     }
                                   }
                                 }
                               }
                            }
                         }
                       }
                    }
                  }
                }
              }
              
           
              if(text2.trim().length()>0){
              	 System.out.println("seadistn Info  :" + text2);
                orders.setInfo(text2.replace('', ' ')); 
                
              }
             
            
              if(text.trim().length()>0){
            	  //	 System.out.println("seadistn Info  :" + text.replace('', ' '));
                     
            	  orders.setEmailBody(text.replace('', ' '));             
              }
             
              if(shipmode.trim().length()>0){
                  orders.setShipMode(shipmode);             
                }
              if(agcode != null) {
            	  orders.agent = agcode;
              }
              
               
              if(agordnum != null && agordnum.trim().length()>0 && driver.getDriverVersion() != null &&
            		  (driver.getDriverVersion().equals("SolcraftJunImport") || driver.getDriverVersion().equals("AxaDunkerImport")
            				  || driver.getDriverVersion().equals("NaviTobisImport") 
            				   || driver.getDriverVersion().equals("NaviTobisKALImport") 
            				   || driver.getDriverVersion().equals("HorizonTriImport") 

                               || driver.getDriverVersion().equals("ViciCZSQLImport")
                                      || driver.getDriverVersion().equals("AxaZpienasImport")
            						  || driver.getDriverVersion().equals("AxaBuraLatImport")
            						  || driver.getDriverVersion().equals("SolcraftAuto")
            						  || driver.getDriverVersion().equals("BiovelaImport")
            						  || driver.getDriverVersion().equals("RVEktoniImport")
            						  || driver.getDriverVersion().equals("OzolsImport") 
            						  || driver.getDriverVersion().equals("KMAHortiImport") 
            						  || driver.getDriverVersion().equals("PalettiLitImport") 
            						  || driver.getDriverVersion().equals("RivileGirImport") 
            						  || driver.getDriverVersion().equals("HorizonKonigImport")
            						  || driver.getDriverVersion().equals("HorizonKonig18Import")
            						   || driver.getDriverVersion().equals("ViaLaimaUUSImport")
            						    || driver.getDriverVersion().equals("Directo")
            						  || driver.getDriverVersion().equals("RivonaImport")
            						   || driver.getDriverVersion().equals("RivonaNorfaImport")
            						  || driver.getDriverVersion().equals("NaviVilandraImport")
            						  || driver.getDriverVersion().equals("AltiaImport")
            						   || driver.getDriverVersion().equals("SAFMaagGrupp")
            						  || driver.getDriverVersion().equals("AxaLitArgaImport")
            						    || driver.getDriverVersion().equals("AltiaLATImport")
            						    || driver.getDriverVersion().equals("RivileCeryxImport")
            						     || driver.getDriverVersion().equals("RivileKurybaImport")
            						     || driver.getDriverVersion().equals("HansaPommImport")
            						    || driver.getDriverVersion().equals("KGGruppImport")
            						      || driver.getDriverVersion().equals("RivileArktisImport")
            						     || driver.getDriverVersion().equals("HansaSatcomImport")
            						    || driver.getDriverVersion().equals("KGGrupp2Import")
            						   || driver.getDriverVersion().equals("DunkerLatImport")
            				           || driver.getDriverVersion().equals("BuitNowacoLTImport")
            				           || driver.getDriverVersion().equals("BiovelaUusImport")
            				          || driver.getDriverVersion().equals("NaviOskarImport")
            				          || driver.getDriverVersion().equals("RivonaKedaImport")
            				           || driver.getDriverVersion().equals("LabbisSkonisImport")
            				           || driver.getDriverVersion().equals("AxaOrklaLV")
            				           || driver.getDriverVersion().equals("GlobusTrade")
            				           || driver.getDriverVersion().equals("AxaAmber")
            				           || driver.getDriverVersion().equals("AxaAmberLitImport")
            				           || driver.getDriverVersion().equals("AxaAmberEstImport")
            				           || driver.getDriverVersion().equals("AxaAmberLatImport")
            				           || driver.getDriverVersion().equals("AxaAmberIBImport")
            				           || driver.getDriverVersion().equals("AxaFazer")
            				           || driver.getDriverVersion().equals("NaviLivikoESTImport")
            				           || driver.getDriverVersion().equals("NaviLivikoLATImport")
            				           || driver.getDriverVersion().equals("NaviLivikoLITImport")
            				           || driver.getDriverVersion().equals("SolcraftFlower")
            				           || driver.getDriverVersion().equals("RivileBalmerk")
            				           || driver.getDriverVersion().equals("ViciPOLSQLImport")
            				           || driver.getDriverVersion().equals("SkaiteVigestaImport")
            				           || driver.getDriverVersion().equals("SkaiteKMImport")
            				           || driver.getDriverVersion().equals("AxaJNGLAT")
            				           || driver.getDriverVersion().equals("IceAxaUusImport")
            				           || driver.getDriverVersion().equals("NaviValdoImport")
            				           || driver.getDriverVersion().equals("SamsonasCSVImport")
            				           || driver.getDriverVersion().equals("KosmelitaImport")
            				           
            						  )){
            	  dokNr = agordnum;         	  
              }
              
              if(driver.getDriverVersion().equals("SkaiteAG")){
            	  String ddd = firma;
            	  ddd =  driver.identifyCustomer(firma, null)  ;
            	if(ddd== null){
            		ddd =  driver.identifyCustomer(firma.substring(1), null)  ;          		
            		if(ddd== null){
            			ddd =  driver.identifyCustomer(firma.substring(2), null)  ;	
                		if(ddd == null){
                			ddd =  driver.identifyCustomer(firma.substring(3), null)  ;	
                			if(ddd != null){
                				firma = ddd;
                			}else{
                			}
                			
                    	}else{
                    		firma = ddd;
                    	}
            		}else{
                		firma = ddd;
                	}
            		
            	}else{
            		firma = ddd;
            	}
              }
           
          //   System.out.println("SIIN OLEMA OMA LUGEMISEGA " +  driver.getDriverVersion() + " " +firma +" " + toode + " text2'" + text2+ "' " +warehouse 
            //		  + " text'" + text +  " shipmode'" + shipmode + "'reference'" + reference + "' dealer:" + dealer);
             
              if("true".equals(parent.getProps().getProperty("doknrFilename"))){
              	dokNr = fileName.substring(0,fileName.indexOf(".txt")).replace('.','-');
              }
              if(dokNr!= null && !dokNr.equals("0")){
              	num = dokNr;
              }
              if(inner > 0){
            	  partii = partii + "|"+ inner;
              }
           
              if("true".equals(parent.getProps().getProperty("reference.topartii"))){
            	  partii = reference;
                }
              
              orders.add(firma,toode,name,kogus,num, dealer, bonus, discount,datelong,pl,price,paymterm,false,agcode,status,unit,warehouse,partii,null,shiptime,shipDateUsed,selActCode,docType);
              
              System.out.println(firma +" " + toode + " " + price + " " + 
              num + " pl-" +  pl +  " "+ warehouse + " | " + dealer + "*" + partii +
              " sdu:" + shipDateUsed + " " + photoname + " " + signaname + " " + contactid + " " + fullDeliveryOnly + " typ:" + docType);
            
              if(shipmode.trim().length()>0){
                  orders.setShipMode(shipmode);             
                }
              
              if(photoname != null && photoname.trim().length()>0){
                  orders.setPhotoName(photoname);             
                }
              if(signaname != null && signaname.trim().length()>0){
                  orders.setSignaName(signaname);             
                }
              
              if(contactid != null && contactid.trim().length()>0){
                  orders.setContactID(contactid);             
                }
              
              if(fullDeliveryOnly != null && fullDeliveryOnly.trim().length()>0){
                  orders.setFullDeliveryOnly(fullDeliveryOnly);             
                }
              
              orders.fileName = fileName;
              if(reference != null && reference.trim().length() > 0){
            	 if(reference.indexOf("|") !=-1){
            		 dokNr = reference.substring(0,reference.indexOf("|"));         		 
            	 }else{
            		 dokNr = reference;
            	 }
                      orders.setTeostaja(reference); 
                }
            }
          }
        }
      }
      System.out.println("in order " + linesonorder + " on order " + count   );
      if(linesonorder > 0 && linesonorder != count){
    	  troubles.append("ORDER PROBLEM 'not all lines received ' \n"+ "CustomerCode: " + firma + "\n" 
 				 + "\t\tAgent:" + agcode + "\nIN DROID lines :" + linesonorder + "\nON ORDER lines :" + count + " \n");
    	
    	  
      }
      
      in.close();
      ff.close();     

    } catch (IOException e){
      EksportFrame.showError(e);
    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
   // System.exit(0);
  }

  private void loadWEBOrder(Orders orders, File dokFile,StringBuffer troubles){
    String fileName = dokFile.getName();
    //System.out.println("agent:" + agent);

     File file = new File(fileName);
      file = dokFile;
    try {
      setEmailBody(orders,fileName);

      try{
        File unzipped = null;
        ZipFile zipFile = new ZipFile(file);

        ZipInputStream zin =
            new ZipInputStream(new FileInputStream(file));
        ZipEntry       entry;

        while ((entry = zin.getNextEntry()) != null) {
          BufferedReader in =
              new BufferedReader(new InputStreamReader(zin));
          PrintWriter    out =
              new PrintWriter(new FileOutputStream(driver.getImportFile() + entry.getName()));
          unzipped = new File(driver.getImportFile() + entry.getName());
          String	   line;

          while ((line = in.readLine()) != null) {
            out.println(line);
          }

          out.close();
          zin.closeEntry();
        }

        zin.close();
        zipFile.close();
        file.delete();
        file = unzipped;
      } catch (ZipException e){
      }

      BufferedReader in = new BufferedReader(
          new FileReader(file));

      String line;
      while((line = in.readLine()) != null){
        //System.out.println("line:" + line);
      	String text = "";
        StringTokenizer st =
            new StringTokenizer(line, String.valueOf(ImportPanel.SEPARATOR));
        if (st.hasMoreTokens()){
          String firma = st.nextToken();
          if (st.hasMoreTokens()){
            String toode = st.nextToken();
            if (st.hasMoreTokens()){
              double kogus = Double.parseDouble(st.nextToken());
              String dokNr = null;
              String dealer = null;
              double bonus = -1;
              double discount = 0;
              String name = " ";
              String datelong = null;
              String pl = "0";
              double price = 0;
              int paymterm = 0;
              String unit = "";
              String agcode = null;
              String warehouse = null;
              if (st.hasMoreTokens()){
                  price = Double.parseDouble(st.nextToken());
                        if (st.hasMoreTokens()) {
                            text = "WEB " + st.nextToken();             
                            }
                        if (st.hasMoreTokens()) {
                        	dokNr =  st.nextToken();             
                            }
                            
                         }
                   
                
              
              orders.add(firma,toode,name,kogus,dokNr, dealer, bonus, discount,datelong,pl,price,paymterm,false,
            		  agcode,99,unit,warehouse,"",null,null,true,null,null);
              
              if(text.trim().length()>0){
                orders.setEmailBody(text); 
                orders.setInfo(text);
                System.out.println(firma +" " + toode + " " + text);
              }
            }
          }
        }
      }
      in.close();

      String dealer = getProps().getProperty("dealer");
      if ((dealer != null) && (dealer.equalsIgnoreCase("true") )){
        File archive = new File(file.getParent() + File.separator + "archive");
        if (!archive.exists()){
          archive.mkdir();
        }
        file.renameTo(new File(archive.getAbsolutePath() + File.separator + file.getName()));
     //   file.delete();
      } else {
        file.delete();
      }

    } catch (IOException e){
      EksportFrame.showError(e);
    }
  }
  
  public void showError(Exception e){
    EksportFrame.showError(e);
  }

  public boolean isDebug(){
    return parent.isDebug();
  }

  public void addLog(String s){
    importLogPanel.addText(s);
  }
  
  private String getCal(String ss){
    Calendar cc = Calendar.getInstance();
  	
  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(4,6))-1);
  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(6,8)));
     
  	return cc.getTimeInMillis() +"" ;
  	
  }

  private String getCalRauta(String ss){
	  Calendar cc = Calendar.getInstance();
  		cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(3,5))-1);
  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(0,2)));

  //	this.addLog(date.toLocaleString());
  	return cc.getTimeInMillis() +"" ;
  	
  }
  
  
  private String getCalTel(String ss){

  Calendar cc = Calendar.getInstance();  	
  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(6,10)));
  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(3,5))-1);
  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(0,2)));
  	return cc.getTimeInMillis() +"" ;
  }
  
  
  private String getCalHTM(String ss){
	  Calendar cc = Calendar.getInstance();
	 
	  if(ss.length() == 9){
		  ss = "0" +  ss;
	  }
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(6,10)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(3,5))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(0,2)));
	  	return cc.getTimeInMillis() +"" ;	
  }
  
  private String getCalXML(String ss){
	  Calendar cc = Calendar.getInstance();
	  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(4,6))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(6,8)));
	     
	  	return cc.getTimeInMillis() +"" ;
	  		
  }
  
  
  private Date getCalFel(String ss,int minus){
	  	//06.06.22
	  Calendar cc = Calendar.getInstance();  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,2)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(3,5))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(6,8)));
	 	cc.add(Calendar.DATE, -minus);
	  	return cc.getTime() ;
	   	
	  }

  
  private Date getCalLit(String ss){
	  	//2006.06.22
	  Calendar cc = Calendar.getInstance();  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
	  	return cc.getTime() ;
	   	
	  }
  
  private Date getCalLitTime(String ss){
	  	//2006.06.22
	  Calendar cc = Calendar.getInstance();  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
	  	cc.set(Calendar.HOUR,Integer.parseInt(ss.substring(11,13)));
	  	cc.set(Calendar.MINUTE,Integer.parseInt(ss.substring(14,16)));
	  	cc.set(Calendar.SECOND,Integer.parseInt(ss.substring(17,19)));
	  	return cc.getTime() ;
	   	
	  }
  
  private int getPaydays(long indate,long paydate){
  	
	  Calendar cc = Calendar.getInstance();
	  cc.setTimeInMillis(indate);
	
  Calendar cc2 = Calendar.getInstance();
	  cc2.setTimeInMillis(paydate);
	  
  	Date date3 = new Date(cc2.getTimeInMillis()-cc.getTimeInMillis());
  	
  	long differenceInMillis = date3.getTime() ;
  	long differenceInDays = differenceInMillis /(24*60*60*1000);

  	return (int)differenceInDays;
  	
  }
	  
  private int getPaydays(String indate,String paydate){
  	
	  Calendar cc = Calendar.getInstance();
	  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(indate.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(indate.substring(4,6))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(indate.substring(6,8)));
  	
  Calendar cc2 = Calendar.getInstance();
	  	
	  	cc2.set(Calendar.YEAR,Integer.parseInt(paydate.substring(0,4)));
	  	cc2.set(Calendar.MONTH,Integer.parseInt(paydate.substring(4,6))-1);
	  	cc2.set(Calendar.DATE,Integer.parseInt(paydate.substring(6,8)));
  	Date date3 = new Date(cc2.getTimeInMillis()-cc.getTimeInMillis());
  	
  	long differenceInMillis = date3.getTime() ;
  	long differenceInDays = differenceInMillis /(24*60*60*1000);

  	return (int)differenceInDays;
  	
  }
  
  

  
  public EksportFrame getEksportFrame(){
    return this.parent;
  }

  private JButton importButton = null;
  private String doknr = null;
  private EksportFrame parent;
  public static final char SEPARATOR = ';';
  private AbstractImportDriver driver;
  private HashMap tellimustel = new HashMap() ;
  public double getTellimustel(String kood) {
	  if(tellimustel.get(kood) != null){
		  return Double.parseDouble((String)tellimustel.get(kood));
	  }else{
		  return 0;
	  }
}



public void setTellimustel(String toode,double kogus) {
	
	if(tellimustel.get(toode) != null){
	double ooo = Double.parseDouble((String)tellimustel.get(toode));
	ooo = ooo + kogus;
	    tellimustel.put(toode, ooo+"");
	}else{
		tellimustel.put(toode, kogus+"");
	}
	
}

private String company = "";
  public String firma = "";
  private JLabel importFileField = new JLabel();
  private ImportLogPanel importLogPanel = null;
  private Vector importFileList = new Vector();
  private Timer timer = new Timer(5000, this);
  private Vector convCodes = new Vector();
  private Hashtable kodirovka = new Hashtable();
  private boolean multimode = false;
  public Hashtable getKodirovka() {
	return kodirovka;
}



public void setKodirovka(Hashtable kodirovka) {
	this.kodirovka = kodirovka;
}

static Category log = Category.getRoot();
  public static final String DIR_BODY = "body";
}
