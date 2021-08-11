/*
 * Created on 29.08.2007
 */
package eevaeksport;

import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public class LogistikaPluss {

    public LogistikaPluss() {
    //	this.parent = parent;
    	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	  
        }
	
	
    public void ManageProducts(TreeMap products){
    	
    }
  
    
    public void ManageCustomers(TreeMap customers){
    	
    }
    
    
    public void AddOrder(Order order){
    	
    }
    
    public Vector getOrders (){
    	Vector orders = new Vector();
    	return orders;
    }
    
    
    private ImportPanel  parent = null;

    
}
