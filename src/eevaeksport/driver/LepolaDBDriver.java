package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class LepolaDBDriver extends AbstractDBDriver implements BulkUpdater {

	public LepolaDBDriver(EksportPanel parent) {
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
			if ((names[i]).indexOf("debt") != -1) {
				file = names[i];
			}
		}
		if (file != null) {

			try {
				File fil = new File(getDir() + file);

				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
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

				fr.close();
				fil.renameTo(new File(getMoveDir() + file));

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
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(5, 7));
		int DAY = Integer.parseInt(kuup.substring(8, 10));
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
				File fil = new File(getDir() + file);
				//  fil.renameTo(new File(getMoveDir()+file));

				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
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
				fr.close();
				fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tm;

	}

	public TreeMap getAgents() {
		
		TreeMap tm = new TreeMap();
		try {

			String file = "agent.txt";

			FileReader fr = new FileReader(getDir() + file);
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {

				// System.out.println(line);
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					Agent agent = new Agent(id);
					if (st.hasMoreTokens()) {
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						agent.setName(name);
						if (st.hasMoreTokens()) {
							 st.nextToken();
							// System.out.println(name + sal);

						}
					}
					tm.put(agent.getName(), agent);
				}
			}}
catch(Exception e ){
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
			if ((names[i]).indexOf("ASIAK") != -1) {
				file = names[i];
			}
		}
		if (file != null && (new File(getDir() + file)).length()>1000) {

			
			try {

				File fil = new File(getDir() + file);
				//  fil.renameTo(new File(getMoveDir()+file));
				
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
				
				//String allStuff = Converter.decode(fil, "cp852");
				//String[] lines = allStuff.split("\n");
				//System.out.println(allStuff);
				//for(int i = 0; i < lines.length ; i++) {
					//String line = lines[i];
				
					String id = line.substring(0,6);
						
						Customer customer = new Customer(id);

						String name = line.substring(6,56);
							if (name.equals(""))
								name = "<" + id + ">";
							
							//System.out.println("enne" +name);
							customer.setName(name.trim());
							
						//	System.out.println("peale" +customer.getName());
							String aadress = line.substring(56,81).trim();
							String indeks = line.substring(81,86).trim();
							String linn = line.substring(86,111).trim();
							String phone = line.substring(111,136).trim();						
						//	System.out.println(phone );
							
								customer.setAddress((clear(aadress) + " |" + clear(linn) + " |" + indeks).trim());
								if(phone.length()>0)
									customer.setPersonalData(phone);				

						Agent age  = new Agent("001");
						Agent ag1  = new Agent("007");
						Agent ag2  = new Agent("006");
						Agent ag3  = new Agent("002");
						Agent ag4  = new Agent("005");
						Agent ag5  = new Agent("008");
						Agent ag6  = new Agent("004");
						Agent ag7  = new Agent("003");
						Agent ag8  = new Agent("020");
						Agent ag9  = new Agent("021");
						Agent ag10  = new Agent("1");
						
						
								customer.addAgent(age);
								customer.addAgent(ag1);
								customer.addAgent(ag2);
								customer.addAgent(ag3);
								customer.addAgent(ag4);
								customer.addAgent(ag5);
								customer.addAgent(ag6);
								customer.addAgent(ag7);
								customer.addAgent(ag8);
								customer.addAgent(ag9);
								customer.addAgent(ag10);
								tm.put(customer.getName(), customer);
								this.customers.add(customer.getID());
						          custMap.put(customer.getID(), customer);
							}
		

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return tm;
	}

	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("TUOTE") != -1) {
				file = names[i];
			}
		}

		if (file != null) {
			try {
				File fil = new File(getDir() + file);
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
					//System.out.println(line);
						String id = line.substring(13,19).trim();
						String name = line.substring(19,44).trim();
						String unit  = line.substring(44,46).trim();
						//name = Converter.decode(name.getBytes(),"cp852" );// see on väljas
						//System.out.println(name);
						Product prod = new Product(id);
						prod.setName(clear(name));
						
						if (unit.length() > 0) {
							prod.setUnit(unit);
						}
														
								double price = Double.parseDouble(line.substring(49,58));
								double vat = Double.parseDouble(line.substring(66,73));

								//System.out.println(line.substring(66,73) + "|" +vat);
								prod.setPrice(price);
								prod.setVat(vat+ "");
                                prod.setStock(-1);
								tm.put(prod.getName(), prod);
														
				}

				reader.close();
				fr.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		System.exit(0);
		return tm;
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector vec = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1) {
				file = names[i];
			}
		}

		System.out.println(file);
		if (file != null) {

			try {
				FileReader fr = new FileReader(getDir() + file);
				File fil = new File(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {

					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String custID = st.nextToken().trim();
						if (this.customers.contains(custID)) {
							if (st.hasMoreTokens()) {
								 st.nextToken().trim();

								if (st.hasMoreTokens()) {
									String prodID = st.nextToken().trim();
									double sold = Double.parseDouble(st
											.nextToken().trim());
									double retu = Double.parseDouble(st
											.nextToken().trim());
									double last = 0;

									if (st.hasMoreTokens()) {
										last = Double.parseDouble(st
												.nextToken().trim());
									}

									CustProd cp = new CustProd(custID, prodID,
											last, sold, retu, " ", 0, 0, 0, " ");
									//   Product prod = (Product)getParent().getProducts().get(prodID);
									// if (prod != null){
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				fil.renameTo(new File(getMoveDir() + file));
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
		return "Via";
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

	 private String clear (String ss){
		    String ret =   ss.replace(',','.');
		    ret =  ret.replace(',','.');
		    ret =  ret.replace(',','.');
		    ret =  ret.replace(',','.');
		    ret =  ret.replace('"',' ').trim();
		    ret =  ret.replace('"',' ').trim();
		    ret =  ret.replace('"',' ').trim();

		    if(ret == null || ret.trim().length()==0){
		    return " ";
		    }else {
		    return ret;
		    }
		  }
	
	private String getMoveDir() {
		String dir = getParent().getProps().getProperty("moveto");
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
}