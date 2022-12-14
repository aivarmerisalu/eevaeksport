package eevaeksport.driver;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.GroupLayout.Alignment;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import ee.merisoft.sm.Product;
import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WebDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,FixLoader,FixReporter,WebReporter {

  public WebDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
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
	   
	 JSONArray prods =  Converter.getWebProducts( rcompany, user ,  pass);
	 
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
			e.printStackTrace();
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
		
		this.getProducts(null);

	
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
	 // Teeme SV listi 
	  
	  	try {
	  		String fnameBASE = this.getExcelDir() + "ReportBase.xlsx";
	  		String fnameCHAIN = this.getExcelDir() + "ReportBaseChain.xlsx";
	  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
			FileInputStream excelifailChain = new FileInputStream(fnameCHAIN);
			XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
			XSSFWorkbook  wbCHAIN = new XSSFWorkbook(excelifailChain);
			XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
			XSSFSheet worksheetCHAIN = wbCHAIN.getSheetAt(0);
	  		
	  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), rcompany, user, pass);
	  		System.out.println("files " + files.toString());
	 
  		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, null, null, "test");
  		
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
  			
  			String signa = dateOnly + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
  			System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
  			
  			
  			if(filterOutOfStock != null && filterOutOfStock.startsWith("999") && done != null && !done.contains(signa + "oos")){
  				
  				String res2 = "";
  				try {
  					if((String)cob.get("resstr2") != null) {
	  					res2 = (String)cob.get("resstr2");
	  				}
  				}catch(Exception e) {
  					
  				}
  			
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
  				worksheetBASE.getRow(outofstocks).createCell(4).setCellValue((String)cob.get("custchain"));
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
  				worksheetBASE.getRow(outofstocks).createCell(14).setCellValue("foto");
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
		
		
		
		Converter.saveObject(done, "signadone.obj");

		String exceldir = this.getExcelDir() + "reports/";
  		File ff = new File( exceldir );
  		if(!ff.exists()) {
  			ff.mkdir();
  		}
  		excelifailBase.close();
  		excelifailChain.close();
		
  		
	  		FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		filenames.add(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		wbBASE.write(output_file_oos); //write changes
	  		output_file_oos.close();  //close the stream
  		
  		
  		/*if(prob) {
	  		FileOutputStream output_file_prob =new FileOutputStream(exceldir + "productdetailproblem-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		arreturn.add(exceldir + "productdetailproblem-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	  		wbPROB.write(output_file_prob); //write changes
	  		output_file_prob.close();  //close the stream
  		}*/

		
	  	
	 	}catch(Exception e) {
	  		e.printStackTrace();
	  	}
	  	
	/*  	
	  ArrayList<String> arr = Converter.createExcelProductProblemsOosReport(algus, lopp, this.getExcelDir(), rcompany, user, pass);
	  System.out.println("tulemus " + arr);
	  for(String fname : arr) {
		  String tekst = " ";
		  if(fname.contains("oos")) {
			  tekst = "Out Of Stock Report";
		  }else {
			  tekst = "Distribution Problems Report";
		  }
		  Converter.sendReportEmail(tekst, fname, parent);
	  }
	  */
	  
	  
	  
	  
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
  	Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
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
	  
	  ArrayList<String> arr = Converter.createExcelProductProblemsOosReport(algus, lopp, this.getExcelDir(), rcompany, user, pass);
	  System.out.println("tulemus " + arr);
	  for(String fname : arr) {
		  String tekst = " ";
		  if(fname.contains("oos")) {
			  tekst = "Out Of Stock Report";
		  }else {
			  tekst = "Distribution Problems Report";
		  }
		  Converter.sendReportEmail(tekst, fname, parent);
	  }
	  
	  
	  
	  
	  
	  System.exit(0);
  }

  }
