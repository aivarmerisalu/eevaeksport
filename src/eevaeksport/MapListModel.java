package eevaeksport;

import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MapListModel implements ListModel{

  public MapListModel( TreeMap custs) {
    this.custs = custs;
    this.size = custs.size();  
  }

  public void removeListDataListener(ListDataListener listener){
  }

  public void addListDataListener(ListDataListener listener){
  }

  public Object getElementAt(int xxx){
    	Iterator iter  = custs.keySet().iterator();
    	int i = 0;
    	String key = "";
    	while(iter.hasNext()){
    		if(i==xxx){
    			key = (String)iter.next();
          		}
    		i++;
    	}
    	
    Customer cust = (Customer)custs.get(key);;
      String id = cust.getID();
      String name = cust.getName();
    //  String name = (String)rs.getObject(2);
      if (name != null){
        name = name.trim();
      }
      ListItem item = new ListItem(id, name);
    
      return item;
    
  }

  public int getSize(){
    return size;
  }

  /**
   *
   * @return the ResultSet value of rs.
   */
  public TreeMap getCusts(){
    return custs;
  }

  private TreeMap custs = null;
  private int size = -1;
}
