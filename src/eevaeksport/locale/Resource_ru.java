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

public class Resource_ru extends PropertyResourceBundle {

  public Resource_ru() throws IOException{
    super(Resource_ru.class.getResourceAsStream("resource_ru.properties"));
  }
}