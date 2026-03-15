package eevaeksport.driver;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.GroupLayout.Alignment;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ee.merisoft.sm.Product;
import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Quest;
import eevaeksport.QuestLine;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WebDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,FixLoader,FixReporter,WebReporter,WebReporter2 {

  public WebDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    companyx = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String companyx = null;
  
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
	   codeEanprods = new TreeMap();
	   if(eanprods == null)
	   eanprods = new TreeMap();
	   
	 JSONArray prods =  Converter.getWebProducts( companyx , user ,  pass);
	 
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
			 codeEanprods.put(code, ean);
			 res.put(name, prod);
			
		} catch (JSONException e) {
			//e.printStackTrace();
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
     return "Web";
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
  private TreeMap codeEanprods = null;
  
  

  @Override
  public void WebReporter() {
	  
	  ArrayList<String> filenames = new ArrayList<String>();
	// teeme kataloogi salvestamiseks
			File f = new File("svdata");
			if(!f.exists()) {
				f.mkdir();
			}
			
			
// loeme signatuurid sisse kui on olemas	
			HashSet done = null;
			try {
				done = (HashSet) Converter.readObject("signadone.obj");
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			if(done == null) {
				 done = new HashSet();
			}
			
			
//teeme perioodi valmis
			Calendar algus = Calendar.getInstance();  
		  	Calendar lopp = Calendar.getInstance();  	
		  	Integer days = 20;
		  if(	parent.getProps().getProperty("webreport.days") != null) {
			  try {
				  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
			  }catch(Exception e) {
				  
			  }
		  };

		  	algus.add(Calendar.DATE, -1 * days);
		  	algus.set(Calendar.HOUR, 0);
		  	lopp.set(Calendar.HOUR, 23);
		  	lopp.set(Calendar.MINUTE, 59);
		  	lopp.add(Calendar.DATE, +2);
			
		  	
		  	try {
		  		String fnameBASE = this.getExcelDir() + "reportbase.xlsx";
		  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
		  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
				XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
				
				CreationHelper createHelper = wbBASE.getCreationHelper();
				
				 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
			      XSSFFont hlinkfont = wbBASE.createFont();
			      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
			      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
			      hlinkstyle.setFont(hlinkfont);

			  	
			  		
			  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), companyx, user, pass, true);
			  	//	System.out.println("files " + files.toString());
			 
		  		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), companyx, user, pass, false, "PRODUCT", null, "test");
		  		
		  		Converter.saveFileInUTF8(f.getPath() + "/currSVjson.json", cc.toString());
		  		int palletrow = 1;
		  		boolean savefile = false;
		  		
		  		for (int i = 0; i < cc.length(); i++) {
		  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
		  		   	String filterOutOfStock = (String)cob.get("resint3").toString();
		  			String dateAndTime = (String)cob.get("recetime");
		  			String dateOnly = dateAndTime.split("T")[0];
		  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
		  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
		  		  String signa = dateAndTime + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
		  		  
		  			if(filterOutOfStock != null && filterOutOfStock.startsWith("333") && done != null && !done.contains(signa )){  	
		  			String filterProblem = (String)cob.get("resstr1");
		  			
		  		  savefile = true;
		  			
		  	    //	String cagid = (String)cob.get("custagent");
		  		//	String cagname = (String)cob.get("custagname");
		  			String agid = (String)cob.get("agentid");
		  			String agname = (String)cob.get("managerName");
		  			if(agname.contains(agid)) {
		  				agname = agname.substring(0,agname.indexOf(agid)).trim();
		  			}
		  			String custname = "";
		  			String custchain = "";
		  			String custaddr = "";
		  			try {
		  				custchain = (String)cob.get("custchain");
		  			} catch(Exception e) {
		  				
		  			}
		  			
		  			try {
		  				custname = (String)cob.get("custname");
		  				if(custname.indexOf(",") != -1) {
		  					custaddr = custname.substring(custname.indexOf(",")+1);
		  					custname = custname.substring(0,custname.indexOf(","));
		  				}
		  			} catch(Exception e) {
		  				
		  			}
		  			
		  		
		  			
		  		//	System.out.println(chain + chDaily + " " + chDaily.contains(chain));
		  	    	
		  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
		  	    	 JSONParser parser = new JSONParser();
		  	    	 
		  	    	org.json.simple.JSONObject obj1 = (org.json.simple.JSONObject) parser.parse((cob.get("comment").toString()));
		  	    
		  	    	String comID = "";
		  	    	String comment = "";
		  	    	
		  	    	try {
		  	    		org.json.simple.JSONObject obj2 = (org.json.simple.JSONObject) parser.parse((cob.get("custcomme").toString()));
		  	    		comID = ((String)obj2.get("comID"));
		  	    		comment = ((String)obj2.get("comNa"));
		  	    	}catch(Exception e) {
		  	    	}
		  	    	String typeid = (String)obj1.get("disID");
		  	    	String typename = (String)obj1.get("disNa");
		  	    	Double typekogus = (Double)obj1.get("disAm");
		  	    	
		  	    	String bonusid = (String)obj1.get("bonID");
		  	    	String bonusname = (String)obj1.get("bonNa");
		  	    	Double bonuskogus = 0d;
		  	    	try {
		  	    		bonuskogus = Double.parseDouble((String)obj1.get("bonAm"));
		  	    	}catch(Exception e) {
		  	    	}
		  	    	
		  	    	Date fromdate = null; //new Date((Long)obj1.get("sDay"));
		  	    	Date todate = null; //new Date((Long)obj1.get("eDay"));
		  	    	try {
		  	    	  fromdate = new Date((Long)obj1.get("sDay"));
		  	    	  todate = new Date((Long)obj1.get("eDay"));
		  	  	     } catch(Exception k) {
	  	    		  k.printStackTrace();
	  	    	     }
		  	    	
		  	    	Date created = new Date(Long.parseLong(((String)cob.get("resstr3"))));
		  	    	
		  	  
		  	    	String cuname = (String)cob.get("custname");
	  				String cuid = (String)cob.get("custid");
	  				if(cuname.endsWith(cuid)) {
	  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
	  				}
		  				
		  				
		  				worksheetBASE.createRow(palletrow);
		  				
		  				worksheetBASE.getRow(palletrow).createCell(0).setCellValue(agname);
		  				
		  			    worksheetBASE.getRow(palletrow).createCell(1).setCellValue(created);
		  				XSSFCell celld3 = worksheetBASE.getRow(palletrow).getCell(1);
		  				XSSFCellStyle cellStyle3 = (XSSFCellStyle)celld3.getCellStyle();
			  	        cellStyle3.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			  	       
		  				worksheetBASE.getRow(palletrow).createCell(2).setCellValue(cuname);
		  				worksheetBASE.getRow(palletrow).createCell(3).setCellValue(custchain);
		  				worksheetBASE.getRow(palletrow).createCell(4).setCellValue(custaddr);

		  				
		  			
		  				//worksheetBASE.getRow(palletrow).createCell(2).setCellValue(typeid);
		  				worksheetBASE.getRow(palletrow).createCell(5).setCellValue(typename);
		  				worksheetBASE.getRow(palletrow).createCell(6).setCellValue(typekogus);
		  				XSSFCell cellk1 = worksheetBASE.getRow(palletrow).getCell(6);
		  				XSSFCellStyle pstyle = worksheetBASE.getRow(0).getCell(6).getCellStyle();
		  				cellk1.setCellStyle(pstyle);
		  				
		  				 
		  				worksheetBASE.getRow(palletrow).createCell(7).setCellValue(fromdate);
		  				XSSFCell celld1 = worksheetBASE.getRow(palletrow).getCell(7);
		  				XSSFCellStyle cellStyle = (XSSFCellStyle)celld1.getCellStyle();
			  	        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
		  				
		  				worksheetBASE.getRow(palletrow).createCell(8).setCellValue(todate);
		  				XSSFCell celld2 = worksheetBASE.getRow(palletrow).getCell(8);
		  				XSSFCellStyle cellStyle2 = (XSSFCellStyle)celld2.getCellStyle();
			  	        cellStyle2.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			  	        
			  	        
			  	        
			  	        worksheetBASE.getRow(palletrow).createCell(9).setCellValue((String)cob.get("prodgroupname"));
			  	      
			  	        worksheetBASE.getRow(palletrow).createCell(10).setCellValue((String)cob.get("resstr2"));
			  	        worksheetBASE.getRow(palletrow).createCell(11).setCellValue((String)cob.get("prodname"));
		  			
		  				//worksheetBASE.getRow(palletrow).createCell(12).setCellValue(bonusid);
		  				
		  				worksheetBASE.getRow(palletrow).createCell(12);
		  				XSSFCell cellk2 = worksheetBASE.getRow(palletrow).getCell(12);
		  				XSSFCellStyle pstyle2 = worksheetBASE.getRow(0).getCell(4).getCellStyle();
		  				cellk2.setCellStyle(pstyle2);
		  				cellk2.setCellValue(bonuskogus);
		  		
		  				// worksheetBASE.getRow(palletrow).createCell(12).setCellValue(comID);
		  				 worksheetBASE.getRow(palletrow).createCell(13).setCellValue(comment);
		  				
		  				
		  				
		  				try {
		  				String foto = null;
		  				org.json.simple.JSONArray phs = (org.json.simple.JSONArray)parser.parse((cob.get("pictureLinks").toString()));
		  				
		  				if(files != null && files.get(cuid) != null && phs != null) {
		  					int a = 0;
		  				//	if(cuid.equals("747724"))
		  						System.out.println("phs " + phs.toString());
		  						for(int y = 0; y < phs.size() ; y++) {
		  							String fname = (String)phs.get(y);
		  						
		  					
		  				
		  					ArrayList arr = (ArrayList)files.get(cuid);
		  					for(int e = 0; e<arr.size(); e++) {
		  						Toode t = (Toode)arr.get(e);
		  						foto = t.getLadu();
		  						if(foto != null && foto.indexOf(fname) !=-1 && t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
		  							
		  							
		  							
		  								
		  				  		         Cell  cell = worksheetBASE.getRow(palletrow).createCell(14+a);
		  				  		         cell.setCellValue("Photo Link "+(a+1) );
		  				  		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
		  				  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
		  				  		         cell.setHyperlink((XSSFHyperlink) link);
		  				  		         cell.setCellStyle(hlinkstyle);
		  				  		     a++;
		  				  				
		  						}
		  					}
		  						}
		  						
		  				}
		  				}catch(Exception e) {
		  					e.printStackTrace();
		  				}
		  				 //URL Link
		  				
		  				
		  				
		  			 
		  				worksheetBASE.getRow(palletrow).createCell(18).setCellValue((String)cob.get("prodid"));
		  				worksheetBASE.getRow(palletrow).createCell(19).setCellValue(agid);
		  				worksheetBASE.getRow(palletrow).createCell(20).setCellValue("LV");
		  				worksheetBASE.getRow(palletrow).createCell(21).setCellValue((String)cob.get("custid"));
		  				worksheetBASE.getRow(palletrow).createCell(23).setCellValue(bonusid);
		  				worksheetBASE.getRow(palletrow).createCell(23).setCellValue(comID);
		  				//
		  				
		  				
		  				
		  			/*	for(int x=0; x<11; x++) {
		  					if(x == 0 || x == 1 || x == 3) {
		  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderCenteredOOS);
		  					}else {
		  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderOOS);
		  					}
		  				}*/
		  				palletrow++;
		  				done.add(signa );
		  			
		       			
		       			
		  			}
		  		}
		  		String exceldir = this.getExcelDir() + "reports/";
		  		File ff = new File( exceldir );
		  		if(!ff.exists()) {
		  			ff.mkdir();
		  		}
		  		excelifailBase.close();
		  		
if(savefile) {
FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	//filenames.add(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	wbBASE.write(output_file_oos); //write changes
	output_file_oos.close();  //close the stream
	System.out.println("report saved " + exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
  
}else {
System.out.println("no data to save");
}
		  	
		  	}catch(Exception e) {
				 e.printStackTrace(); 
			  }
		  	
		  	
			try {
				Converter.saveObject(done, "signadone.obj");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		  
		  System.exit(0);
  }
  
  



  
  
  

  public void WebReporterZP() {
	  
	  ArrayList<String> filenames = new ArrayList<String>();
// teeme kataloogi salvestamiseks
		File f = new File("svdata");
		if(!f.exists()) {
			f.mkdir();
		}
		
		
	// loeme signatuurid sisse kui on olemas	
		HashSet done = null;
		try {
			done = (HashSet) Converter.readObject("signadone.obj");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(done == null) {
			 done = new HashSet();
		}
		  ArrayList chDaily = new ArrayList();
		  ArrayList chWED = new ArrayList();
		  if(parent.getProps().getProperty("chainlistWED") != null) {
			  chWED = new ArrayList<String>(Arrays.asList(parent.getProps().getProperty("chainlistWED").split(",")));
		  }
		  
	      if(parent.getProps().getProperty("chainlistDAILY") != null) {
	    	  chDaily = new ArrayList<String>(Arrays.asList(parent.getProps().getProperty("chainlistDAILY").split(",")));  
		  }
	      
	      System.out.println("arrWED " + chWED.toString());
	      System.out.println("arrDAILY " + chDaily.toString());
		  
		ArrayList hasDaily = new ArrayList();
		ArrayList hasWednesday = new ArrayList();
		
		
		this.getProducts(null);

	
		Calendar algus = Calendar.getInstance();  
	  	Calendar lopp = Calendar.getInstance();  	
	  	Integer days = 2;
	  if(	parent.getProps().getProperty("webreport.days") != null) {
		  try {
			  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
		  }catch(Exception e) {
			  
		  }
	
	  };

	  
	  days = 2;
	  	algus.add(Calendar.DATE, -1 * days);
	  	algus.set(Calendar.HOUR, 0);
	  	lopp.set(Calendar.HOUR, 23);
	  	lopp.set(Calendar.MINUTE, 59);
	  	lopp.add(Calendar.DATE, +2);
	 // Teeme SV listi 
	  
	  	try {
	  		String fnameBASE = this.getExcelDir() + "ReportBase.xlsx";
	  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
	  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
			
			XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
			
			
			
			CreationHelper createHelper = wbBASE.getCreationHelper();
			
			 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
		      XSSFFont hlinkfont = wbBASE.createFont();
		      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
		      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
		      hlinkstyle.setFont(hlinkfont);

			
	  		
	  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), companyx, user, pass, true);
	  	//	System.out.println("files " + files.toString());
	 
  		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), companyx, user, pass, false, null, null, "test");
  		
  		Converter.saveFileInUTF8(f.getPath() + "/currSVjson.json", cc.toString());
  		int outofstocks = 1;
		for (int i = 0; i < cc.length(); i++) {
  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
  			
  			String filterProblem = (String)cob.get("resstr1");
  			String filterOutOfStock = (String)cob.get("resint3").toString();
  			String dateAndTime = (String)cob.get("recetime");
  			String dateOnly = dateAndTime.split("T")[0];
  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
  			
  	    	String cagid = (String)cob.get("custagent");
  			String cagname = (String)cob.get("custagname");
  			String agid = (String)cob.get("agentid");
  			String agname = (String)cob.get("managerName");
  			String chain = (String)cob.get("custchain");
  			
  		//	System.out.println(chain + chDaily + " " + chDaily.contains(chain));
  			
  		
  			
  			String signa = dateOnly + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
  			
  		
       		
   			
  			
   			if(!done.contains(signa + "chain")) {
   				if(chWED.contains(chain)) {
 					if(!hasWednesday.contains(chain))
 						hasWednesday.add(chain);
 				}
     				if(chWED.contains(chain)) {
     					if(!hasWednesday.contains(chain))
     						hasWednesday.add(chain);
     				}
       			}
       			
       			
  			
  			if(filterOutOfStock != null && filterOutOfStock.startsWith("999") && done != null && !done.contains(signa + "oos")){
  				
  				
  				worksheetBASE.createRow(outofstocks);
  				worksheetBASE.getRow(outofstocks).createCell(0).setCellValue(agname);
  				String cuname = (String)cob.get("custname");
  				String cuid = (String)cob.get("custid");
  				if(cuname.endsWith(cuid)) {
  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
  				}
  				//worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agid);
  				worksheetBASE.getRow(outofstocks).createCell(2).setCellValue(cuname);
  				worksheetBASE.getRow(outofstocks).createCell(3).setCellValue(cuid);
  			
  				worksheetBASE.getRow(outofstocks).createCell(4).setCellValue(chain);
  			
  				worksheetBASE.getRow(outofstocks).createCell(5).setCellValue(dateOnly);
  				worksheetBASE.getRow(outofstocks).createCell(6).setCellValue(timeOnly);
  				//worksheetBASE.getRow(outofstocks).createCell(7).setCellValue(res2);
  				worksheetBASE.getRow(outofstocks).createCell(8).setCellValue((String)cob.get("prodid"));
  				worksheetBASE.getRow(outofstocks).createCell(9).setCellValue(codeEanprods.get((String)cob.get("prodid"))+"");
  				worksheetBASE.getRow(outofstocks).createCell(10).setCellValue((String)cob.get("prodname"));
  				worksheetBASE.getRow(outofstocks).createCell(11).setCellValue((String)cob.get("prodgroupname"));
  				worksheetBASE.getRow(outofstocks).createCell(12).setCellValue((String)cob.get("prodtypename"));
  				try {
  	  				worksheetBASE.getRow(outofstocks).createCell(13).setCellValue((String)cob.get("comment"));
  	  				}catch(Exception e) {
  	  					
  	  				}
  				String foto = null;
  				if(files != null && files.get(cuid) != null) {
  					
  				//	if(cuid.equals("747724"))
  					//	System.out.println(files.get(cuid).toString());
  					
  					
  					ArrayList arr = (ArrayList)files.get(cuid);
  					for(int e = 0; e<arr.size(); e++) {
  						Toode t = (Toode)arr.get(e);
  						if(!t.getID().equals("prodid")) {
  							
  					//		if(cuid.equals("747724"))
  						//System.out.println(t.getID()+ " " +  (String)cob.get("prodid") +" " + agid+ " " + t.comment +" " + t.getGr1() + " "  + dateOnly );
  							
  						if(t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
  							foto = t.getLadu();
  						}
  					}}
  				}
  				
  				 //URL Link
  				if(foto != null) {
  		         Cell  cell = worksheetBASE.getRow(outofstocks).createCell(14);
  		         cell.setCellValue("Photo Link");
  		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
  		         cell.setHyperlink((XSSFHyperlink) link);
  		         cell.setCellStyle(hlinkstyle);
  				}
  				worksheetBASE.getRow(outofstocks).createCell(15).setCellValue(cagname);
  				
  				
  				
  			/*	for(int x=0; x<11; x++) {
  					if(x == 0 || x == 1 || x == 3) {
  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderCenteredOOS);
  					}else {
  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderOOS);
  					}
  				}*/
  				outofstocks++;
  				done.add(signa + "oos");
  			}
  			
  			}
	

		String exceldir = this.getExcelDir() + "reports/";
  		File ff = new File( exceldir );
  		if(!ff.exists()) {
  			ff.mkdir();
  		}
  		excelifailBase.close();
  		

  		FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
  		//filenames.add(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
  		wbBASE.write(output_file_oos); //write changes
  		output_file_oos.close();  //close the stream
  		
  	
  		System.out.println("hasDaily " + hasDaily.toString());
  		
  		if(hasDaily.size() >0) {
  		for(int u = 0; u < hasDaily.size(); u++)	{
  		String chain = (String) hasDaily.get(u);
  		
  		boolean empty = true;
  		
  		//Siin teeme kettidele aruanded valmis
  	
	  	String fnameCHAIN = this.getExcelDir() + "ReportBaseChain.xlsx";
		FileInputStream excelifailChain = new FileInputStream(fnameCHAIN);
		XSSFWorkbook  wbCHAIN = new XSSFWorkbook(excelifailChain);
		XSSFSheet worksheetCHAIN = wbCHAIN.getSheetAt(0);
	     
	  	CreationHelper createHelper2 = wbCHAIN.getCreationHelper();
		
		 XSSFCellStyle hlinkstyle2 = wbCHAIN.createCellStyle();
	      XSSFFont hlinkfont2 = wbCHAIN.createFont();
	      hlinkfont2.setUnderline(XSSFFont.U_SINGLE);
	      hlinkfont2.setColor(IndexedColors.BLUE.getIndex());
	      hlinkstyle2.setFont(hlinkfont2);
		
  		
	 	 outofstocks = 1;
			for (int i = 0; i < cc.length(); i++) {
	  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
	  			
	  			String filterProblem = (String)cob.get("resstr1");
	  			String filterOutOfStock = (String)cob.get("resint3").toString();
	  			String dateAndTime = (String)cob.get("recetime");
	  			String dateOnly = dateAndTime.split("T")[0];
	  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
	  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
	  			
	  	    	String cagid = (String)cob.get("custagent");
	  			String cagname = (String)cob.get("custagname");
	  			String agid = (String)cob.get("agentid");
	  			String agname = (String)cob.get("managerName");
	  			String chJ = (String)cob.get("custchain");
	  			
	  			String signa = dateOnly + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
	  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
	  			
	  			
	  			if(filterOutOfStock != null && filterOutOfStock.startsWith("999") && done != null && !done.contains(signa + "chain") && chJ != null && chJ.equals(chain)){
	  				
	  				empty = false;
	  			
	  				worksheetCHAIN.createRow(outofstocks);
	  				//worksheetCHAIN.getRow(outofstocks).createCell(0).setCellValue(agname);
	  				String cuname = (String)cob.get("custname");
	  				String cuid = (String)cob.get("custid");
	  				if(cuname.endsWith(cuid)) {
	  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
	  				}
	  				//worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agid);
	  				worksheetCHAIN.getRow(outofstocks).createCell(1).setCellValue(cuname);
	  				worksheetCHAIN.getRow(outofstocks).createCell(2).setCellValue(dateOnly);
	  				worksheetCHAIN.getRow(outofstocks).createCell(3).setCellValue(timeOnly);
	  				
	  			//	worksheetCHAIN.getRow(outofstocks).createCell(4).setCellValue(chain);
	  				worksheetCHAIN.getRow(outofstocks).createCell(5).setCellValue(codeEanprods.get((String)cob.get("prodid"))+"");
	  				worksheetCHAIN.getRow(outofstocks).createCell(6).setCellValue((String)cob.get("prodname"));
	  				
	  				
	  				
	  				try {
	  					worksheetCHAIN.getRow(outofstocks).createCell(7).setCellValue((String)cob.get("comment"));
	  	  				}catch(Exception e) {
	  	  					
	  	  				}
	  				String foto = null;
	  				if(files != null && files.get(cuid) != null) {
	  					ArrayList arr = (ArrayList)files.get(cuid);
	  					for(int e = 0; e<arr.size(); e++) {
	  						Toode t = (Toode)arr.get(e);
	  						if(!t.getID().equals("prodid")) {
	  					//	System.out.println(t.getID()+ " " +  (String)cob.get("prodid") +" " + agid+ " " + t.comment +" " + t.getGr1() + " "  + dateOnly );
	  						if(t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
	  							foto = t.getLadu();
	  						}
	  					}}
	  				}
	  				
	  				 //URL Link
	  				if(foto != null) {
	  		         Cell  cell = worksheetCHAIN.getRow(outofstocks).createCell(8);
	  		         cell.setCellValue("Photo Link");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}
	  				//worksheetCHAIN.getRow(outofstocks).createCell(15).setCellValue(cagname);
	  			
	  				outofstocks++;
	  				done.add(signa + "chain");
	  			}
	  			
	  			
	  			
	  			}
			
  			excelifailChain.close();
  if(!empty) {
	  		FileOutputStream output_file_chain =new FileOutputStream(exceldir + "productOOSChain-" + chain + "-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		filenames.add(exceldir + "productOOSChain-" + chain + "-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  	  	
	  		wbCHAIN.write(output_file_chain); //write changes
	  		output_file_chain.close();  //close the stream
	  	}
  		}
  		}
  	
	  		
  		System.out.println("hasWednesday " + hasWednesday.toString());
  		Calendar cal = Calendar.getInstance();
  		if(hasWednesday.size() >0 && cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
  		for(int u = 0; u < hasWednesday.size(); u++)	{
  		String chain = (String) hasWednesday.get(u);
  		
  	  boolean empty = true;
  		
  		//Siin teeme kettidele aruanded valmis
  	
	  	String fnameCHAIN = this.getExcelDir() + "ReportBaseChain.xlsx";
		FileInputStream excelifailChain = new FileInputStream(fnameCHAIN);
		XSSFWorkbook  wbCHAIN = new XSSFWorkbook(excelifailChain);
		XSSFSheet worksheetCHAIN = wbCHAIN.getSheetAt(0);
	     
	  	CreationHelper createHelper2 = wbCHAIN.getCreationHelper();
		
		 XSSFCellStyle hlinkstyle2 = wbCHAIN.createCellStyle();
	      XSSFFont hlinkfont2 = wbCHAIN.createFont();
	      hlinkfont2.setUnderline(XSSFFont.U_SINGLE);
	      hlinkfont2.setColor(IndexedColors.BLUE.getIndex());
	      hlinkstyle2.setFont(hlinkfont2);
		
  		
	 	 outofstocks = 1;
			for (int i = 0; i < cc.length(); i++) {
	  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
	  			
	  			String filterProblem = (String)cob.get("resstr1");
	  			String filterOutOfStock = (String)cob.get("resint3").toString();
	  			String dateAndTime = (String)cob.get("recetime");
	  			String dateOnly = dateAndTime.split("T")[0];
	  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
	  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
	  			
	  	    	String cagid = (String)cob.get("custagent");
	  			String cagname = (String)cob.get("custagname");
	  			String agid = (String)cob.get("agentid");
	  			String agname = (String)cob.get("managerName");
	  			String chJ = (String)cob.get("custchain");
	  			
	  			String signa = dateOnly + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
	  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
	  			
	  			
	  			if(filterOutOfStock != null && filterOutOfStock.startsWith("999") && done != null && !done.contains(signa + "chain2") && chJ != null && chJ.equals(chain)){
	  				
	  			empty = false;
	  			
	  				worksheetCHAIN.createRow(outofstocks);
	  				//worksheetCHAIN.getRow(outofstocks).createCell(0).setCellValue(agname);
	  				String cuname = (String)cob.get("custname");
	  				String cuid = (String)cob.get("custid");
	  				if(cuname.endsWith(cuid)) {
	  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
	  				}
	  				//worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agid);
	  				worksheetCHAIN.getRow(outofstocks).createCell(1).setCellValue(cuname);
	  				worksheetCHAIN.getRow(outofstocks).createCell(2).setCellValue(dateOnly);
	  				worksheetCHAIN.getRow(outofstocks).createCell(3).setCellValue(timeOnly);
	  				
	  			//	worksheetCHAIN.getRow(outofstocks).createCell(4).setCellValue(chain);
	  				worksheetCHAIN.getRow(outofstocks).createCell(5).setCellValue(codeEanprods.get((String)cob.get("prodid"))+"");
	  				worksheetCHAIN.getRow(outofstocks).createCell(6).setCellValue((String)cob.get("prodname"));
	  				
	  				
	  				
	  				try {
	  					worksheetCHAIN.getRow(outofstocks).createCell(7).setCellValue((String)cob.get("comment"));
	  	  				}catch(Exception e) {
	  	  					
	  	  				}
	  				String foto = null;
	  				if(files != null && files.get(cuid) != null) {
	  					ArrayList arr = (ArrayList)files.get(cuid);
	  					for(int e = 0; e<arr.size(); e++) {
	  						Toode t = (Toode)arr.get(e);
	  						if(!t.getID().equals("prodid")) {
	  						//System.out.println(t.getID()+ " " +  (String)cob.get("prodid") +" " + agid+ " " + t.comment +" " + t.getGr1() + " "  + dateOnly );
	  						if(t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
	  							foto = t.getLadu();
	  						}
	  					}}
	  				}
	  				
	  				 //URL Link
	  				if(foto != null) {
	  		         Cell  cell = worksheetCHAIN.getRow(outofstocks).createCell(8);
	  		         cell.setCellValue("Photo Link");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}
	  			//	worksheetCHAIN.getRow(outofstocks).createCell(15).setCellValue(cagname);
	  			
	  				outofstocks++;
	  				done.add(signa + "chain2");
	  			}
	  			
	  			
	  			
	  			}
			
  			excelifailChain.close();
  		  if(!empty) {
	  		FileOutputStream output_file_chain =new FileOutputStream(exceldir + "productOosChainWed-" + chain + "-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		filenames.add(exceldir + "productOosChainWed-" + chain + "-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  	//	arreturn.add(exceldir + "productdetailproblem-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
		  	
	  		wbCHAIN.write(output_file_chain); //write changes
	  		output_file_chain.close();  //close the stream
	  	}
  		}
  		}
		
	  	
	 	}catch(Exception e) {
	  		e.printStackTrace();
	  	}
	  	
	 	
	
	  System.out.println("filenames " + filenames);
	  for(String fname : filenames) {
		  String tekst = " ";
		 
			  tekst = "Out Of Stock Report CHAIN ";
		
		  Converter.sendReportEmail(tekst, fname, parent,null);
	  }
	  
	  
	  
		try {
			Converter.saveObject(done, "signadone.obj");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	  
	  System.exit(0);
  }
  
  
  @Override
  public void FixLoader() {
  	
  eanprods =  Converter.loadMore();
  	  
  	  if(eanprods == null) {
  		  eanprods = new TreeMap();
  	  }
  	this.getProducts(null);
  	TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  	System.out.println("Contracts " + contracts.toString());
  	Converter.sendContractJson(contracts ,  companyx,  user ,  pass);
  	System.exit(0);
  }
  
  
  @Override
  public void FixReporter() {
		Calendar algus = Calendar.getInstance();  
	  	Calendar lopp = Calendar.getInstance();  	
	  	Integer days = 3;
	  if(	parent.getProps().getProperty("webreport.days") != null) {
		  try {
			  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
		  }catch(Exception e) {
			  
		  }
	
	  };
	  	algus.add(Calendar.DATE, -1 * days);
	  	algus.set(Calendar.HOUR, 0);
	  	lopp.set(Calendar.HOUR, 23);
	  	lopp.set(Calendar.MINUTE, 59);
	  	lopp.add(Calendar.DATE, +2);
	  
	  ArrayList<String> arr = Converter.createExcelProductProblemsOosReport(algus, lopp, this.getExcelDir(), companyx, user, pass);
	  System.out.println("tulemus " + arr);
	  for(String fname : arr) {
		  String tekst = " ";
		  if(fname.contains("oos")) {
			  tekst = "Out Of Stock Report";
		  }else {
			  tekst = "Distribution Problems Report";
		  }
		  Converter.sendReportEmail(tekst, fname, parent,null);
	  }
	  
	  System.exit(0);
  }

@Override
public void WebReporter2() {
	Converter.uploadQuestsAndAnswers(companyx, user, pass, parent.getProps(),this.getExcelDir());
	System.out.println("web " );
	System.exit(0);
}



  }
