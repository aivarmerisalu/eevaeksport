package eevaeksport;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DealerProduct {

  public DealerProduct() {
  }

  public void add(String yksus, String product,String stock,String laokuup,String deacode){
    Vector products = (Vector)list.get(String.valueOf(yksus));
    if (products == null){
      products = new Vector();
      list.put(String.valueOf(yksus), products);
    }
    if(!getDealers().contains(yksus)){
    	 this.getDealers().add(yksus);	
    }
   
    products.addElement(product);
    products.addElement(stock) ;
    products.addElement(laokuup) ;
    products.addElement(deacode);
    Hashtable tooted = (Hashtable)list2.get(yksus);
    if(tooted != null){
    	tooted.put(product, stock);
    	 list2.put(yksus, tooted);
    }else {
    	tooted = new Hashtable();
    	tooted.put(product, stock);
    	 list2.put(yksus, tooted);
    }
    Hashtable partiid = (Hashtable)list3.get(yksus);
    if(partiid != null){
    	partiid.put(product, laokuup);
    	 list3.put(yksus, partiid);
    }else {
    	partiid = new Hashtable();
    	partiid.put(product, laokuup);
    	 list3.put(yksus, partiid);
    }
   
    
  }

  public String getCSVData(){
    StringBuffer sb = new StringBuffer();
    Enumeration enum1 = list.keys();
    while(enum1.hasMoreElements()){
      String key = (String)enum1.nextElement();
      Enumeration prods = ((Vector)list.get(key)).elements();
      while(prods.hasMoreElements()){
        String prodID = (String) prods.nextElement();
        String stock = (String) prods.nextElement();
        String laokuup = (String) prods.nextElement();
        String deacode = (String) prods.nextElement();

          sb.append(key + ";" + prodID + ";" + stock + ";" + laokuup + ";" + deacode + "\n");
      }
    }
    return sb.toString();
  }

  public void remDealerProduct(String dealer,String prodid){
    Enumeration enums= list.keys();
    
    while(enums.hasMoreElements()){
      String key = (String)enums.nextElement();
      int i = 0;
      Enumeration prods = ((Vector)list.get(key)).elements();
      while(prods.hasMoreElements()){
        String prodID = (String) prods.nextElement();
        if(dealer != null && dealer.equals(key) && prodid.equals(prodID)){
        	((Vector)list.get(key)).removeElementAt(i);
        }
        i++;
      }
    }
  }

  
  public String getDealerProductLOT(String dealer,String prodid){
  if(dealer != null){
	  Hashtable vv = (Hashtable)list3.get(dealer);
	//  System.out.println(dealer + " " + prodid + " " + vv.size() + vv.toString() );
           try{
        	   String partii = (String)vv.get(prodid);
               return partii ;
           }catch (Exception e){
        	  // e.printStackTrace();
           }            
  }
    return null;
  }
  
  public double getDealerProductStock(String dealer,String prodid){
  if(dealer != null){
	  Hashtable vv = (Hashtable)list2.get(dealer);
	//  System.out.println(dealer + " " + prodid + " " + vv.size() + vv.toString() );
           try{
        	   String stocks = (String)vv.get(prodid);
        	  
        	   double stock = Double.parseDouble(stocks);
        	   
             return stock ;
           }catch (Exception e){
        	  // e.printStackTrace();
           }            
  }
    return -1;
  }

  private Vector dealers = new Vector();
  public Vector getDealers() {
	return dealers;
}

public void setDealers(Vector dealers) {
	this.dealers = dealers;
}

private Hashtable list = new Hashtable();
public Hashtable list2 = new Hashtable();
public Hashtable list3 = new Hashtable();
}
