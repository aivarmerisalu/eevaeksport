package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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

public class MediatoTXTDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

	public MediatoTXTDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
	}

	public String getPilotDayData(Agent agent) {
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public void setBulkSales(Map agents) {

	}
	public String getSaveDebtInfo() {
	   	return null;
	   }
	   
	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public Vector getDebt(String agentCode) {
		Vector list = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("debt.txt") != -1) {
				file = names[i];
			}
		}
		if (file != null) {

			try {
				InputStream is = new FileInputStream(getDir() + file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";","; ");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					Debt debt = new Debt();
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						debt.setCustID(id);
						if (st.hasMoreTokens()) {
							String arve = st.nextToken().trim();
							debt.setDoknr(arve);
						}
						if (st.hasMoreTokens()) {
							String kuup = st.nextToken().trim();
							debt.setDuedate(getDate(kuup));
						}
						if (st.hasMoreTokens()) {
							String sum = st.nextToken().trim();
							double summa = Double.parseDouble(sum);
							debt.setSum(summa);
						}
						if (st.hasMoreTokens()) {
							String yle = st.nextToken().trim();
							debt.setDue(yle);
						}

						/*   if (st.hasMoreTokens()){
						 String prodGroup = st.nextToken().trim();
						 agent.setProdGroup(prodGroup);
						 }*/
							list.addElement(debt);
						
					}
				}

				reader.close();
				//fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public String getDistribution() {
		return null;
	}

	public String getSaveStockInfo() {
		return null;
	}

	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt("20" +kuup.substring(6, 8));
		int MONTH = Integer.parseInt(kuup.substring(3, 5))-1;
		int DAY = Integer.parseInt(kuup.substring(0, 2));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}

	public void connectionDown() {
	}

	public void connectionUp() {
	}

	public TreeMap getProdGroups() throws SQLException {
		TreeMap tm = new TreeMap();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1) {
				file = names[i];
			}
		}
		if (file != null) {

			try {

				InputStream is = new FileInputStream(getDir() + file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				String line = null;
				String id = null;
				String name = null;
				while ((line = reader.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);

					if (st.hasMoreTokens()) {
						id = st.nextToken().trim();
					}
					if (st.hasMoreTokens()) {
						name = st.nextToken().trim();
					}
					tm.put(id, name);
				}
				reader.close();
			//	fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tm;

	}

	  private static String getNumeric(String s){
	  	String res = "";
	    for (int i = 0 ; i < s.length(); i++){
	      if (isNumeric(s.charAt(i))){
	        res = res + s.charAt(i);
	      }
	    }
	    return res;
	  }
	  
	  private static boolean isNumeric(char ch)
	  {
	      return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '.' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
	  }
	
	public TreeMap getAgents() {
		TreeMap tm = new TreeMap();
		try {
			getFixedAssortmentData();
			
			String file = "agent.txt";
			String add = getParent().getProps().getProperty("add");

			if (add == null || add.trim().length() == 0) {
				add = "";
			}

			InputStream is = new FileInputStream(getDir() +add+ file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {

				// System.out.println(line);
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim().replace('*',' ');
					Agent agent = new Agent(id);
					if (st.hasMoreTokens()) {
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						agent.setName(name + " " + id);
						if (st.hasMoreTokens()) {
							String sal = st.nextToken();
							// System.out.println(name + sal);
							if(sal.indexOf(",")!= -1){
								sal = 	sal.substring(0,sal.indexOf(","));
							}
							
							if(sal.indexOf(".")!= -1){
								sal = 	sal.substring(0,sal.indexOf("."));
							}
                         agent.setMonthSales(Integer.parseInt(getNumeric(sal)));
						}
					}
					tm.put(agent.getName(), agent);
				}
			}

			String[] names = (new File(getDir())).list();
			file = null;
			for (int i = 0; i < names.length; i++) {
				if ((names[i]).indexOf("agendimyyk") != -1) {
					file = names[i];
				}
			}
			if (file != null) {
				 is = new FileInputStream(getDir() + file);
				 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				line = null;
				while ((line = reader.readLine()) != null) {

					//System.out.println(line);
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Agent age = (Agent) tm.get(id);

						if (st.hasMoreTokens()) {
						st.nextToken().trim();
							if (st.hasMoreTokens()) {
								String sal = st.nextToken();
								int sales = new Integer(sal.trim().substring(0,
										sal.indexOf("."))).intValue();
								age.setMonthSales(sales);
							}
						}
					}
				}
				reader.close();
				is.close();
	//			f.renameTo(new File(getMoveDir() + file));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tm;
	}

	public TreeMap getCustomers(Agent agent) {

		TreeMap tm = new TreeMap();
		HashMap	custMap = new HashMap();
		this.customers = new ArrayList();
		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klient") != -1) {
				file = names[i];
			}
		}
		if (file != null && (new File(getDir() + file)).length()>1000) {

			try {


				InputStream is = new FileInputStream(getDir() + file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				String line = null;

			
				
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";","; ");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Customer customer = new Customer(id);
						customer.setAddress(" ");

						if (st.hasMoreTokens()) {
							String payer = st.nextToken().trim();
							customer.setPayer(payer);
						}

						Agent age = null;
						if (st.hasMoreTokens()) {
							age = new Agent(st.nextToken().replaceAll("_A"," ").trim());
						}

						if (st.hasMoreTokens()) {

							String name = st.nextToken().trim();

							Customer cust = (Customer) tm.get(name);
							if (cust != null) {

								//System.out.println(age.getID() + " "
									//	+ cust.getName());
								cust.addAgent(age);
								tm.put(cust.getName(), cust);

							} else {

								//System.out.println(age.getID() + "  else " );
								if (name.equals(""))
									name = "<" + id + ">";
								customer.setName(name);
								if (st.hasMoreTokens()) {
									String aadress = st.nextToken().trim();
									if (aadress != null) {
										customer.setAddress(aadress);
									}
								}

								if (st.hasMoreTokens()) {
									customer.setDebt(Double.parseDouble(st
											.nextToken().trim()));
								}

								if (st.hasMoreTokens()) {
									customer.setDiscount(Double.parseDouble(st
											.nextToken().trim()));
								}

								customer.addAgent(age);
								customer.addAgent(new Agent("manager"));
								if (age.getID().equals("GO30")){
									customer.addAgent(new Agent("JJ30"));
								}
								
								if (age.getID().equals("JJ30")){
									customer.addAgent(new Agent("GO30"));
								}
								
								tm.put(customer.getName(), customer);
								this.customers.add(customer.getID());
						          custMap.put(customer.getID(), customer);
							}
						}

					}
				}
				
                   reader.close();
				String file1 = "lisaklient.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
			  is = new FileInputStream(getDir() + file);
				 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				line = null;
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Customer abi = new Customer("ABI" + id);
				if (st.hasMoreTokens()) {
		     		String agcode = st.nextToken();
		     		
					abi.addAgent(new Agent(agcode));
					if (agcode.equals("JJ30")){
						abi.addAgent(new Agent("GO30"));
					}
					abi.addAgent(new Agent("manager"));
					if (st.hasMoreTokens()) {	
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						abi.setName("S " +name.toUpperCase());	
					}
					if (st.hasMoreTokens()) {	
						String addr = st.nextToken().trim();
						if (addr.equals(""))
							addr = "<" + id + ">";
						abi.setAddress(addr);	
				          custMap.put(abi.getID(), abi);
					}
				}

			//	System.out.println(abi.getName() + abi.getAgents().toString() );
				tm.put(abi.getName(), abi);
				this.customers.add(abi.getID());
			     }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }


                reader.close();
                
				String file2 = "custvisit.txt";
				add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
			   is = new FileInputStream(getDir() + file);
				 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				line = null;
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Customer abi = (Customer)custMap.get(id);
				if (abi != null){
				
				if (st.hasMoreTokens()) {
		     		String visit = st.nextToken();
		     		if (visit != null && visit.trim().length()>0){
		     			abi.setVisit(visit);								
		     		}
				  }
				}
                }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }				
				

				String file3 = "custagent.txt";
				add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
			   is = new FileInputStream(getDir() + file);
				 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				line = null;
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Customer abi = (Customer)custMap.get(id);
				if (abi != null){
				
				if (st.hasMoreTokens()) {
		     		String visit = st.nextToken();
		     		if (visit != null && visit.trim().length()>0){
		     			abi.addAgent(new Agent(visit));								
		     		}
				  }
				}
                }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }
				
				
				reader.close();
				

				//fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("outbabe");
			System.exit(0);
		}

		
		return tm;
	}

	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted.txt") != -1) {
				file = names[i];
			}
		}

		if (file != null) {
			try {
				File fil = new File(getDir() + file);
				FileInputStream is = new FileInputStream(getDir() + file);
				BufferedReader	 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";","; ");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					//System.out.println(line);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Product prod = new Product(id);
						if (st.hasMoreTokens()) {

							 	prod.setName(st.nextToken().trim());
								
							if (st.hasMoreTokens()) {
								double price = Double.parseDouble(st
										.nextToken().trim());
								// if (price > 0){

								prod.setPrice(price);
								if (st.hasMoreTokens()) {
									double inCases = Double.parseDouble(st
											.nextToken().trim());
									if (inCases > 0) {
										prod.setInCases(inCases);
										if (st.hasMoreTokens()) {
											String ean = st.nextToken().trim();
											if (ean.length() > 0) {
												prod.setEAN(ean);

											}
											
											if (st.hasMoreTokens()) {
												prod.setStock(Double.parseDouble(st.nextToken().trim()));
											}
											
											if (st.hasMoreTokens()) {
												String gr = st.nextToken()
														.trim();
												if (gr.length() > 0) {
													prod.setGroup(gr);
												}
											}

											
											//System.out.println(line);
											//System.out.println(prod.getEAN());
											
											
												if (st.hasMoreTokens()) {
													String unit = st
															.nextToken();
													if (unit != null
															&& unit.trim()
																	.length() > 0) {
														prod.setUnit(unit
																.trim());
													}
												}

												if (st.hasMoreTokens()) {
													String gr = st.nextToken();
													//System.out.println(gr);
													if (gr != null
															&& gr.trim()
																	.length() > 0) {
														//	prod.setGroup(gr.trim());
													}
												}
												tm.put(prod.getName(), prod);
											
										}
									}
								}
							}
						}
					}
				}

				reader.close();
				
				//fil.renameTo(new File(getMoveDir() + file));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tm;
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector vec = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode.txt") != -1) {
				file = names[i];
			}
		}

		//System.out.println(file);
		if (file != null) {

			try {
				FileInputStream is = new FileInputStream(getDir() + file);
				BufferedReader	 reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
                   line = line.replaceAll(";","; ");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String custID = st.nextToken().trim();
						if (this.customers.contains(custID)) {
						

								if (st.hasMoreTokens()) {
									String prodID = st.nextToken().trim();
									double last = Double.parseDouble(st
											.nextToken().trim());
									double sold = Double.parseDouble(st
											.nextToken().trim());
									double retu = 0;

									

									CustProd cp = new CustProd(custID, prodID,
											last, sold, retu, " ", 0, 0, 0, " ");
									//   Product prod = (Product)getParent().getProducts().get(prodID);
									// if (prod != null){
									vec.addElement(cp);
								}						
						}
					}
				}

				reader.close();
			//	fil.renameTo(new File(getMoveDir() + file));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return vec;

	}

	public String getStartingTime() {
		return null;
	}

	public String getDriverVersion() {
		return "MediatoTXT";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public double getSalary(String agentCode) {
		return 0;
	}

	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}



	public static final String SEPARATOR = ";";



	public static final String PRODUCT_FILE = "tooted.txt";

	public static final String AGENT_FILE = "agent.txt";

	public static final String CUSTOMER_FILE = "klient.txt";

	public static final String CUSTOMER_AGENT_FILE = "klagent.txt";

	public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.txt";

	public static final String PRODGROUP_FILE = "tootegrupp.txt";

	public static final String DEBT_FILE = "debt.txt";

	public static final String CUSTOMER_DEBT_FILE = "debt.txt";

	private String subjectFilesTo = null;

	private ArrayList customers = null;

	@Override
	public Vector getPriceMatrixData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.getProducts(null);
		File f = new File(this.getExcelDir() + "/sortiments.xls");
		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		
		try {
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Products");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					groups.put(a + "", ((int)row.getCell((short) a)
							.getNumericCellValue()));
				}
			}
//System.out.println("groups " + groups.toString());

			for (int i = 5; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
               	for (int a = 4; a < row.getLastCellNum(); a++) {

						String group =  groups.get(a + "")+ "";
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.equals("1")) {

						// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0 ) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								 /*if(insertassort){
		                               insertSortLine(mline);   
		                           }*/
								result.add(mline);
								sb.append(mline.getStringForFixedAssortment()+ "\n");
							}
						}
					}
					   
                       }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		 System.out.println("fixed Suurus ");

		 Converter.saveFile("fixassort.txt", sb.toString());
		 
		return result;

	}


	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
}