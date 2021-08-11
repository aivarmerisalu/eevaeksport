package eevaeksport;

import ee.mobilejava.util.Searchable;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ListItem implements Searchable{

  public ListItem(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String toString(){
    return name;
  }

  public String getID(){
    return this.id;
  }

  public String getName(){
    return this.name;
  }

  public String getSearchString(){
    return getName();
  }

  private String id = null;
  private String name = null;
}