/*
 * Created on 30.12.2007
 */
package eevaeksport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class OrderFormReader
{
    Vector  itemNumbers;

    public OrderFormReader( String filename ) throws IOException
    {
        itemNumbers = new Vector();

        readFile( filename );
    }

    private void readFile( String filename ) throws IOException
    {
        BufferedReader      reader = new BufferedReader( new FileReader( filename ) );
        String              line;
        Vector                 col1 = new Vector();
        Vector                  col2 = new Vector();
        Vector                  col3 = new Vector();

        itemNumbers.clear();
        
        while ( ( line = reader.readLine() ) != null ) {
//System.out.println(line);
                /* Uusi sivu */
            line = line.replaceAll(";","; ");
                String[] s = line.split( ";" );

                try {
                    /* Tarkista, onko s[0] tuotetietokannassa */
                    
                    if ( s[0].trim().length() > 0 ) {
                        col1.add( s[0] );
                        
                    }
                }
                catch ( Exception e ) {
                    /* ignore */
                }

                try {
                    /* Tarkista, onko s[4] tuotetietokannassa */

                    if( s[4].trim().length() > 0 ) {
                        col2.add( s[4] );
                    }
                }
                catch ( Exception e ) {
                }

                try {
                    /* Tarkista, onko s[8] tuotetietokannassa */

                    if ( s[8].trim().length() > 0 ) {
                        col3.add( s[8] );
                    }
                }
                catch( Exception e ) {
                }
            
        }
        
        itemNumbers.addAll( col1 );
        itemNumbers.addAll( col2 );
        itemNumbers.addAll( col3 );
        System.out.println("is " + itemNumbers.size());


    }

    public Vector getItemNumbers()
    {
        return ( itemNumbers );
    }

    public static void main( String[] args )
    {
        try {
            OrderFormReader     formReader = new OrderFormReader( args [0] );

            Vector itemNumbers = formReader.getItemNumbers();

            Iterator i = itemNumbers.iterator();
            while ( i.hasNext() ) {
                System.out.println( ((String) i.next()) );
            }

            System.exit( 0 );
        }
        catch ( Exception e ) {
            e.printStackTrace();

            System.exit( 1 );
        }
    }
}

