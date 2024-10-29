package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DaividaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public DaividaDBDriver(EksportPanel parent) {
    super(parent);
    PropertyConfigurator.configure("log4j.properties");
    this.subjectFilesTo = parent.getString("filemsg");	
     country = getParent().getProps().getProperty("companycode");
  }
  
   
  private String country = null;
  static Category log = Category.getRoot();
  
  private String getStringFromFile(String file){
	  StringBuilder sb = new StringBuilder();
	  try {
		  
		  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getParent().getProps().getProperty("data.dir")
				  +  file),"UTF8"));
			    
	      String line = br.readLine();

	      while (line != null) {
	          sb.append(line);
	         // sb.append(System.lineSeparator());
	          line = br.readLine();
	      }
	      br.close();

	  } catch (Exception e) {
		  e.printStackTrace();
	  } finally {
	   
	  }
	 return sb.toString();	
  }
  
  
  public String getSaveDebtInfo() {
  	  return null;
   }
 
  
  public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day );
		
		sb.append("." + month  + ".");
		sb.append("" + cal.get(Calendar.YEAR));
		
	    return sb.toString();
	  }

  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	 public Vector getPriceMatrixData() {
		return priceass;
	}
	
   public Vector getPriceMatrixDataXX() {

   	Vector lines = new Vector();
   	fixedass = new Vector();
   	prices = new HashMap();
   	usedCustCodes = new HashSet();
   	
   		InputSource xml = new InputSource(new StringReader(getStringFromFile("PriceMeriEsb.xml")));	
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//pi_mstr").evaluate(xml, XPathConstants.NODESET);
			
		for (int i = 0 ; i < nodeList.getLength() ; i++){
			Node o = nodeList.item(i);
			   
			double price = 0;
				String custID = null;
				String clGroup = null;
				String prodID = null;
				String discGroup = null;
				String start = null;
				String end = null;
				String listname = null;
				String lineinfo = null;
				String discT = null;
				double discount = 0;
	            String type = null;  
	
				NodeList ll = o.getChildNodes();
			for (int r = 0 ; r < ll.getLength() ; r++){
				Node nn = ll.item(r);
				
				  if(nn.getNodeName().equals("piAmtType")){
					   try{  type = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
					   }catch(Exception e){ }
				   }
	
				  
				  if(nn.getNodeName().equals("piList")){
					   try{  listname = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
					   }catch(Exception e){ }
				   }
	
				  if(nn.getNodeName().equals("piStart")){
					   try{  start = nn.getFirstChild().getNodeValue();
					   }catch(Exception e){ }
				   }
				  if(nn.getNodeName().equals("piExpire")){
					   try{  end = nn.getFirstChild().getNodeValue();
					   }catch(Exception e){ }
				   }
		  if(nn.getNodeName().equals("piCsCode")){
			   try{  clGroup = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("piPartCode")){
			   try{  prodID = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("piListPrice")){
			   try{  price = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
 
		  
		  /*
		          <qdoc:pid_det>
                  <qdoc:operation/>
                  <qdoc:pidAmt>0.74</qdoc:pidAmt>
                  <qdoc:pidDomain>D012LT</qdoc:pidDomain>
                  <qdoc:pidListId>103218</qdoc:pidListId>
                  <qdoc:pidQty>0</qdoc:pidQty>
               </qdoc:pid_det>
*/
		 				
			
		
		  if(nn.getNodeName().equals("pid_det")){
			  
 NodeList llx = nn.getChildNodes();
			  
			  for (int rr = 0 ; rr < llx.getLength() ; rr++){
				  Node nnn = llx.item(rr);
				  if(nnn.getNodeName().equals("pidAmt")){
					   try{ 
						   if(discount == 0){
							   discount = Double.parseDouble(nnn.getFirstChild().getNodeValue());
							   discT = discount + "";
							}else{
								discT = nnn.getFirstChild().getNodeValue();
							}
					   }catch(Exception e){ }
				  }
				  
				  if(nnn.getNodeName().equals("pidQty")){
					   try{ 
						   String aaa =  nnn.getFirstChild().getNodeValue();
						   if(!aaa.equals("0")){
							   if(lineinfo == null){
								   lineinfo = "Kiek." + aaa + " Kain." + discT + "€";
							   }else{
								   lineinfo = lineinfo + "|Kiek." + aaa + " Kain." + discT + "€";
							   }
							}
					   }catch(Exception e){ }
				  }
			 }
		   }
		}
		  
		  
			 MatrixLine mline = new MatrixLine();

		if (custID != null && custID.trim().length() > 0) {
			mline.setCustID(custID.trim());
			if(!usedCustCodes.contains(custID.trim())){
				usedCustCodes.add(custID.trim());
			}
		}
		if (mline.getCustID() == null && clGroup != null
				&& clGroup.trim().length() > 0) {
			mline.setCliGroup(clGroup.trim());
			if(!usedCustCodes.contains(clGroup.trim())){
				usedCustCodes.add(clGroup.trim());
			}
		}

		if (prodID != null && prodID.trim().length() > 0) {
			mline.setProdID(prodID.trim());
		}
		if (mline.getProdID() == null && discGroup != null
				&& discGroup.trim().length() > 0) {
			mline.setDiscGroup(discGroup);
			
		}
		//mline.setDiscount(discount);

		
		if((listname != null && listname.trim().startsWith("AK"))){
			//System.out.println( clGroup + "'" + prodID + "'" + discGroup + "'" + discount +  " " 
				//		+ type + " " + start + " " + end + " " + lineinfo) ;
				
			     
			     if(start != null ){
						mline.setStartDate(start);
						if(end != null){
							mline.setEndDate(end);
						}else{
							mline.setEndDate(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
						}
						
					}
				}
				
		
		if((type != null && type.equals("4")) ){
	//System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount +  " " 
			//	+ type + " " + start + " " + end + " " + lineinfo) ;
		
			if(start != null ){
				mline.setStartDate(start);
				if(end != null){
					mline.setEndDate(end);
				}else{
					mline.setEndDate(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
				}
			
			}
		}
		

		
		
		 
		
		//vaatame ka on hapu 
		boolean validdate = true;
		
		
		
		if(validdate){
			
		
		if (price != 0 || discount  > 0) {
			 if(discount > 0){
					price = discount;
				}
				mline.setPrice(price);
			
			   if(prices.get(prodID) != null){
					double pr = Double.parseDouble((String)prices.get(prodID));
					if(price > pr)
					prices.put(prodID, price + "");
				}else{
					prices.put(prodID, price + "");
				}
			  
		}
		
		
		if(clGroup != null && prodID != null && price > 0){
			if(lineinfo != null){
				mline.setLineinfo(lineinfo);
				//System.out.println("Lineinfo " + lineinfo);
			}
			lines.add(mline);
		}
		}
			}
			
			
				
		

	} catch (Exception ex) {
		ex.printStackTrace();
	}
   	System.out.println("Price matrix tehtud " + lines.size() + "\nPrices size" + prices.size());
   	fixedass = (Vector)lines.clone();
	return lines;

   }
   private HashSet usedCustCodes = null;

   private HashMap prices = new HashMap();
	
   
   private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:report", "mobile", "mobile");
	  }
   
   
   
   private TreeMap getGrupid(){
	      File fff = new File(this.getExcelDir() + "Sortiment.xls" );
	      TreeMap groups = new TreeMap(); 
	  	try{
	          FileInputStream ff = new FileInputStream(fff);
	  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
	  		HSSFSheet sheet = workbook.getSheetAt(1);
	  		HSSFRow row = null;
	  		
	  		row = sheet.getRow(0);
	  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  		
	  		//System.out.println("rownum " + row.getLastCellNum());
	  		
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum();a++){
	  			try{
	  			//	Loeme sisse rea
	  				row = sheet.getRow(a);
	  				if (row.getCell((short) 0) != null) {
	  				


	  	    			   String kood = null;
	  	    			  if(row.getCell((short)0) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(kood.indexOf(".")!= -1){
		    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
		    	    			  	 }
		    	    			  	 String sort = null;
		    	    			  	try{
		    	    			  		sort = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    	    		} 
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
		    	    			  	 groups.put(kood,sort);
	  	    			  
	  	    				
	  				}	   
	  				}
	 			}catch (Exception e){
	  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
	  			}
	  		}
	  		
	  		
	  
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
	  	 
	  	 System.out.println("grupid suurus " + groups.size() );
	      
	     return groups; 
	  	 
	  }

   
   
  
   private HashMap prodHash = new HashMap();
   
	    private String getYYYYpMMpDDpTimeNiceString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("-" + month  + "-");
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		  }


   private Connection con2 = null;
   
   Hashtable prodCodes = null;
   
  
  
   private Vector fixedass = new Vector();
   private Vector priceass = new Vector();
   
   
   public Vector getFixedAssortmentData() {
	  return fixedass;

	}

      
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
              return lines;
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	          vec.addElement(inVec.elementAt(i));
	        }
	        Vector sorted = new Vector(vec.size());
	        while(vec.size() > 0){
	          MatrixLine smallest = null;
	          Product smProd = null;
	          for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
	        	  MatrixLine ol = (MatrixLine)enumr.nextElement();
	            Product prod = (Product)products.get(ol.getProdID());
           //   System.out.println("Prod " + prod + "  " + ol.getProdID());
	            if ( (smallest == null && smProd == null )
	            		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
	              smallest = ol;
	              smProd = (Product)products.get(smallest.getProdID());
	            }
	          }
	          vec.removeElement(smallest);
	          sorted.addElement(smallest);
	        }
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }


 
       public String getSaveStockInfo(){
    	   this.getProducts(null);
    	   StringBuffer products = new StringBuffer();
    	  
    	 
          if(this.stocks != null && this.stocks.size() > 0){
        	 Iterator itera =  stocks.keySet().iterator();
          
              while(itera.hasNext()){
                	   
                	String id = itera.next() + "";
                	double kogus = stocks.get(id);
                	int kog = (int)(kogus);
                	if(kog != 0){
                	   products.append(id +  ";");
 		               products.append(" ;");
 		               products.append(getParent().getNF().format(kog) + "; "  +  "\n");
                	}
              }
          }
       return products.toString();

       }

       
       
         
       public Vector getDebt(String agentCode){
    	   return debtList;
       }
       
     private  Vector debtList = null;
   
     private HashMap<String,Double> overx = null;

     private HashMap<String,Double> used = null;
     public Vector getDebtX(){
    	
    	 overx = new HashMap<String,Double>();
    	 used = new HashMap<String,Double>();
    	 debtList = new Vector();
	 
	   
	   InputSource xml = new InputSource(new StringReader(getStringFromFile("DebtMeriEsb.xml")));	
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//ar_mstr").evaluate(xml, XPathConstants.NODESET);
			
		for (int i = 0 ; i < nodeList.getLength() ; i++){
		
			Node o = nodeList.item(i);
		
		   String saaja = null;

		   String maksja = null;
		   String number = null;
           Date duedate =  null;
           Date date =  null;
           double volg = 0;
          
           
			NodeList ll = o.getChildNodes();
		for (int r = 0 ; r < ll.getLength() ; r++){
			Node nn = ll.item(r);
			
	  if(nn.getNodeName().equals("arCust")){
		   try{  saaja = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
		
		
	  if(nn.getNodeName().equals("arBill")){
		   try{  saaja = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
		
	  
	  if(nn.getNodeName().equals("arNbr")){
		   try{  number = nn.getFirstChild().getNodeValue();
		   }catch(Exception e){ }
	   }

	  if(nn.getNodeName().equals("arDueDate")){
		   try{  duedate = getDate(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }

	  if(nn.getNodeName().equals("arEffdate")){
		   try{  date = getDate(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
	  
	  if(nn.getNodeName().equals("arOpenAmt")){
		   try{  volg = Double.parseDouble(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
		}
		
	          Debt debt = new Debt();
	          debt.setCustID(saaja);
	          debt.setDoknr(number);
	     if(duedate != null){
	          debt.setDuedate(duedate);
	          int due = getDue(debt.getDuedate().getTime()) ;
	          debt.setDue(due+"");	    	 
              debt.setDate(date);
	          debt.setSum(volg);
	        		if(used.get(saaja) == null ){
	        			used.put(saaja, volg);
	        		}else{
	        			used.put(saaja,used.get(saaja) + volg);
	        		}
	        	 if(due > 0){
	        		 if(overx.get(saaja) == null ){
		        			overx.put(saaja, volg);
		        		}else{
		        			overx.put(saaja,overx.get(saaja) + volg);
		        		} 
	        	     }
	        	  debtList.addElement(debt);  
	     }

	      //    System.out.println("Debt " + debt.getCustID() + " " + debt.getSum());
	          
		}
		}catch(Exception m){
			m.printStackTrace();
		}
		
		   
	   
	   System.out.println("DebtList size " + debtList.size());
        return debtList;
  }

   
   private int getDue(long dd){
	      int res = 0;
	      res =(int) ((-dd+(new java.util.Date()).getTime())/86400000) ;
	      return res ;
	   }
   
  	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0,4));
		int MONTH = Integer.parseInt(kuup.substring(5,7))-1;
		int DAY = Integer.parseInt(kuup.substring(8,10));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
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
       
       
       
   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     
     baskets = new TreeMap();
     types = new TreeMap();
     prodSorting = new TreeMap();
     
  	try {

		InputSource xml = new InputSource(new StringReader(getStringFromFile("ItemGrpMeriEsb.xml")));

		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile("//dsOutput").evaluate(xml, XPathConstants.NODESET);
				
		for (int i = 0 ; i < nodeList.getLength() ; i++){
			Node o = nodeList.item(i);
			
			String grpCode = null;
			String grpDesc = null;
			String grpType = null;
			String sorting = null;
			
					NodeList lll = o.getChildNodes();
					
					for (int r = 0 ; r < lll.getLength() ; r++){
							
						
								Node nnn2 = lll.item(r);
								
								
								NodeList lll2 = nnn2.getChildNodes();
								
								for (int w = 0 ; w < lll2.getLength() ; w++){
									Node nnn = lll2.item(w);
								
								
						if(nnn.getNodeName().equals("grpCode")){
							   try{  grpCode = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
						

						  if(nnn.getNodeName().equals("grpDesc")){
							   try{  grpDesc = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
						  
						  if(nnn.getNodeName().equals("codeFldName")){
							   try{  grpType = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }

						  if(nnn.getNodeName().equals("sorting")){
							   try{  sorting = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
							
						 // System.out.println(grpType + " " + grpCode);
						  if(grpCode != null && grpDesc != null){
							  tm.put(grpCode, Converter.enCodeEst(grpDesc  ));
						  }

						  
						  if(grpType != null && grpCode != null){
								
					if(grpType.equals("pt__chr08")){
						
						baskets.put(grpCode, Converter.enCodeEst(grpDesc + " " + grpCode ));	
						
					}
					
					if(grpType.equals("pt__chr02")){
						types.put(grpCode, Converter.enCodeEst(grpDesc));				
					}
					
					if(sorting != null){
					   prodSorting.put(grpCode, sorting);	
					}
					
					sorting = null;
							}
								}}
					}
  
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	System.out.println("tm " + tm.size());
         return tm;
   }

   
   public TreeMap getProdGroupsSecond() throws SQLException {
		         return types;
		   }


   private TreeMap suppliersTM = null;
   TreeMap agents = null;
   TreeMap agents1 = null;

  
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // this.getProdGroups();
       agents = new TreeMap();
       agents1 = new TreeMap();
       HashMap asendajad = new HashMap();
       
       try {
			 FileReader fr = new FileReader("settings/vacation.txt");
	         BufferedReader reader = new BufferedReader(fr);
	       String line = null;
	 
	        while((line = reader.readLine()) != null ){
	           
	        	StringTokenizer st = new StringTokenizer(line, ";");
	
	          if (st.hasMoreTokens()) {
	            String ag1 =	st.nextToken();
	            String man = st.nextToken().trim();
	            Vector vv = new Vector();
	            if(asendajad.get(ag1) != null){
	           	 vv = (Vector)asendajad.get(ag1);
	           	 vv.add(man);
	           	 asendajad.put(ag1, vv);
	            }else{
	           	 vv.add(man);
	           	 asendajad.put(ag1, vv); 
	             }
	            }
	          }
		 
		}catch(Exception d){
		d.printStackTrace();	
		}
		System.out.println("asendajad "+ asendajad.size());
	
		 String aaa = getStringFromFile("SalesPersonMeriEsb.xml");
		
		 
       InputSource xml = new InputSource(new StringReader(aaa));	
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//salesPerson").evaluate(xml, XPathConstants.NODESET);
	

		for (int i = 0 ; i < nodeList.getLength() ; i++){
		
			Node o = nodeList.item(i);
			String kood = null;
			String nimi = null;
			String phone = null;

			String basket = null;
				NodeList ll = o.getChildNodes();
				for (int r = 0 ; r < ll.getLength() ; r++){
					
					
					Node nn = ll.item(r);
					//System.out.println(nn.getNodeName());
					
					  if(nn.getNodeName().equals("spAddr")){
						   try{  kood = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
						   }catch(Exception e){ }
					   }
					  
					  if(nn.getNodeName().equals("adBasket")){
						   try{  basket = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
					

					  if(nn.getNodeName().equals("adName")){
						   try{  nimi = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
						

					  if(nn.getNodeName().equals("adPhone")){
						   try{  phone = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
				}
				
			
			
		
                   Agent sm = new Agent (kood);
          
                   if(asendajad.get(kood) != null){
                     	Vector vv = (Vector)asendajad.get(kood);
                     	Enumeration aa = vv.elements();
                     	while(aa.hasMoreElements()){
                     		sm.addDubbler((String)aa.nextElement());
                     		System.out.println("Lisan asendaja " + kood + " " + sm.getDubbler().toString());
                     	}
                     }
                   
                   
                   sm.setName(kood + " " + nimi);
                  
                   sm.setLanguage("LIT");
                   sm.setCountry("lt");
                   sm.setPassWord("0000");
                  String email = parent.parseFirstEmail(parent.getEmail(kood));
                  if(email.length() > 3){
                	  sm.setEmail(email);
                  }
                  if(email.length() > 0){
                	  sm.setActive(true);
                  }
                  
                  if(phone != null){
                	  sm.setPhone(phone);
                  }
             
                	//agentidele on avaja settid abasketid .
                  if(basket != null){
                	  sm.addProdGroupList(basket);
                  }
              	//System.out.println("Agendi data " + kood + " " + nimi + " "+ phone + " "+ sm.getEmail());
             
                  
                     tm.put(kood + " " +nimi, sm); 
                     agents.put(kood, nimi);
                     agents1.put(kood, sm);
        } 
 	 
		 }catch(Exception e){
    		  e.printStackTrace();
    	  }
     System.out.println("Agendilist tehtud" );


     
     Agent sm = new Agent ("HOUSE");
		       		
		 sm.setName("OFFICE");
     tm.put("HOUSE", sm); 
   //  setBulkSales(agents1);
     return tm;
   }

   
   private StringBuffer removeTags(String input , String tag){
	   StringBuffer res  = new StringBuffer();
	   int len = tag.length();
	   while (input.indexOf("<" + tag + ">") != -1){
		   int a = input.indexOf("<" + tag + ">");
		   res.append(input.substring(0,a));
		   input = input.substring(a + len + 1);
		  // System.out.println("vahe " + input.substring(0,100));
		   if(input.indexOf("</" +tag + ">") != -1){
			   input = input.substring(input.indexOf("</" +tag + ">") + len + 3);
		   }
		   //System.out.println("vahe2 " + input.substring(0,100));
	   }
	   
	  res.append(input);
	   return res ;
   }
   
private String getMonth(){
	Calendar cal = Calendar.getInstance();
	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}
   




public static String getDataDirStr(){
	StringBuffer sb = new StringBuffer();

	Calendar	cal = Calendar.getInstance();
	
	
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1) {day = "0" + day;}

	String minute = String.valueOf(cal.get(Calendar.MINUTE));
	if (minute.length() == 1) {minute = "0" + minute;}

	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
	if (month.length() == 1){month = ("0") + month;}

	sb.append(("" + cal.get(Calendar.YEAR)).substring(2,4));
	sb.append("" + month);
	sb.append(day +"");
	sb.append(minute +"");
	return sb.toString();
}



  private String getcal(char day){
	  String res = null;
	  Calendar cal = Calendar.getInstance();
	  int weekday = cal.get(Calendar.DAY_OF_WEEK);
	 
	
	  if(day =='A'){
		  if (weekday != Calendar.MONDAY){
			  while (weekday != Calendar.MONDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='B'){
		  if (weekday != Calendar.TUESDAY){
			  while (weekday != Calendar.TUESDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		   }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='C'){
		  if (weekday != Calendar.WEDNESDAY){
			  while (weekday != Calendar.WEDNESDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='D'){
		  if (weekday != Calendar.THURSDAY){
			  while (weekday != Calendar.THURSDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='E'){
		  if (weekday != Calendar.FRIDAY){
			  while (weekday != Calendar.FRIDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='F'){
		  if (weekday != Calendar.SATURDAY){
			  while (weekday != Calendar.SATURDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='G'){
		  if (weekday != Calendar.SUNDAY){
			  while (weekday != Calendar.SUNDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  
	  res = cal.getTimeInMillis() + "";
	  cal.add(Calendar.DATE, 7);
	  res = res + "," + cal.getTimeInMillis() + "";  
	  
	  
	  return res ;
  }

  private String makeDeldays(String deldays){
	  String res = null;
	  char[] chars = deldays.toCharArray();
	  for (int i = 0; i < chars.length ; i++) {
	      char c = chars[i];
	     // System.out.println("char " + c);
		  if(res == null){
			  res = getcal(c);
		  }else{
			  res = res + "," + getcal(c);
		  }
	  }
		 
	  
	  
	  return res ;
  }
        	  
        	  
       public String getDistribution() {
         return null;
       }
       
       
     
       
       private HashMap getCalldata(){
    	   
    	   HashMap call = new HashMap();
    	   
    	   try{
    			 
    		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		    DocumentBuilder db = dbf.newDocumentBuilder();
    			FileInputStream in = getCleanInputStream(new File(  getParent().getProps().getProperty("data.dir")
    					+ "CallDatesMerisoft.xml"));
    			String handler = null;
    			Document doc = null;
    			try{
    				doc = db.parse(in,handler);
    			}catch(SAXException a){
    				System.out.println("Viga XML-is ");
    				
    			}
    		    Node root = getNode(doc, "dsOutput");
    		  
    			//System.out.println("AAAAXY " + root.getNodeName()+ " " + root.getChildNodes().getLength());
		
    		    
    		for (int i = 0 ; i < root.getChildNodes().getLength() ; i++){
    			Node o = root.getChildNodes().item(i);
    			String agent = null;
    			String klient = null;
    			String day = null;

    			if(o.getNodeName().equals("xxcmsp_det")){
    			
    				NodeList ll = o.getChildNodes();
    				//System.out.println("AAAAX " + o.getNodeName() + " " + ll.getLength());
    				for (int r = 0 ; r < ll.getLength() ; r++){
    					
    					Node nn = ll.item(r);
    				//	System.out.println("AAAA " + nn.getNodeName());
    					if(nn.getNodeName().equals("xxcmspSpad")){
    						   try{  agent= nn.getFirstChild().getNodeValue();
    						   }catch(Exception e){ }
    					   }
    		
    					  if(nn.getNodeName().equals("xxcmspCmad")){
    						   try{  klient = nn.getFirstChild().getNodeValue();
    						   }catch(Exception e){ }
    					       }
    			        	
    				
    				 if(nn.getNodeName().equals("xxcmspCallcode")){
    					   try{  day = nn.getFirstChild().getNodeValue();
    					   }catch(Exception e){ }
    				       }
    		        	}
    			
    				
    				if(agent != null && day != null && klient != null && agent.length() > 0 && day.length() > 0 && klient.length() > 0){
    					call.put(klient.trim()+ "-" + agent.trim(), Converter.getVisitDay(day));
    				}
    				
    			}	
    			         }
    		 }catch(Exception e){
    			 e.printStackTrace();
    		 }
    		// System.out.println("Call "+ call.toString());

    		 return call;
    	   
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
    	  
    	  private Node getNode(Node rt, String s){
    		    return GenericDOM.getNode(rt, s);
    		  }
       

   public TreeMap getCustomers(Agent agent){
	      getDebtX();
	  
	      priceass =  this.getPriceMatrixDataXX();
			System.out.println("fixedass suurus " + fixedass.size());
		
	     // HashMap visits =  getCalldata();
	      TreeMap tm = new TreeMap();
	    int loe = 0;
	     StringBuffer vips = new StringBuffer();
	    
	    	 
	    	 String tulem = getStringFromFile("CustomerMeriEsb.xml");
	    	 
	    	
	     InputSource xml = new InputSource(new StringReader(tulem));	
			XPath xPath =  XPathFactory.newInstance().newXPath();
			 try{
			NodeList nodeList = (NodeList) xPath.compile("//Customer").evaluate(xml, XPathConstants.NODESET);
System.out.println(" Node size " + nodeList.getLength());
			for (int i = 0 ; i < nodeList.getLength() ; i++){
			
				Node o = nodeList.item(i);
                  String fiAndPr = null;    
				NodeList ll = o.getChildNodes();
				String kood = null;
				String payer = null;
                String name =  null;
                String street =  null;
                String visit =  null;
                String deldays =  null;
                String inde =  null;
                String city =  null;
                String phone =  null;
                String person =  null; 
                String ag =    null; 
                String state = null;
                double balance = 0;
                double over = 0;
                double limit = 0;
                int due = 0 ;
    			double used = 0;
    			boolean vip = false ;
    			
    		for (int r = 0 ; r < ll.getLength() ; r++){

				Node nn = ll.item(r);

		  if(nn.getNodeName().equals("cmAddr")){
			   try{  kood = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
			
		  
		  
		  if(nn.getNodeName().equals("anx_det")){
			  NodeList llx = nn.getChildNodes();
			  
			  for (int rr = 0 ; rr < llx.getLength() ; rr++){
				  Node nnn = llx.item(rr);
				  if(nnn.getNodeName().equals("anxCode")){
					   try{ 
						   if(fiAndPr == null){
							   fiAndPr = Converter.cleanLitLatEst(nnn.getFirstChild().getNodeValue());
											   
						   }else{
							   fiAndPr = fiAndPr + "|" + Converter.cleanLitLatEst(nnn.getFirstChild().getNodeValue());
						   }
					   }catch(Exception e){ }
				  }
			 }
						   }
		  
		  
		  
		  
		  
		  if(nn.getNodeName().equals("cmBill")){
			   try{  payer = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmChr04")){
			   try{  deldays = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  
		  if(nn.getNodeName().equals("adName")){
			   try{  name = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  

		  if(nn.getNodeName().equals("adVip")){
			   try{  String na = nn.getFirstChild().getNodeValue();
			   if(na.equals("true")){
				   vip = true;
			   }
			   }catch(Exception e){ }
		   }
		  
		  
		 
		  
		  if(nn.getNodeName().equals("adLine1")){
			   try{  street = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("adZip")){
			   try{  inde = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("adCity")){
			   try{  city = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  

		  if(nn.getNodeName().equals("adState")){
			   try{  state = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
			
		  if(nn.getNodeName().equals("adPhone")){
			   try{  phone = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("adAttn")){
			   try{  person = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("cmSlspsn")){
			   try{ 
				   if(ag != null){
					   ag = ag + "," + nn.getFirstChild().getNodeValue();
					  }else{
					   ag = nn.getFirstChild().getNodeValue();
					}
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("cmBalance")){
			   try{ balance = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmUsedCredit")){
			   try{ used = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmDebtDue")){
			   try{ over = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmCrLimit")){
			   try{ limit = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  

		  if(nn.getNodeName().equals("ctDueDays")){
			   try{ due = Integer.parseInt( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
			}
			
			
				 if(kood != null  && name != null ){
					
					 if(vip){
						 vips.append(kood + "\n"); 
					 }
					 vip = false;
					 
	         	  try{	
				Customer customer = new Customer(kood);
				if(payer != null){
					customer.setPayer(Converter.cleanLitLatEst(payer));
				}
				customer.setPaymTerm(due +"");
				String per = "";
				if(person != null){
					per = person;
				}
				
				if(phone != null){
					per = per + " " + phone;
				}
				
				if(per.trim().length()>0){
					customer.setPersonalData(Converter.enCodeEst(per.trim()));
				}
				String aadr = "";
				if(street != null){
					aadr = street;
				}
				
				if(city != null){
					aadr = aadr + " " + city;
				}
				
				if(deldays  != null  && !deldays.equals("ABCDEFG")){
					//System.out.println("del days " + customer.getID() + " " + deldays);
		        	customer.setDelDays(makeDeldays(deldays));
		        	//System.out.println("del days " + customer.getID() + " " + customer.getDelDays());
		        }
				
				customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadr.trim(),"Lithuania")));
				customer.setName(Converter.enCodeEst(name + " " + kood + " " + aadr));
				if(ag != null){
					StringTokenizer st = new StringTokenizer(ag,",");
					while (st.hasMoreElements()){
						String add = Converter.cleanLitLatEst(st.nextToken());
						Agent aas = new Agent(add);
						//if(visits.get(customer.getID()+"-"+add) != null){
							//aas.setVisit((String)visits.get(customer.getID()+"-"+add));
						//}
						customer.addAgent(aas);
						  Agent agg = ((Agent)this.agents1.get(add));
	         				if(agg != null && agg.getDubbler().size()>0){
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           						
	           					}
	           					customer.addAgent(new Agent("HOUSE"));
	           				}
	           			}
			    	}
				
				
				if(fiAndPr != null){
					customer.setGroup(fiAndPr);
					StringTokenizer st = new StringTokenizer(fiAndPr,"|");
					while(st.hasMoreElements()){
						String aa = st.nextToken();
						customer.addFixSortCode(aa);
						
					}
					if(usedCustCodes.contains(customer.getID())){
						customer.addFixSortCode(customer.getID());
					}
					if(usedCustCodes.contains(customer.getPayer()) &&
							!customer.getFixSortCode().contains(customer.getPayer())){
						customer.addFixSortCode(customer.getPayer());
					}
					//System.out.println("custGroup " + fiAndPr);
				}
				
				if(state != null && state.equals("19")){
					customer.setIsPayer(true);
				}
				
				customer.setUsedCredit(balance);
			if(overx.get(customer.getID()) != null){
				customer.setDebt(overx.get(customer.getID()));
			}
			
			if(overx.get(customer.getPayer()) != null){
				customer.setDebt(overx.get(customer.getPayer()));
			}	
				customer.setCredit(limit + "");
	     
	         		 if(getParent().getProps().getProperty("all.customers.list") != null){
	         			 String ags = getParent().getProps().getProperty("all.customers.list");
	             			StringTokenizer st = new StringTokenizer(ags,",");
		         			while (st.hasMoreTokens()){
		         				Agent a = new Agent(st.nextToken());
		         					customer.addAgent(a);
		         			}
		         		 }
	         		  
		  
	         		 if(visit != null && visit.trim().length() > 0){
	         			customer.setVisit(Converter.getVisitDay(visit)) ;
	         		 }
	         		 
	         			   tm.put(customer.getName(), customer);
		          //         this.customers.add(customer.getID()); 
	                     
	         		 
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
			}
			 }
			}catch(Exception r){
        		  r.printStackTrace();
        	  }
	     
 System.out.println("cust suurus " + tm.size());
			return tm;
   }


private TreeMap<String,Double> stocks = null; 

private HashSet grpsUsed = null;
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
    stocks = new TreeMap<String,Double>();
    
    grpsUsed = new HashSet();
	System.out.println("Tooted algan");
	getProdGroups();
 
	
     try {   
 		XPath xPath =  XPathFactory.newInstance().newXPath();
 		InputSource xml = new InputSource(new StringReader(this.getStringFromFile("ItemMeriEsb.xml")));		
 		NodeList nodeList = (NodeList) xPath.compile("//Item").evaluate(xml, XPathConstants.NODESET);
 	  	 
    	 
 		System.out.println("Products process "  + nodeList.getLength() ) ;
 		
 		for (int i = 0 ; i < nodeList.getLength() ; i++){
			Node o = nodeList.item(i);
			
			String kood = null;
			String top = null;
			String nimi = null;
			String nimi2 = null;
			String grupp = null;
			String gruppSec = null;
			String basket = null;
			String type = null;
			
			String status = null;
			String unit = null;
			double price = 0;
			double stock = 0;

			double incase = 0;
			double minamount = 0;
			double optamount = 0;	
			
			NodeList ll = o.getChildNodes();
			for (int r = 0 ; r < ll.getLength() ; r++){
			
				Node nn = ll.item(r);
		  if(nn.getNodeName().equals("ptPart")){
			   try{  kood = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("ptChr08")){
			   try{  basket = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("ptChr02")){
			   try{  type = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptLog01")){
			   try{  top = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  
		  if(nn.getNodeName().equals("ptDesc1")){
			   try{  nimi = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptDesc2")){
			   try{  nimi2 = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("pgNbr")){
			   try{  grupp = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("pgNbr2")){
			   try{  gruppSec = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptStatus")){
			   try{  status = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptUm")){
			   try{  unit = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("ptPrice")){
			   try{  price = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
          if(nn.getNodeName().equals("inQtyOhCalc")){
			   try{  stock = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
          

          if(nn.getNodeName().equals("incase")){
			   try{  incase = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
          
          

          if(nn.getNodeName().equals("ptDec02")){
			   try{  minamount = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
          
         

          if(nn.getNodeName().equals("ptDec01")){
			   try{  optamount = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
			}
			
			
if(kood != null && nimi != null){

	System.out.println("toode " + kood + " " + nimi + " " + grupp + " " + gruppSec);
		      Product prod = new Product(kood);
		      String add = "";
		      prod.setVat("21");
		      
			if(nimi2 != null){
				nimi =nimi + " " + nimi2 + " " + kood;
			}
			
			if(gruppSec != null){
				prod.setGroup(gruppSec);
			}
			
			/*if(basket != null){
				prod.setGroup(basket);
			 if(baskets.get(basket) != null){
				String aaa =  baskets.get(basket).toString();
				if(aaa.length()>3){
				    add = aaa.substring(0,3) + " ";
				}else{
					add = aaa + " ";
				}
			 }
				//if(!grpsUsed.contains(grupp)){
					//grpsUsed.add(grupp);
				//}
			}*/
			
			prod.setName(Converter.enCodeEst( add + nimi));
			
			
			if(top != null && top.equals("true")){
				prod.setStatusTop("1");
			}
				
				
			if(type != null){
				prod.setGroupSecond(type);
				
			}
			
			if(unit != null){
				prod.setUnit(unit);
				if(unit.equals("VN")){
					prod.setOptAmountInUnits(1);
					prod.setNoCommas(true);
				}
			}
			
			if(price > 0){
				prod.setPrice(price);
			}
			

			if(minamount > 0){
				prod.setMinAmountInUnits(minamount);
			}
			
			
			if(optamount > 0){
				prod.setOptAmountInUnits(optamount);
			}
			
			
			if(incase > 0){
				prod.setInCases(incase);
			}
			
			

			if(stock > 0 && stock < 10000){
	        	prod.setStock(stock);
	        	stocks.put(kood, stock);
	    	}
			
			
			if(prices.get(prod.getID()) != null){
				try{
					prod.setPrice(Double.parseDouble(prices.get(prod.getID())+""));
				}catch(Exception e){
					
				}
				//panenme toote juhul kui hind on kuskilolemas.

				 tm.put(prod.getName() , prod); 
			  prodHash.put(prod.getID(), prod);
	        		  
			}
}       
 		}
         	  }catch(Exception e){
         		  e.printStackTrace();
         	  }
     
     System.out.println("Tooted läbi " + tm.size()); 
        
     return tm;
   }

   

   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   
   private String getKlient(String kli){
	   String klient = null ;
	   return klient ;
   }
   
   
    public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	   
	   try{
		     InputSource xml = new InputSource(new StringReader(getStringFromFile("CustProductMeriEsb.xml")));	
				XPath xPath =  XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile("//Line").evaluate(xml, XPathConstants.NODESET);
				System.out.println("CP nodelist suurus " + nodeList.getLength());
				
				for (int i = 0 ; i < nodeList.getLength() ; i++){
				
					Node o = nodeList.item(i);
					
					String saaja = null;
					String toode = null;
	                String kuupaev =  null;
	                String lastbill =  null;
	                double kogus = 0;
	                double lastprice = 0;
	                if(o.getNodeName().equals("Line")){
	                
					NodeList ll = o.getChildNodes();
				for (int r = 0 ; r < ll.getLength() ; r++){
					Node nn = ll.item(r);
					
					/*  
<ihCust>10000941</ihCust>
<ihInvDate>2015-09-28</ihInvDate>
<idhInvNbr>MPA94635</idhInvNbr>
<idhPart>611037</idhPart>
<idhQtyInv>2.0</idhQtyInv>
<idhPrice>1.24</idhPrice>
</Line>
*/
					
					if(nn.getNodeName().equals("idhInvNbr")){
						   try{  lastbill = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
			  if(nn.getNodeName().equals("ihCust")){
				   try{  saaja = Converter.cleanLitLatEst(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhPart")){
				   try{  toode = nn.getFirstChild().getNodeValue();
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("ihInvDate")){
				   try{  kuupaev = nn.getFirstChild().getNodeValue().substring(5);
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhQtyInv")){
				   try{  kogus = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhPrice")){
				   try{  lastprice = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
			  
			  
			  // System.out.println("Tere " + saaja + " " + toode + " " +  kogus);
			    if(saaja != null && toode != null){
			    	CustProd cp = new CustProd(saaja, toode, kogus, 0, 0,kuupaev,lastprice,0,0," ");
			    	cp.setAbiKood(lastbill);
					  custProds.add(cp);
				}
				}	
	                }
	
				 
				    
			    }
				
			
	   }catch(Exception e){
		   e.printStackTrace();
	   }
	   
	   System.out.println("Tere custprods " + custProds.size());
     return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "BiovelaUus";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }
   
   private HashMap ketid = null;
   private HashMap sortimendid = null;
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap baskets = new TreeMap();
   private TreeMap types = new TreeMap();
   private TreeMap prodSorting = new TreeMap();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private String session = "";
 
   private String username = null;
   private int onpage = 100;
   private String company = null;
   private String password = null;
   private String apiversion = null;
   private String returntype = null;
   private String userstuff = null;
     private URL myurl = null;

  /**
   * setBulkSales
   *
   * @param agents Map
   */
     
     private Hashtable docCust = null;
     private Hashtable brandplans = null;
     private Hashtable basketnames = null;
     private Hashtable branddones = null;
     
     
  public void setBulkSales(Map agents) {
	         
	  
	  // loeme sisse kuu plaanid igale managerile
	    brandplans = new Hashtable();
	    basketnames = new Hashtable();
        branddones = new Hashtable();
	    
	   try{
		

		   BufferedReader br = new BufferedReader(new FileReader( "plan/GroupNames.csv"));
		    String line = null;
		    line = br.readLine();
		    while((line = br.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(line, ";");
			       if (st.hasMoreTokens()){
			    	
			    	   String code = st.nextToken();
			    	   String name = st.nextToken();
			    	   basketnames.put(code, name); 
			    	   
			       }   
		       }
		  
		   
	   br = new BufferedReader(new FileReader( "plan/PlanData.csv"));
	    line = null;
	    line = br.readLine();
	    while((line = br.readLine()) != null){
	    	StringTokenizer st = new StringTokenizer(line, ";");
		       if (st.hasMoreTokens()){
		    	   String period = st.nextToken();
		    	   String basket = st.nextToken();
		    	   String agent = st.nextToken();
		    	 
		    	   String planeur = st.nextToken();
		    	   if(planeur.indexOf(".") >0){
		    		   planeur = planeur.substring(0,planeur.indexOf(".")) + planeur.substring(planeur.indexOf(".")+1);
		    	   }
		    	   
		    	   String plankg = st.nextToken();
		    	   if(plankg.indexOf(".") >0){
		    		   plankg = plankg.substring(0,plankg.indexOf(".")) + plankg.substring(plankg.indexOf(".")+1);
		    	   }
		    	   
		    	   Toode tt = new Toode(basket);
		        	  tt.setPlancur(Double.parseDouble(planeur));
		        	  tt.setPlanvol(Double.parseDouble(plankg));
		        	  Vector asjad = new Vector();
	                  if(brandplans.get(agent) != null){
	                	   asjad = (Vector)brandplans.get(agent);
	                  }
	                  asjad.add(tt);
	                  brandplans.put(agent, asjad);
	                  
		       }
		         
		       
		       
		       
		
	    }
	    
	       
		   br = new BufferedReader(new FileReader( "plan/SalesData.csv"));
		    line = null;
		    line = br.readLine();
		    while((line = br.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(line, ";");
			       if (st.hasMoreTokens()){
			    	   //01.07.2015	1001	1074	868	385	1.103	468

			    	   String period = st.nextToken();
			    	   String basket = st.nextToken();
			    	   String agent = st.nextToken();
			    	 
			    	   String planeur = st.nextToken();
			    	   if(planeur.indexOf(".") >0){
			    		   planeur = planeur.substring(0,planeur.indexOf(".")) + planeur.substring(planeur.indexOf(".")+1);
			    	   }
			    	   
			    	   String plankg = st.nextToken();
			    	   if(plankg.indexOf(".") >0){
			    		   plankg = plankg.substring(0,plankg.indexOf(".")) + plankg.substring(plankg.indexOf(".")+1);
			    	   }
			    	   
			    	   String doneeur = st.nextToken();
			    	   if(doneeur.indexOf(".") >0){
			    		   doneeur = doneeur.substring(0,doneeur.indexOf(".")) + doneeur.substring(planeur.indexOf(".")+1);
			    	   }
			    	   
			    	   String donekg = st.nextToken();
			    	   if(donekg.indexOf(".") >0){
			    		   donekg = donekg.substring(0,donekg.indexOf(".")) + donekg.substring(donekg.indexOf(".")+1);
			    	   }
			    	  
			    	   Toode tt = new Toode(basket);
			        	  tt.setPlancur(Double.parseDouble(planeur));
			        	  tt.setPlanvol(Double.parseDouble(plankg));
			        	  tt.setDonecur(Double.parseDouble(doneeur));
			        	  tt.setDonevol(Double.parseDouble(donekg));
			        	
			    	   
			    	   Vector asjad = new Vector();
		                  if(branddones.get(agent) != null){
		                	   asjad = (Vector)branddones.get(agent);
		                	   boolean added = false;
		                       for(int i = 0;i < asjad.size();i++){
		                    	   Toode ts = (Toode)asjad.elementAt(i);
		                    	 //  System.out.println("TS enne " + ts.toString());
		                    	   if(ts.getID().equals(tt.getID())){
		                    		added = true;  
		                    		ts.setPlancur(ts.getPlancur() + Double.parseDouble(planeur));
                                    ts.setPlanvol(ts.getPlanvol() + Double.parseDouble(plankg));
		                    		ts.setDonecur(ts.getDonecur() + Double.parseDouble(doneeur));
		                    		ts.setDonevol(ts.getDonevol() + Double.parseDouble(donekg));

			                    	//   System.out.println("TS hiljem " + ts.toString());
		                    	   }
		                       }
		                      
		                       if(!added){
		                    	   asjad.add(tt);
				               }
		                       branddones.put(agent, asjad);
				                       
		                  }else{
		                	  asjad.add(tt);
			                  branddones.put(agent, asjad);
			                  
		                  }
			    	   
			        	  
			       }
		    }
	
	    
	    
	    
	   
	   // System.out.println("branddones " + branddones.toString());

	 //   System.out.println("bransplans " + brandplans.toString() );
	    
	   }catch(Exception k){
		  // k.printStackTrace();
	   }
		       
		      InputSource xml = new InputSource(new StringReader(getStringFromFile("SalesPersonMeriEsb.xml")));	
				XPath xPath =  XPathFactory.newInstance().newXPath();
				 try{
				NodeList nodeList = (NodeList) xPath.compile("//salesPerson").evaluate(xml, XPathConstants.NODESET);
		
				for (int i = 0 ; i < nodeList.getLength() ; i++){
					Node o = nodeList.item(i);
					String kood = null;
					double sales = 0;
						NodeList ll = o.getChildNodes();
						for (int r = 0 ; r < ll.getLength() ; r++){
							
							Node nn = ll.item(r);
				
							if(nn.getNodeName().equals("spAddr")){
								   try{  kood = nn.getFirstChild().getNodeValue();
								   }catch(Exception e){ }
							   }
				
							  if(nn.getNodeName().equals("calcCurrSales")){
								   try{  sales = Double.parseDouble(nn.getFirstChild().getNodeValue());
								   }catch(Exception e){ }
							       }
					        	}
						//System.out.println("BulkSales " + kood + " " + sales );
						   if(kood != null && agents.get(kood) != null){
							   ((Agent)agents.get(kood)).setMonthSales((int)sales);
							   String asia = "Basket EUR ir KG |";
							    String asi = "";
							   if(branddones.get(kood) != null){
								   
				                	Vector vv =   (Vector)branddones.get(kood);
				                	 Enumeration enume = vv.elements();
				                	 while(enume.hasMoreElements()){
				                		 Toode tt = (Toode)enume.nextElement();
				                		 
				                		 double plankg = 0;
										   double planeur = 0;
										   
										   if(brandplans.get(kood) != null){
											   Vector vvf =   (Vector)brandplans.get(kood);
											   Enumeration enumec = vvf.elements();
							                	 while(enumec.hasMoreElements()){
							                		 Toode tx = (Toode)enumec.nextElement();
							                		 if(tx.getID().equals(tt.getID())){
							                			 plankg = tx.getPlanvol();
							                			 planeur = tx.getPlancur();
							                		 }
							                	 }
							               }
				                		 
				                		 
				                		 
				                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
				                			asi = asi + ( this.basketnames.get(tt.getID()) + "| " +
				                					("Day: " + (int)(tt.getDonecur()/tt.getPlancur()*100))
				                					+ "% Plan.: " +(int) tt.getPlancur() + " Parda.: " + (int)tt.getDonecur() + "| ");
				                			asi = asi + ("Month: " +((int)(tt.getDonecur()/planeur*100))
				                					+ "% Plan.: " +(int) planeur + "| ");
				                		       
				                			
				                			asi = asi + ("Kg Day: " +
				                					((int)(tt.getDonevol()/tt.getPlanvol()*100))
				                					+ "% Plan.: " +(int) tt.getPlanvol() + " Parda.: " + (int)tt.getDonevol() + "| ");
				                			asi = asi + ("KG Month :  " +
				                					((int)(tt.getDonevol()/plankg*100))
				                					+ "% Plan.: " +(int) plankg + "| | ");
				                		       
				                			
				                		 
				                		   }
				                	    }
				                    }
			                    if(asi.trim().length() > 0){
			                    	((Agent)agents.get(kood)).setStart( Converter.enCodeEst(asia + asi) );
					                //   System.out.println( kood + " " + asi);
						                	
			                    }
							   
						   }
						
						
					         }
		 	 
				 }catch(Exception e){
		    		  e.printStackTrace();
		    	 }
	
				 System.out.println("BulkSales tehtud" );
    
	}
  
}
