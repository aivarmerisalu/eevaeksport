package eevaeksport.driver;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;


import static java.util.Arrays.asList;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ODOOApiDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,FixLoader {

  public ODOOApiDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    
    username = getParent().getProps().getProperty("odoo.user");	
    company = getParent().getProps().getProperty("odoo.dbname");
    password = getParent().getProps().getProperty("odoo.pass");
    server = getParent().getProps().getProperty("odoo.server");
		try {
			uid = login(server,company,username,password);
		
		if (uid >0) {
 			System.out.println("Login Ok " + uid);
		  } else {
  			System.out.println("Login Fail ");
		 }
		} catch (Exception e) {
			e.printStackTrace();
		}

	    user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.companyx");
      
       // getPriceMatrixData();
      //  getCustomers(null);
         }
      String user =  null;
      String pass = null;
      String rcompany = null;
// login 
 
 private int uid = -1;

 
 private TreeMap eanprods = null;
 
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

private Object[] params = new Object[] {};

   public Vector getPriceMatrixData(){
	   Vector lines = new Vector();
	
		TreeMap<Integer,Integer> verIdsM = new TreeMap<Integer,Integer>();
		Vector<Integer> verIds = new Vector<Integer>();
	   TreeMap sorted = new TreeMap();
	   
		   try {    
		   XmlRpcClient models = new XmlRpcClient() {{
			    setConfig(new XmlRpcClientConfigImpl() {{
			        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
			    }});
			}};
			
			
			
			// kogume kokku prl_id-d 
			List<Object> res = null;
			
		//	this.getFielsnames("product.pricelist", models);
			res = java.util.Arrays.asList((Object[])models.execute("execute_kw", java.util.Arrays.asList(
					    company, uid, password,
					    "product.pricelist", "search_read",
					    asList(asList(asList("active", "=", true),asList("id", "in", this.prlInUse))), //asList("id", "in", this.prlInUse) (java.util.Arrays.asList("employee", "=", false))
					    new HashMap() {{
					        put("fields", java.util.Arrays.asList( "id","type","name", "version_id")); // 
					        put("limit", 1500);
					    }}
					)));
			
		//	System.out.println("res1 " + res.size() );
			
			 for (Object json : res){
				 HashMap row = (HashMap)json;
				 
				 Object [] countryIdName = (Object []) row.get("version_id");
			     if (countryIdName != null){
			    	try {
			    		
			    		for(int i = 0; i<countryIdName.length; i++ ) {
			    		//	 System.out.println("res20 " + i + " " + countryIdName[i] + " " + row.get("id") + " "+ row.get("name"));
			    			 verIdsM.put((Integer)countryIdName[i],(Integer)row.get("id"));
				    		 verIds.add((Integer)countryIdName[i]);		
			    		}
			    		 
			    	} catch(Exception e) {
			      }
			    }
			  }
		    	
		/*	 res = java.util.Arrays.asList((Object[])models.execute("execute_kw", java.util.Arrays.asList(
					    company, uid, password,
					    "product.pricelist.version", "search_read",
					    asList(asList(asList("active", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
					    new HashMap() {{
					        put("fields", java.util.Arrays.asList("name", "pricelist_id", "date_start", "date_end", "active", "items_id")); // 
					        put("limit", 500);
					    }}
					)));
		
			 
			 
				System.out.println("res2 " + res.size() + " "  + res.toString());
				
			*/	
				
				/* for (Object json : res){
                        HashMap row = (HashMap)json;
			    	
                       String name =  row.get("name")+"";
                       String prid =  row.get("pricelist_id")+"";
                       String verid =  row.get("id")+"";
                       String lopp =  row.get("date_end")+"";
                       String algus =  row.get("date_start")+"";
                       
				        String asi = "items_id";
				     
				     Object [] countryIdName = (Object []) row.get(asi);
				     if (countryIdName != null){
				    	
				    //	 System.out.println("res20 " + countryIdName[0] + " "+ name + " "+ prid+ " "+verid+ " "+lopp+ " "+algus);
				       }
		                
				 }*/
			    	
				
				// this.getFielsnames("product.pricelist.item", models);
				
				
				
				
				
				 res = java.util.Arrays.asList((Object[])models.execute("execute_kw", java.util.Arrays.asList(
						    company, uid, password,
						    "product.pricelist.item", "search_read",
						    asList(asList(asList("price_version_id", "in", verIds ))), //this.prlInUse
						    new HashMap() {{
						        put("fields", java.util.Arrays.asList("product_id", "product_tmpl_id",
                                        "categ_id", "min_quantity", "sequence",
                                        "date_start", "date_end", "base",
                                        "price_discount", "price_surcharge",
                                        "price_round", "price_min_margin",
                                        "price_max_margin","price_version_id")); // 
						        put("limit", 60000);
						    }}
						)));
				 
				
					//System.out.println("res3 " + res.size() + " " + res.toString());
			
					
		     
		     for (Object json : res){
		    	 HashMap row = (HashMap)json;
		    	 
		    	    String custID = "";
	                String clGroup =  "";
	                String prodID = null;
	                String prGroup = null;
	                
	            
	                Integer base  = (Integer) row.get("base");
	                
	                double price = (Double) row.get("price_surcharge");
	                double discount =( (Double) row.get("price_discount") ) ;
	                if(discount <0 && discount > -1) {
	                	discount = discount *-100;
	                  } else {
	                	discount = 0;
	                  }
	               // System.out.println("ale " + price + " " + discount);
	                int paevi = 0;
	                String algus = null;
	                try {
	                	algus = (String) row.get("date_start") ;
	                }catch(Exception e) {
	                }
	                
	                String lopp = null;
	                try {
	                	lopp = (String) row.get("date_end") ;
	                }catch(Exception e) {
	                }
	                
		    	 
	                String asi = "categ_id";
	     		   
			    try {
			    	  Object [] countryIdName = (Object []) row.get(asi);
					     if (countryIdName != null){
					    	 prGroup = countryIdName[0] + "";
				 	       }
			    }catch(Exception e) {
			    	
			    }
		    	 
		      asi = "product_id";
		   
		    	 String prodName = " noname";
		    try {
		    	  Object [] countryIdName = (Object []) row.get(asi);
				     if (countryIdName != null){
				    	 prodID = countryIdName[0] + "";
				    	 prodName = countryIdName[1] + "";
			 	       }
		    }catch(Exception e) {
		    	
		    }
		   
		    if(prodID != null || prGroup != null ) { 
		     
		     
              asi = "price_version_id";
		     
              Object [] countryIdName = (Object []) row.get(asi);
		     if (countryIdName != null){
		    	 clGroup = countryIdName[0] + "";
		    	// System.out.println("nimi " + countryIdName[1] + "");
		    	 clGroup = ""+verIdsM.get((Integer)countryIdName[0]);
	 	       }
		     
		     
		    
		    
		     
		     
             MatrixLine mline = new MatrixLine();
    
          boolean goes = true;
          
          
            if (lopp != null) {
					mline.setEndDate(lopp.substring(0, 10));
					if(getDate(lopp).before(new Date())) {
						goes = false;
					}
					//ei pane kuupäeva kui lõppu pole 
					if (algus != null) {
						mline.setStartDate(algus.substring(0, 10));
						if(getDate(algus).after(new Date())) {
							goes = false;
						}
					}
				}
            
				
				
			  

             if (custID != null && custID.trim().length() > 0) {
               mline.setCustID(custID.trim());
             }
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(clGroup.trim());
             }

             if (prodID != null && prodID.trim().length() > 0) {
               mline.setProdID(prodID.trim());
             }
             
             if (prGroup != null && prGroup.trim().length() > 0) {
                 mline.setProdGroup(prGroup.trim());
               }
             if (discount != 0) {
                 mline.setDiscount(discount);
               }
            
             if (price != 0) {
               mline.setPrice(price);
             }
             
            
             if((price > 0 || discount > 0) && goes ) {
            	 if(lopp != null) {
            		 sorted.put(clGroup+prodName+prGroup, mline); 
            	 }else {
            		 if(!sorted.containsKey(clGroup+prodName+prGroup)) {
            			 sorted.put(clGroup+prodName+prGroup, mline); 
            		 }
            	 }
            	  
            	 //  if(lopp != null && lopp.contains("2021-08") && clGroup.equals("208"))
                   //    System.out.println("line " + prodID + " " + prGroup + " " + price +" " + discount + " " + algus + " "+ lopp + " " + clGroup);  
              		   
             }
         
           }

		     }
		      
		       
		       }catch(Exception e) {
		    	   e.printStackTrace();
		       }
		   
		//   System.out.println("pm size " + sorted.size());
		Iterator itera =  sorted.keySet().iterator();
		while(itera.hasNext()) {
			  lines.add(sorted.get(itera.next())); 	
		}
		 
		   System.out.println("pm size " + lines.size());
	       return lines;
	   }
    
   
   static int login(String url, String db, String login, String password) throws XmlRpcException, MalformedURLException {
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setEnabledForExtensions(true);

		config.setServerURL(new URL(url+"/xmlrpc/2/common"));
		client.setConfig(config);
		Object[] params = new Object[] {db,login,password}; // Ok & simple
		Object uid = client.execute("login", params);
		if (uid instanceof Integer) {
			return (int) uid;
		}else {
			System.out.println(uid.toString());
		}
			
		return -1;
	}
	  
	   public String getPilotDayData(Agent agent){
	     return null;
	   }
	   
	   public String getSaveDebtInfo() {
	   	return null;
	   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 
	 public String getProductSortingData(){
	   	return null;
	   }

   public ManagerData getManagerData() throws SQLException{
     return null;
  }


   

 
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		return result;
	}

   public DealerProduct DealerProductsList() {
     return null;
   }


       public String getSaveStockInfo(){
            return null;
       }


   public Vector getDebt(String agentCode){
	   try {
		
       	final XmlRpcClient models = new XmlRpcClient() {{
       	    setConfig(new XmlRpcClientConfigImpl() {{
       	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
       	    }});
       	}};
       	
       	 
       	 //this.getFielsnames("account.invoice", models);
       	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
       			    company, uid, password,
       			    "account.invoice", "search_read",
       			    asList(asList(
       			    		asList("state", "=", "open"))), //(java.util.Arrays.asList("employee", "=", false))
       			    new HashMap() {{
       			        put("fields", java.util.Arrays.asList("number", "partner_id","date_invoice",  "date_due",  "residual"));
       			        put("limit", 20000);
       			    }}
       			)));
       	
       	
       	
       	
       	//  System.out.println("Res " + res.toString());
       	  
       	  for (Object json : res){
       		  HashMap row = (HashMap)json;  
       		  
       		  String num = row.get("number")+"";
       		  String payer = null;
       	 
             try {
            	 Object [] countryIdName = (Object []) row.get("partner_id");
                 if (countryIdName != null){
                	 payer = countryIdName[0] + "";
                  }
                } catch(Exception e) {
            	 e.printStackTrace();
                }
       		  
       		  
       		  Debt debt = new Debt();
     		  debt.setCustID(payer); 
              debt.setPayerID(payer);
              debt.setDoknr(num);
             
           
              debt.setSum(Double.parseDouble(row.get("residual")+""));
              debt.setDuedate(getDate(row.get("date_due")+""));                  
              debt.setDate(getDate(row.get("date_invoice")+"")); 
              debt.setDue(days(debt.getDuedate())+"");
          
             
              debtList.addElement(debt);
             
       	  }

       	
      
       } catch (Exception e) {
       	e.printStackTrace();
           System.out.println("Error while reading data from server:\n\n" + e.getMessage());
       }
	   
	   System.out.println("debtList "+ debtList.size());
        return this.debtList;
  }

   
   private int days(Date date){
	   int i = 0;
	   long diff =  new Date().getTime() -  date.getTime()  ;
	   i = (int) (diff / 86400000 );
	   return i;
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

           
         

           public TreeMap getProdGroupsSecond() throws SQLException {
        	   TreeMap tm = new TreeMap();
        	   Object[] params = new Object[]{};
        	
        	   try {    
        	   XmlRpcClient models = new XmlRpcClient() {{
        		    setConfig(new XmlRpcClientConfigImpl() {{
        		        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
        		    }});
        		}};
        		List<Object> res = java.util.Arrays.asList((Object[])models.execute("execute_kw", java.util.Arrays.asList(
        				    company, uid, password,
        				    "product.product", "search_read",
        				    asList(asList(asList("sale_ok", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
        				    new HashMap() {{
        				        put("fields", java.util.Arrays.asList( "categ_id")); // 
        				        put("limit", 2000);
        				    }}
        				)));
        		
        	     StringBuffer sb2 = new StringBuffer();
        	     
        	     for (Object json : res){
        	    	 HashMap row = (HashMap)json;
        	        String asi = "categ_id";
        	     
        	     Object [] countryIdName = (Object []) row.get(asi);
        	     if (countryIdName != null){
        	    	 String country = countryIdName[1] + "";
        	    	 
        	    	 country = country.replaceAll("LEIB-SAI / Leib / ", "L");	    	 
        	    	 country = country.replaceAll("LEIB-SAI / Sai / ", "S");	    	 
        	    	 country = country.replaceAll("LEIB-SAI / Taignad / ", "S");
        	    	 country = country.replaceAll("Väikesai / ", "V");
        	    	 country = country.replaceAll("Küpsis / ", "S");
        	    	 country = country.replaceAll("Sygavkylm / ", "Sy");
        	    	 country = country.replaceAll("TORT / ", "T");
        	    	 
        	    	 if(country.equals("TClara koogid")){
        	    		 country = "Kook";
        	    	 }
        	    	 if(country.equals("TClara tordid")){
        	    		 country = "Tort";
        	    	 }

        	    	 
         	         tm.put(countryIdName[0],Converter.enCodeEst(country));
        	       }
        	      } 
        	       
        	       }catch(Exception e) {
        	    	   e.printStackTrace();
        	       }
        	   System.out.println("groups " + tm.size());
               return tm;
             }

           
   public TreeMap getAgents(){
	   // paneme ainult need agendid kes on meil kirjeldatud
	   Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
  TreeMap agents = new TreeMap();
  agents1 = new TreeMap();
  

  
  Object[] params = new Object[] {};
	      
	        try {
	        
	        	final XmlRpcClient models = new XmlRpcClient() {{
	        	    setConfig(new XmlRpcClientConfigImpl() {{
	        	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
	        	    }});
	        	}};
	        	
	        	 
	        	//  this.getFielsnames("res.users", models);
	        	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
	        			    company, uid, password,
	        			    "res.users", "search_read",
	        			    asList(java.util.Arrays.asList(
	        			    		params)), //(java.util.Arrays.asList("employee", "=", false))
	        			    new HashMap() {{
	        			        put("fields", java.util.Arrays.asList("name"));
	        			        put("limit", 500);
	        			    }}
	        			)));
	        	
	        	  System.out.println("Res " + res.toString());
	        	  
	        	  for (Object json : res){
	        		  HashMap row = (HashMap)json;  
	        		  String agid = row.get("id")+"";
	        		  if(props.containsKey(agid)){
	        				Agent agen = new Agent(agid);
			            	agen.setName(row.get("name")+"");
			                agents.put(agen.getName(), agen); 
			                agents1.put(agid, agen);
	        		  }
	        	  }

	        	
	       
	        } catch (Exception e) {
	        	e.printStackTrace();
	            System.out.println("Error while reading data from server:\n\n" + e.getMessage());
	        }
	     
	     //  getProducts(null);
           // System.exit(0);
	     
     return agents;
   }



	public String getDistribution() {
         return null;
       }
 
	
      private Vector<Integer> prlInUse = new Vector<Integer>();
  
      
      
       public TreeMap getCustomers(Agent agent){

    	   try{
         	   Converter.getManagersReplacements(agents1, rcompany, user, pass, false); 
           } catch(Exception e){
         	e.printStackTrace();
           }
       
         TreeMap tm = new TreeMap();
      
   	  	
	    	try{

	        	final XmlRpcClient models = new XmlRpcClient() {{
	        	        setConfig(new XmlRpcClientConfigImpl() {{
	        	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
	        	    }});
	        	}};
	        	
	        Iterator itera = agents1.keySet().iterator();
	         ArrayList<Integer> a = new ArrayList<Integer>();
	         while(itera.hasNext()) {
	        	 a.add(Integer.parseInt(itera.next()+""));
	         }
	        	
	      System.out.println("agendid kellel kliente vaatame " + a.toString());
	    		
	      
	      TreeMap<String,Double> debts = new TreeMap<String,Double>();
	      
	    	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
       			    company, uid, password,
       			    "account.invoice", "search_read",
       			    asList(asList(
       			    		asList("state", "=", "open"),asList("date_due", "<", Converter.getYYYY_MM_DDpString(Calendar.getInstance())))), //(java.util.Arrays.asList("employee", "=", false))
       			    new HashMap() {{
       			        put("fields", java.util.Arrays.asList("number", "partner_id","date_invoice",  "date_due",  "residual"));
       			        put("limit", 20000);
       			    }}
       			)));
       	
	     
	      
       	  for (Object json : res){
       		  HashMap row = (HashMap)json;  
       		  
       		  String payer = null;
       		  double sum =  Double.parseDouble(row.get("residual")+"");
       	 
             try {
            	 Object [] countryIdName = (Object []) row.get("partner_id");
                 if (countryIdName != null){
                	 payer = countryIdName[0] + "";
                  }
                } catch(Exception e) {
            	 e.printStackTrace();
                }
       		  if(debts.get(payer) != null) {
       			 debts.put(payer,debts.get(payer)+sum);
       		  }else {
       			 debts.put(payer, sum);
       		  }
             
       	  }
	      
       	 // System.out.println("Debts " + debts.toString());
	      
	      
	    		//  this.getFielsnames("res.partner", models);
	        	 res  =	 asList((Object[])models.execute("execute_kw", asList(
	        			    company, uid, password,
	        			    "res.partner", "search_read",
	        			    asList(asList( asList("user_id", "in", a ))), //(java.util.Arrays.asList("employee", "=", false))
	        			    new HashMap() {{
	        			        put("fields", java.util.Arrays.asList("street","customer","city",
	        			        		   "claims_ids","state_id","post_address","parent_id",
	        			        		   "indirect_parent_id","retail_chain_id","phonecall_ids","user_ids","task_ids","country_id",
	        			        		   "external_ref_id","user_id",
	        			        		   "child_ids","phone",       		   
	        			        		   "category_id","section_id","country","email","purchase_order_ids","company_id"
	        			        		  ,"name","property_product_pricelist"));
	        			        put("limit", 5000);
	        			    }}
	        			)));
	        	
	        	
	        	  
	        for (Object json : res){
	       
	        HashMap row = (HashMap)json; 
	       
	        
             String cuid = row.get("id") + "";
             String agid = null;  
             try {
            	 Object [] countryIdName = (Object []) row.get("user_id");
                 if (countryIdName != null){
                	 agid = countryIdName[0] + "";
                  }
                  
             }catch(Exception e) {
            	 e.printStackTrace();
             }
             
             
           
         //    System.out.println("Res kliendid " + agid + " " + row.toString());
             
             String street = row.get("street") + "";
             String city = row.get("city") + "";
             String country = "";
             String asi = "country_id";
             try {
            	 Object [] countryIdName = (Object []) row.get(asi);
                 if (countryIdName != null){
                   country = countryIdName[1] + "";
                  }
                  
             }catch(Exception e) {
            	// e.printStackTrace();
             }
             
               Customer cust = new Customer(cuid);
               cust.setName(Converter.enCodeEst(row.get("name") + ""));
               
               if(!agid.equals("null")) {
            	   cust.addAgent(new Agent(agid));
               }
               Agent aa1 = (Agent)agents1.get(agid.trim());
           	if(aa1 != null) {
	           		Vector vec = aa1.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String age = (String) en.nextElement();
								cust.addAgent(new Agent(age));
							}
						}
	              }
               
               String aard = "";
               if(street != null && street.trim().length() > 0 && !street.equals("null")){
            	   aard = street;
               }
               
               if(city != null && city.trim().length() > 0  && !city.equals("null") ){
            	   aard = aard + " " + city;
               }
               if(country != null && country.trim().length() > 0  && !country.equals("null")){
                  
               }
               
               if( aard.length() > 0 ) {
            	   cust.setAddress(Converter.adressLink(Converter.enCodeEst(aard), "Estonia"));
               }
              
                
               Object []  countryIdName =  null;
          
          try {
        	   countryIdName = (Object []) row.get("retail_chain_id");
              if (countryIdName != null && countryIdName.length > 0){
               cust.setChain( countryIdName[1] + "" );
               }   
                
          }catch(Exception e) {
        	  e.printStackTrace();
          }
                
          
 countryIdName =  null;
          
          try {
        	   countryIdName = (Object []) row.get("property_product_pricelist");
        	//   System.out.println(" hkiri" + countryIdName + "" );
              if (countryIdName != null && countryIdName.length > 0){
            	//  System.out.println(" hkiri" + countryIdName[0] + "" );
            	  Integer id = Integer.parseInt(countryIdName[0] +"");
            	
                   cust.setGroup(id+"");
                   if(!prlInUse.contains(id)) {
                	      prlInUse.add(id);
                	     // System.out.println(id + " " +countryIdName[1]);
                   }
             
               }   
                
          }catch(Exception e) {
        	  e.printStackTrace();
          }
                
                
               String email = row.get("email") + "";
               String phone = row.get("phone") + "";
                String conta = "";
                if(phone != null && phone.trim().length() > 0 && !phone.equals("null")){
                	conta = conta + phone;
                }
               
                if(email != null && email.trim().length() > 0 && !email.equals("null")){
                	conta = conta + " " + email;
                }
                
                if(debts.get(cuid) != null) {
                	cust.setDebt(debts.get(cuid));
                }
                
                tm.put(cust.getName(), cust);

           }

           }catch(Exception e){
	    	e.printStackTrace();	
	    	}
	    	    
    	     return tm;
    	   }

   public TreeMap getAgents1() {
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   
   
   private TreeMap<String,String> grs = new TreeMap<String,String>();
   
   
   public TreeMap getProdGroups() throws SQLException {
	   TreeMap tm = new TreeMap();
	  
	
	   try {    
	   XmlRpcClient models = new XmlRpcClient() {{
		    setConfig(new XmlRpcClientConfigImpl() {{
		        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
		    }});
		}};
		
		
		//this.getFielsnames("product.category", models);
		List<Object> res = java.util.Arrays.asList((Object[])models.execute("execute_kw", java.util.Arrays.asList(
				    company, uid, password,
				    "product.category", "search_read",
				    params, //(java.util.Arrays.asList("employee", "=", false))
				    new HashMap() {{
				        put("fields", java.util.Arrays.asList( "id","name","parent_id","parent_right","parent_left")); // 
				        put("limit", 2000);
				    }}
				)));
		
		

	     
	     for (Object json : res){
	    	 HashMap row = (HashMap)json;
	      
	     //System.out.println("asi " + row.toString());
	     String country =  null;
    	 
	     try {
	    	 
	    	  String asi = "parent_id";
	     Object [] countryIdName = (Object []) row.get(asi);
	     if (countryIdName != null){
	    	  country = countryIdName[0] + "";
	    	// System.out.println("asi00 " + countryIdName.length + " "+ country + " " + row.get("id"));
	    	  grs.put(row.get("id")+"", country);
	 	     }
         
	     } catch(Exception e) {
	    	 
	     }
	     
	    
	     
	     if(country == null) {
	    	 tm.put(row.get("id"),Converter.enCodeEst(row.get("name")+ " "+ row.get("id")));
		 }
	     
	      } 
	       
	       }catch(Exception e) {
	    	   e.printStackTrace();
	       }
	   System.out.println("groups0 " + tm.toString());
       return tm;
   }
   
   private void getFielsnames(String tabel, XmlRpcClient models) throws Exception {
	   Object  res2 = 	  (Map<String, Map<String, Object>>)models.execute("execute_kw", asList(
			    company, uid, password,
			    tabel, "fields_get",
			    new ArrayList(),
			    new HashMap() {{
			        put("attributes", java.util.Arrays.asList("string","type"));
			    }}
			));
	  
	  System.out.println("Tabel " + tabel + " " + res2.toString());
   }
   
   public TreeMap getProducts(Agent agent){
	   
	   try {
			getProdGroups();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
     TreeMap tm = new TreeMap();
   
      TreeMap  ladutr = new TreeMap();
      Object[] params = new Object[] {};
      TreeMap names = new TreeMap();
  try {    
  	final XmlRpcClient models = new XmlRpcClient() {{
	    setConfig(new XmlRpcClientConfigImpl() {{
	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
	    }});
	}};
	
	
//	this.getFielsnames("product.category", models);
  //  System.exit(0);
	List<Object> res0 = java.util.Arrays.asList((Object[])models.execute("execute_kw", asList(
			    company, uid, password,
			    "product.product", "search_read",
			    asList(asList(asList("sale_ok", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
			    new HashMap() {{
			        put("fields", java.util.Arrays.asList("name")); // 
			        put("limit", 6000);
			    }}
			)));
	
	 
	  for (Object json : res0){
		  HashMap row = (HashMap)json;  
		  names.put(row.get("id")+"", row.get("name")+"");
	  }
	  
	  
	
      
	List<Object> res = java.util.Arrays.asList((Object[])models.execute("execute_kw", asList(
			    company, uid, password,
			    "product.product", "search_read",
			    asList(java.util.Arrays.asList(
			    		asList("sale_ok", "=", true),
			    		asList("type", "=", "product"),
			    		asList("warranty", "=",0.01 ),
			    	//	asList("id", "=",25714 ),
			    		asList("active", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
			    new HashMap() {{
			        put("fields", java.util.Arrays.asList( "price_extra","product_tmpl_id",
			       		 "type","packaging","description","warehouse_id", "product_category",
			       		 "reception_count","code","rental","track_production","price","price_margin","qty_available",
			       		 "active","weight_net","default_code","weight" ,"name","prod_quant","only_full",
			       		 "produce_delay","list_price","sale_delay","property_stock_account_input",
			       		 "lst_price","delivery_count", "volume","lst_price_switch","ean13","categ_id","packaging_ids")); // 
			        put("limit", 3000);
			    }}
			)));
	 
	/* */
	
	
     
     for (Object json : res){

    	 HashMap row = (HashMap)json;
    	// System.out.println("Res " + row.toString());
         Product prod = new Product(row.get("id")+"");
         String code = row.get("default_code") + "";
         if(code.equals("false")) {
        	 code = "";
         }
         
         String ean = row.get("ean13") +"";
         String add = "";
         if(!ean.equals("false")) {
        	 prod.setEAN(ean);
        	 if(ean.length() > 0) {
        		 add = ean.substring(ean.length() - 4);
        	 }
        		if(eanprods == null) {
            		eanprods= new TreeMap();
            	}
          
            eanprods.put(prod.getEAN(), prod.getID());
         }
         
         prod.setName(Converter.enCodeEst(Converter.firstToUpperCase(row.get("name")+"") + " " + add));
         prod.setPartii("Kood: " + code);
       	
         String christ = getParent().getProps().getProperty("odoo.christ");	
         if(christ != null && prod.getName().toLowerCase().contains(christ)) {
        	 prod.setStatusTop("5");
         }
         
        
         
        String idname = null;
	         Object [] countryIdName = (Object []) row.get("product_tmpl_id");
	      //   System.out.println(countryIdName.toString() + " " + prod.getName());
	         if (countryIdName != null){
	          String country = countryIdName[1] + "";
	          idname = (country);
	           }
         
        // System.out.println(idname + " " + prod.getName());
         if(idname != null && names.get(idname) != null) {
        	 prod.setName(Converter.enCodeEst(names.get(idname)+" " + code));
          }
         
         String opt = row.get("only_full") +"";
         String incase = row.get("prod_quant") +"";
         
         if(!incase.equals("null")) {
        	 prod.setInCases(Double.parseDouble(incase)*2);
        	 if(opt.equals("true")) {
        		 prod.setOptAmountInUnits(prod.getInCases());
        	 }
        	  }
         
         
         
         
     String country = null;
    
     
     
     String asi = "categ_id";
     
    countryIdName = (Object []) row.get(asi);
     if (countryIdName != null){
       country = countryIdName[0] + "";
       prod.setGroupSecond(country);
       if(grs.get(country) != null) {
    	   String gr = grs.get(country);
    	  
    	   if(grs.get(gr) != null) {
    		   gr  = grs.get(gr);
    	   }
    	   if(grs.get(gr) != null) {
    		   gr  = grs.get(gr);
    	   }
    	   
    	   prod.setGroup(gr);
       }
    	  if(prod.getName().toLowerCase().contains("amps")) {
    		  System.out.println(prod.getGroup() + " " + country + " " + prod.getName());  
    	  }
       
       }
     
     
     
     
     asi = "packaging_ids";
     
     countryIdName = (Object []) row.get(asi);
    // System.out.println(" " +countryIdName.getClass().getName() + " " + idname);
      if (countryIdName != null && countryIdName.length > 0){
    	  try {
    		    country = countryIdName[0] + "";;
    	        System.out.println(" " +country + " " + idname);
    	  }catch(Exception e) {
    		  e.printStackTrace();
    	  }
        
        } 
     try {
    	 prod.setPrice(Double.parseDouble(row.get("lst_price_switch")+""));   
     }catch(Exception e) {
    	e.printStackTrace(); 
     }
   //  prod.setStock(0);
     try {
    	 double st = Double.parseDouble(row.get("qty_available")+"");
    	 if(st > 0) {
    		 prod.setStock(Double.parseDouble(row.get("qty_available")+""));   
    	 }
    	
     }catch(Exception e) {
    	e.printStackTrace(); 
     }
   
     
     tm.put(prod.getName(), prod);
     }

}catch(Exception e){
  	e.printStackTrace();	
  	}
System.out.println("tm.size() " + tm.size());
     return tm;
   }

 
 
   
   public static String deCode(String res ){

	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     TreeMap kord = new TreeMap();
     try{

       	final XmlRpcClient models = new XmlRpcClient() {{
       	        setConfig(new XmlRpcClientConfigImpl() {{
       	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
       	    }});
       	}};
       	
       	Calendar now = Calendar.getInstance();
       	now.add(Calendar.DAY_OF_YEAR, 1);
       	
     	Calendar mstart = Calendar.getInstance();
     	mstart.set(Calendar.DAY_OF_YEAR, -30);

   	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
  			    company, uid, password,
  			    "stock.move", "search_read",
  			    asList(asList(
  			    		asList("state", "in", asList("done")),
  			    		asList("picking_id", "!=", false),
  			    		asList("date", "<", Converter.getYYYY_MM_DDpString(now)),
  			    		asList("date", ">", Converter.getYYYY_MM_DDpString(mstart))
  			    		)), 
  			    new HashMap() {{
  			        put("fields", java.util.Arrays.asList("name", "product_id","partner_id", "product_uom_qty", "price_unit","date"));
  			        put("limit", 150000);
  			    }}
  			)));
  	
    //System.out.println(res);
  
    
  	  for (Object json : res){
  		  HashMap row = (HashMap)json;  
  		 String klient = null;
  		  
  		String  asi = "partner_id";
  	     try {
  	    	 
  	    
  		Object []  countryIdName = (Object []) row.get(asi);
  	    // System.out.println(" " +countryIdName.getClass().getName() + " " + idname);
  	      if (countryIdName != null && countryIdName.length > 0){
  	    	  try {
  	    		    klient = countryIdName[0] + "";
  	    	  }catch(Exception e) {
  	    		  e.printStackTrace();
  	    	  }
  	        } 
  	    String  nimi = row.get("name")+"";
  		String date =  row.get("date")+"";
  		String lastdate = date.substring(5, 10);
  	    String prod = null;
  	    
  	      asi = "product_id";
	      countryIdName = (Object []) row.get(asi);
	    
	     // System.out.println(" " +countryIdName.getClass().getName() + " " + idname);
	    
	     if (countryIdName != null && countryIdName.length > 0){
	    	  try {
	    		    prod = countryIdName[0] + "";
	    	  }catch(Exception e) {
	    		  e.printStackTrace();
	    	  }
	        } 
  		  
  		  double kogus =  Double.parseDouble(row.get("product_uom_qty") + "");
  		  double hind =  Double.parseDouble(row.get("price_unit") + "");
  		
  		  if(klient != null && prod != null) {
  			   CustProd cp = new CustProd(klient, prod, kogus ,0 ,0 ,lastdate,hind,0,0," ");
  		        kord.put(klient+nimi+date.substring(0,10), cp);  
  		  }
	     
	        
  	     } catch(Exception r) {
	    	 
	     }
  	  }
  	  
  	  System.out.println("kord " + kord.size());
  	  
  	  TreeMap sort = new TreeMap();
  	  
  	 Iterator itera = kord.keySet().iterator();
  	 while (itera.hasNext()) {
  		 String aa = (String) itera.next();
  		 String bb = aa.substring(0,aa.length()-10);
  		 sort.put(bb, kord.get(aa)); // nii tuleb viimane sisse siit
  		// System.out.println("sees " + aa); 
  	  }
  	   System.out.println("sort " + sort.size());
  	   
  	   
  	 itera = sort.keySet().iterator();
  	 while (itera.hasNext()) {
  		 String aa = (String) itera.next();
  		 vec.add(sort.get(aa)); 
  	  }
    
     } catch(Exception e) {
 	     e.printStackTrace();
      }
    
     
   //  System.exit(0);
 
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ODOOApi";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
 
   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


 

  public void setBulkSales(Map agents) {
	  try{

      	final XmlRpcClient models = new XmlRpcClient() {{
      	        setConfig(new XmlRpcClientConfigImpl() {{
      	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
      	    }});
      	}};
      	
      	Calendar now = Calendar.getInstance();
      	now.add(Calendar.DAY_OF_YEAR, 1);
      	
    	Calendar mstart = Calendar.getInstance();
    	mstart.set(Calendar.DAY_OF_MONTH, 1);

  	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
 			    company, uid, password,
 			    "sale.order", "search_read",
 			    asList(asList(
 			    		asList("state", "in", asList("confirmed", "exception", "done")),
 			    		asList("date_order", "<", Converter.getYYYY_MM_DDpString(now)),
 			    		asList("date_order", ">", Converter.getYYYY_MM_DDpString(mstart))
 			    		)), //(java.util.Arrays.asList("employee", "=", false))
 			    new HashMap() {{
 			        put("fields", java.util.Arrays.asList("user_id", "amount_untaxed"));
 			        put("limit", 20000);
 			    }}
 			)));
 	
  // System.out.println(res);
    
 	  for (Object json : res){
 		  HashMap row = (HashMap)json;  
 		 String ag = null;
 		  
 		String  asi = "user_id";
 	     
 		Object []  countryIdName = (Object []) row.get(asi);
 	    // System.out.println(" " +countryIdName.getClass().getName() + " " + idname);
 	      if (countryIdName != null && countryIdName.length > 0){
 	    	  try {
 	    		    ag = countryIdName[0] + "";
 	    		 //  System.out.println(" " + ag);
 	    	  }catch(Exception e) {
 	    		  e.printStackTrace();
 	    	  }
 	        } 
 		  
 		  
 		  
 		  double sales =  Double.parseDouble(row.get("amount_untaxed")+"");
 		  if(ag != null) {
 		 Agent agent = (Agent)agents.get(ag);
         if (agent != null){
       	   agent.setMonthSales((int)sales);
             }
 	        }
 		  }
	//  System.out.println("Agents " + agents.toString());

     } catch(Exception e) {
	     e.printStackTrace();
     }
	  
  }

  private String username = null;
  private String company = null;
  private String password = null;
  private String server = null;
  
}
