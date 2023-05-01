
package eevaeksport.keygenjava;


public class KeyGen
{

    private KeyGen()
    {
    }

    public static String getKey(String name, String id)
    {
    	
    
        return String.valueOf((id + name + id).hashCode());
    }
}
