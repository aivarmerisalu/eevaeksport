package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
import eevaeksport.manager.ManagerData;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ViaDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter {

	
	
	public ViaDBDriver(EksportPanel parent) {
		super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
	}
    public Vector getFixedAssortmentData() {
        return new Vector();
      }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getPilotDayData(Agent agent) {
		return null;
	}
	   public Vector getPriceMatrixData() {
	      return null;
	   }

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public void setBulkSales(Map agents) {

	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}
	 public String getProductSortingData(){
	   	return null;
	   }
	public String getAssortmentStr(Agent agent) {
		return null;
	}
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	public Vector getDebt(String agentCode) {
		Vector list = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("debt") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1 ) {
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
		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
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
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}
		
		String filemulti = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
		}
		
		
		String filemulti2 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
		}
		
		
		
	//	System.out.println(file + " " + filemulti);
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
				
				
				
				
				
				if(filemulti != null){
					File film = new File(getDir() + filemulti);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
						tm.put(id, name);
					}
					fr.close();
					 film.renameTo(new File(getMoveDir() + filemulti));
				}
				
				
				if(filemulti2 != null){
					File film = new File(getDir() + filemulti2);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti2);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
						tm.put(id, name);
					}
					fr.close();
					 film.renameTo(new File(getMoveDir() + filemulti2));
				}
				
				
				
				
				
				String file1 = "tootelisad.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
                
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				 st.nextToken();
		     	 st.nextToken();
		     	 st.nextToken();
				 String grupp = st.nextToken().trim();
				
				 if(grupp.length()>0){
					 tm.put(grupp,grupp);
				 }					
			   }
			}
			    fr.close();
				

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
			String add = getParent().getProps().getProperty("add");

			if (add == null || add.trim().length() == 0) {
				add = "";
			}

			FileReader fr = new FileReader(getStatDir() + add + file);
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {

			//	 System.out.println(line);
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
			}

			
			String file2 = "dubbler.txt";
			File ff = new File(getStatDir() + add + file2);
			if(ff.exists()){		
			 fr = new FileReader(getStatDir() + add + file2);
			 reader = new BufferedReader(fr);
		 line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					
					Iterator iter = tm.values().iterator();
					while(iter.hasNext()){
						Agent agent = (Agent)iter.next();
						if(agent.getID().equals(id)){
							while (st.hasMoreTokens()) {
								agent.addDubbler(st.nextToken().trim());
							}
						}	
					}
					
				}
			}
			}
			
			
			
			String[] names = (new File(getDir())).list();
			file = null;
			for (int i = 0; i < names.length; i++) {
				if ((names[i]).indexOf("agendimyyk") != -1 && getParent().getProps().getProperty("stockcode") != null &&
						(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
					file = names[i];
				}
			}
			if (file != null) {
				fr = new FileReader(getDir() + file);
				File f = new File(getDir() + file);
				reader = new BufferedReader(fr);
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
				fr.close();
				f.renameTo(new File(getMoveDir() + file));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tm;
	}

	public TreeMap getCustomers(Agent agent) {

		TreeMap tm = new TreeMap();
     	custMap = new HashMap();
		this.customers = new ArrayList();
		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("kliendid") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}
		System.out.println("file " +file);
		if (file != null && (new File(getDir() + file)).length()>10) {

			try {

				File fil = new File(getDir() + file);
				//  fil.renameTo(new File(getMoveDir()+file));

				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;

	     		if(file.indexOf("22")!=- 1){
	     			Customer custo = new Customer("Gutta1");

					custo.setName("NP Foods Eesti O�");
					custo.setPayer("Laima1");
					custo.setAddress("Kuskil Tallinnas");
					Enumeration enums = getParent().getAgentsHash().elements();
	             	  while (enums.hasMoreElements()) {
	             	    Agent a = (Agent) enums.nextElement();
	             	    custo.addAgent(a);
	             	   }
					//tm.put(custo.getName(),custo);
					//this.customers.add(custo.getID());
			        //  custMap.put(custo.getID(), custo);
		             	  
		     			 custo = new Customer("Laima2");
						custo.setName("NP Foods Eesti O� / Komp., N�idis");
						custo.setPayer("Laima2");
						custo.setAddress("Kuskil Tallinnas");
						 enums = getParent().getAgentsHash().elements();
		             	  while (enums.hasMoreElements()) {
		             	    Agent a = (Agent) enums.nextElement();
		             	    custo.addAgent(a);
		             	   }
						//tm.put(custo.getName(),custo);
					//	this.customers.add(custo.getID());
				      //    custMap.put(custo.getID(), custo);
				          
				          
				          
				     	 custo = new Customer("NPFoods1");
							custo.setName("NP Foods Eesti O� / N�idised");
							custo.setPayer("Laima2");
							custo.setAddress("Kuskil Tallinnas");
							 enums = getParent().getAgentsHash().elements();
			             	  while (enums.hasMoreElements()) {
			             	    Agent a = (Agent) enums.nextElement();
			             	    custo.addAgent(a);
			             	   }
							tm.put(custo.getName(),custo);
							this.customers.add(custo.getID());
					          custMap.put(custo.getID(), custo);
					          
				          
				          
				 		 custo = new Customer("Laima3");
							custo.setName("NP Foods Eesti O� / Kontori n�idised");
							custo.setPayer("Laima3");
							custo.setAddress("Kuskil Tallinnas");
							
			             	    Agent a = new Agent("HOUSE");
			             	    custo.addAgent(a);
			             	   
						//	tm.put(custo.getName(),custo);
							//this.customers.add(custo.getID());
					         // custMap.put(custo.getID(), custo);
		     		


	     		
	     		}

	     		

				
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;", "; ;");
					System.out.println(line);
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
							age = new Agent(st.nextToken().trim());
					
							//age = (Agent)getParent().getAgentsHash().get(age.getID());
						
						}
					
						if (st.hasMoreTokens()) {
							String name = st.nextToken().trim();

							Customer cust = (Customer) tm.get(name);
							if (cust != null) {
									if(age!= null){
									cust.addAgent(age);										
								}
								tm.put(cust.getName(), cust);
							} else {
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
								try{
									customer.setDebt(Double.parseDouble(st.nextToken().trim()));
								}catch(Exception e ){
									
								}
							
								}

								if (st.hasMoreTokens()) {
									try{
									customer.setDiscount(Double.parseDouble(st
											.nextToken().trim()));
}catch(Exception e ){
									
								}
								}
								
								if (st.hasMoreTokens()) {
									String blokk = st.nextToken().trim();
									if(blokk != null && blokk.equals("1")){
										customer.setSalesBlock(true);
									}
									
									if(blokk != null && blokk.equals("2")){
										customer.setSalesBlock(true);
									}
									
									if(blokk != null && blokk.equals("3")){
										customer.setSalesBlock(true);
									}
									
								}
								
								if (st.hasMoreTokens()) {
									
									String makse = st.nextToken();
									if(makse != null && makse.trim().length() > 0)
									makse = makse.substring(1);
								if(makse.equals("ette")){
									customer.setPaymTerm("-1");
								}else{
									//customer.setPaymTerm(makse); muutsin �ra et sokki poleks 
								}
								}
								
									if(age != null){
										customer.addAgent(age);	
										customer.addAgent(new Agent("HOUSE"));
										customer.addAgent(new Agent("manager"));
										customer.addAgent(new Agent("INF12"));
									}
						
							
							
								
								if (age != null){
									Vector vec = age.getDubbler();
									if(vec!= null && vec.size() >0){
										Enumeration en = vec.elements();
										while(en.hasMoreElements()){
											String ag = (String) en.nextElement();
											customer.addAgent(new Agent(ag));
										}
									}
								}
								
								
								tm.put(customer.getName(), customer);
								this.customers.add(customer.getID());
						          custMap.put(customer.getID(), customer);
						       
							}
						}

					}
				}
				
                   fr.close();
                   reader.close();
				String file1 = "lisaklient.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
				Customer abi = null;
                
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				if (st.hasMoreTokens()) {
		     		String agcode = st.nextToken();
		     		
		     		if(agcode.indexOf("29")!=- 1 || agcode.indexOf("31") != -1){
                     abi = new Customer(id);	
                    }else{
                   	 abi = new Customer("ABI" + id);		
                    }
		     		
		     		
		     		
					abi.addAgent(new Agent(agcode));
					
								
					Agent age = (Agent)getParent().getAgentsHash().get(agcode);
					
					
					if (age != null){
						Vector vec = age.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String ag = (String) en.nextElement();
								abi.addAgent(new Agent(ag));
							}
						}
					}
					
					abi.addAgent(new Agent("KK22"));
					abi.addAgent(new Agent("manager"));
					abi.addAgent(new Agent("HOUSE"));
					if (st.hasMoreTokens()) {	
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						

			     		if(agcode.indexOf("29")!=- 1 || agcode.indexOf("31") != -1){
							abi.setName(name.toUpperCase());	
	                    }else{
							abi.setName("S " +name.toUpperCase());		
	                    }
						
						
					}
					if (st.hasMoreTokens()) {	
						String addr = st.nextToken().trim();
						if (addr.equals(""))
							addr = "<" + id + ">";
						abi.setAddress(addr);	
				          custMap.put(abi.getID(), abi);
					}
					if (st.hasMoreTokens()) {	
						String paev = st.nextToken().trim();
						if (!paev.equals(""))
						abi.setVisit(paev);	
				          custMap.put(abi.getID(), abi);
					}
				}

			//	System.out.println(abi.getName() + abi.getID());
				tm.put(abi.getName(), abi);
				 custMap.put(abi.getID(), abi);
				this.customers.add(abi.getID());
			     }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }


                fr.close();
                reader.close();
                
				String file2 = "custvisit.txt";
				add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
				fr = new FileReader(getStatDir() + add + file2);
				reader = new BufferedReader(fr);
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

		   try{
				fr = new FileReader(getStatDir()  + file3);
				reader = new BufferedReader(fr);
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
		     			//System.out.println("abiagent" + visit + " " +id);
		     		}
				  }
				}
                }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }				
				
				reader.close();
				fr.close();

				fil.renameTo(new File(getMoveDir() + file));

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
		TreeMap koodiga = new TreeMap();
		String[] names = (new File(getDir())).list();
		String file = null;
		String filestock  = null;
		String filemulti = null;
		String filestockmulti  = null;

		String filemulti2 = null;
		String filestockmulti2  = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				filestock = names[i];
			}
		}
		
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filestockmulti = names[i];
			}
		}
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filestockmulti2 = names[i];
			}
		}
		
		
		
		 TreeMap partii = new TreeMap(); 
			if (filestockmulti2 != null) {
				try {
					File fil = new File(getDir() + filestockmulti2);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					fil.renameTo(new File(getMoveDir() + filestockmulti2));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
			
			if (filestockmulti != null) {
				try {
					File fil = new File(getDir() + filestockmulti);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					fil.renameTo(new File(getMoveDir() + filestockmulti));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
			
			
		if (filestock != null) {
			try {
				File fil = new File(getDir() + filestock);
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				reader.readLine();
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll("\",\"",",");
					line = line.replaceAll("\"","");
					line = line.replaceAll(",,",", ,");
					line = line.replaceAll(",,",", ,");
					StringTokenizer st = new StringTokenizer(line, ",");
					if( line.startsWith("LIN")){
					
						st.nextToken();
						String prodid = st.nextToken().trim();
						double nn = Double.parseDouble(st.nextToken().trim());
						if(st.hasMoreTokens() && nn > 0){
							st.nextToken();
							st.nextToken();
							st.nextToken();
							st.nextToken();
							st.nextToken();
							try{
								String info = 	st.nextToken().trim();
							//	System.out.println(line + " nnn "+ info);
								if(partii.get(prodid) != null){
									String inn = (String) partii.get(prodid);
									if(inn.length()> 15){
										inn = inn + "|";
									}
									partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
								}else{
									partii.put(prodid, info.replaceAll(";", ":"));
							     }	
									
							}catch (Exception ss){
								
							}
						
						}				
					}
				}
				fr.close();
				fil.renameTo(new File(getMoveDir() + filestock));
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
		
		
		
		String gronly = null;
		if(getParent().getProps().getProperty("grouponly") != null){
			gronly= getParent().getProps().getProperty("grouponly");
		}
		

		if (file != null) {
			try {
				File fil = new File(getDir() + file);
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;","; ;");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					
					boolean sv = false;
					//System.out.println(line);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Product prod = new Product(id);
						String name = "";
						if (st.hasMoreTokens()) {
                             name = st.nextToken().trim();
                           
                             if(name.startsWith("* ")){
                            	 sv = true;
                             }
                             
							 if ("22".equals(getParent().getProps().getProperty("stockcode"))) {
							 	prod.setName( "    "+id.substring(0,2) + " " + name);	
							 }else{
							 	prod.setName("    "+name);
								 	
							 }
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
												String gr = st.nextToken()
														.trim();
												if (gr.length() > 0) {
													prod.setGroup(gr);
												}
											}
   if(partii.get(id) != null){
	   prod.setPartii((String) partii.get(id));
	//   System.out.println("Sain partii "  + prod.getPartii());
   }
										
											//System.out.println(prod.getEAN());
											if (st.hasMoreTokens()) {
												prod.setStock(Double
														.parseDouble(st
																.nextToken()
																.trim()));
												//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
												// if (prod.getAgentProduct().equals(agent.getProdGroup())){
												//tm.put(prod.getName(), prod);
												//b}
												st.nextToken();
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
                                               String hhh = null;
												if (st.hasMoreTokens()) {
													String gr = st.nextToken();
													//System.out.println(gr);
													if (gr != null
															&& gr.trim()
																	.length() > 0) {
														hhh =(gr.trim());
													}
												}
											//	System.out.println(prod.getName().trim().indexOf("Y"));
												//System.out.println(line);
												if(getParent().getProps().getProperty("stockcode").equals("22") ){
													if(prod.getName().trim().indexOf("Y")!=3){
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												   }else{
													   boolean goes = true ;
													   if(sv && prod.getStock()<1 && getParent().getProps().getProperty("stockcode").equals("29")){ // henkeli erijuhtum et SV algusega 
														   goes = false;
													   }
													   if(gronly != null && !gronly.equals(hhh)){
														   goes = false;
													   }
													   if(goes){
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	   
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
				fr.close();
				fil.renameTo(new File(getMoveDir() + file));
				
				//siin pannakse kylge send separate juhul kui on multi firma case
				
				if(filemulti != null){
				//	System.out.println("filemulti" + filemulti);
					Iterator ite = tm.keySet().iterator();
					while(ite.hasNext()){
						Product pp = (Product)tm.get(ite.next());
						pp.setSendSeparate(getParent().getProps().getProperty("stockcode"));
					}
				}
				
				
				//siin on blokk teie toote failijaoks
			
				if(filemulti2 != null){
					//System.out.println("filemulti2" + filemulti2);
				
					
				 fil = new File(getDir() + filemulti2);
					 fr = new FileReader(fil);
					 reader = new BufferedReader(fr);
					 line = null;
					//	System.out.println("filemulti2" + fil.getAbsolutePath());
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll(";;","; ;");
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						//System.out.println(line);
						if (st.hasMoreTokens()) {
							String id = st.nextToken().trim();
							Product prod = new Product(id);
							prod.setSendSeparate(getParent().getProps().getProperty("stockcodethird"));
							if (st.hasMoreTokens()) {

									prod.setName("   "+getParent().getProps().getProperty("stockcodethird") + " " + st.nextToken().trim());			 	
								 
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
													String gr = st.nextToken()
															.trim();
													if (gr.length() > 0) {
														prod.setGroup(gr);
													}
												}
	   if(partii.get(id) != null){
		   prod.setPartii((String) partii.get(id));
		 // System.out.println("Sain partii "  + prod.getPartii());
	   }
												//System.out.println(line);
												//System.out.println(prod.getEAN());
												if (st.hasMoreTokens()) {
													prod.setStock(Double
															.parseDouble(st
																	.nextToken()
																	.trim()));
													//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
													// if (prod.getAgentProduct().equals(agent.getProdGroup())){
													//tm.put(prod.getName(), prod);
													//b}
													st.nextToken();
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
												//	System.out.println(prod.getName().trim().indexOf("Y"));
													if(getParent().getProps().getProperty("stockcode").equals("22") ){
														if(prod.getName().trim().indexOf("Y")!=3){
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	
														}
													   }else{
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												}
											}
										}
									}
								}
							}
						}
					}
					fr.close();
					fil.renameTo(new File(getMoveDir() + filemulti2));
				}
				
				
				
			if(filemulti != null){
			
				
			 fil = new File(getDir() + filemulti);
				 fr = new FileReader(fil);
				 reader = new BufferedReader(fr);
				 line = null;
					//System.out.println("filemulti" + fil.getAbsolutePath());
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;","; ;");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					//System.out.println(line);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Product prod = new Product(id);
						prod.setSendSeparate(getParent().getProps().getProperty("stockcodesec"));
						
						if (st.hasMoreTokens()) {
								prod.setName("   "+getParent().getProps().getProperty("stockcodesec") + " " + st.nextToken().trim());			 	
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
												String gr = st.nextToken()
														.trim();
												if (gr.length() > 0) {
													prod.setGroup(gr);
												}
											}
   if(partii.get(id) != null){
	   prod.setPartii((String) partii.get(id));
	  // System.out.println("Sain partii "  + prod.getPartii());
   }
											//System.out.println(line);
											//System.out.println(prod.getEAN());
											if (st.hasMoreTokens()) {
												prod.setStock(Double
														.parseDouble(st
																.nextToken()
																.trim()));
												//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
												// if (prod.getAgentProduct().equals(agent.getProdGroup())){
												//tm.put(prod.getName(), prod);
												//b}
												st.nextToken();
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
											//	System.out.println(prod.getName().trim().indexOf("Y"));
												if(getParent().getProps().getProperty("stockcode").equals("22") ){
													if(prod.getName().trim().indexOf("Y")!=3){
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												   }else{
													tm.put(prod.getName(), prod);
													koodiga.put(prod.getID(), prod);	
												}
											}
										}
									}
								}
							}
						}
					}
				}
				fr.close();
				fil.renameTo(new File(getMoveDir() + filemulti));
			}
			
			
			//siin l�peb teise firma toodete lisamine
				
				
				
				String file1 = "tootelisad.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
				
		   try{
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
                
			while ((line = reader.readLine()) != null) {
			
			
//Kaubakood	Nimetus	PDA -s	tk alusel	tk kastis	tk karbis	tk 1/4 m��gialusel	tk 1/2 m��gialusel	V��tkood

		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Product prodd =(Product) koodiga.get(id); 
				if( prodd!= null){
				
		     	 String name = st.nextToken();
		     	 if(prodd.getName().toLowerCase().indexOf("everest") != -1){
		     		 prodd.setName("    72 " + name.trim());
		     	 }
		     	 st.nextToken();
				
				 String grupp = st.nextToken().trim();
				 if(grupp.length()>0){
					 prodd.setGroup(grupp);
				 }
	             String alusel = st.nextToken();
	             if(alusel.trim().length()>0){
	            	 try{
		            	 prodd.setOnPallet(Double.parseDouble(alusel.trim())); 
	            	 }catch(Exception ee){
	            	 }	
	             }
	             String kastis = st.nextToken();
	             if(kastis.trim().length()>0){
	            	 try{
		            	 prodd.setInCases(Double.parseDouble(kastis.trim())); 
	            	 }catch(Exception ee){
	            	 }	
	             	
	             }
			    	

				    String minam = st.nextToken();
		             if(minam.trim().length()>0){
		            	 try{
			            	 prodd.setOptAmountInUnits(Double.parseDouble(minam.trim())); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		             
		             String info = st.nextToken();
		             if(info.trim().length()>0){
		            	 try{
			            	 prodd.setPartii( "1/4 alusel " + info.trim() + " | "); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		             
		             info = st.nextToken();
		             if(info.trim().length()>0){
		            	 try{
			            	 prodd.setPartii( prodd.getPartii() + "1/2 alusel " + info.trim() ); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		     	
				} 	
				}
			    
			}

	
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			reader.close();
			fr.close();
			fil.renameTo(new File(getMoveDir() + file));
	
			
		}catch (Exception e) {
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
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}

		
		String filemulti = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
		}
 
		
		
		String filemulti2 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
		}
 
		
		//System.out.println(file);
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
											 sold, retu,last, " ", 0, 0, 0, " ");
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
		
		if (filemulti != null) {

			try {
				FileReader fr = new FileReader(getDir() + filemulti);
				File fil = new File(getDir() + filemulti);
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
											 sold, retu,last, " ", 0, 0, 0, " ");
									//   Product prod = (Product)getParent().getProducts().get(prodID);
									// if (prod != null){
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				fil.renameTo(new File(getMoveDir() + filemulti));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
		
		// teine lisafirma 
		
		if (filemulti2 != null) {

			try {
				FileReader fr = new FileReader(getDir() + filemulti2);
				File fil = new File(getDir() + filemulti2);
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
											 sold, retu,last, " ", 0, 0, 0, " ");
									//   Product prod = (Product)getParent().getProducts().get(prodID);
									// if (prod != null){
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				fil.renameTo(new File(getMoveDir() + filemulti2));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		
		
		
		
		
		
		
		return vec;

	}

	

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);
      TreeMap ags = new TreeMap();
      Enumeration enums = getParent().getAgentsHash().elements();
	  while (enums.hasMoreElements()) {
	    Agent a = (Agent) enums.nextElement();
	    ags.put(a.getID(),a.getName());
	   }

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
       
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(tekst + sep);

        //Siia peame sama agendi nime 
        sb.append("0;0;" + "nimetu" +  "; ; ; ;0");
        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      Iterator iter = customers.iterator();
      
      StringBuffer bf2 = new StringBuffer();
      while(iter.hasNext()){
      	String resi =(String) iter.next();
      //	System.out.println(resi);
      	Customer cust = (Customer)custMap.get(resi);
      	if(cust != null){
      		
      	TreeMap agents = cust.getAgents();
    Iterator iter2 = agents.values().iterator();
        while(iter2.hasNext()){
        	Agent aa = (Agent)iter2.next();
        	if(!aa.getID().equals("manager")){
        	
        	bf2.append(cust.getID() + sep);
            bf2.append(aa.getID() + sep);
            String agentName = aa.getName();
            if (agentName == null) agentName = " ";
            bf2.append(agentName + sep);
            String email = parent.parseFirstEmail(parent.getEmail(aa.getID()));
            if(email == null || email.trim().length()== 0 )email = " ";
            bf2.append(email + "\n");
        	
        	}
          }
         }	
    	}
        

      md.setClientAgentInfo(bf2.toString());

      return md;
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

	private String getStatDir() {
		String dir = getParent().getProps().getProperty("statdir");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
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
	public  HashMap	custMap = new HashMap();
	private String subjectFilesTo = null;
    private String url2 = "jdbc:odbc:report";
	private ArrayList customers = null;
}