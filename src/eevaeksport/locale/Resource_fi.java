package eevaeksport.locale;

import java.io.IOException;
import java.util.PropertyResourceBundle;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Resource_fi extends PropertyResourceBundle {

  public Resource_fi() throws IOException{
    super(Resource_fi.class.getResourceAsStream("resource_fi.properties"));
  }
}