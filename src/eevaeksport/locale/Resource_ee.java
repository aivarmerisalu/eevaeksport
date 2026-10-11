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

public class Resource_ee extends PropertyResourceBundle {

  public Resource_ee() throws IOException{
    super(Resource_ee.class.getResourceAsStream("resource_ee.properties"));
  }
}