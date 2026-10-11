/*
 * Created on 05.06.2008
 */
package eevaeksport.routines;


public interface CheckDigit {

    
    public String calculate(String code) throws CheckDigitException;

    public boolean isValid(String code);

}
