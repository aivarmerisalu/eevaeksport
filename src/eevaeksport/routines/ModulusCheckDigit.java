/*
 * Created on 05.06.2008
 */
package eevaeksport.routines;
public abstract class ModulusCheckDigit implements CheckDigit {

    private int modulus;


    public ModulusCheckDigit(int modulus) {
        this.modulus = modulus;
    }


    public int getModulus() {
        return modulus;
    }


    public boolean isValid(String code) {
        if (code == null || code.length() == 0) {
            return false;
        }
        try {       
            int modulusResult = calculateModulus(code, true);
            return (modulusResult == 0);
        } catch (CheckDigitException  ex) {
            return false;
        }
    }

  
    public String calculate(String code) throws CheckDigitException {
        if (code == null || code.length() == 0) {
            throw new CheckDigitException("Code is missing");
        }
        int modulusResult = calculateModulus(code, false);
        int charValue = (modulus - modulusResult) % modulus;
        return toCheckDigit(charValue);
    }


    protected int calculateModulus(String code, boolean includesCheckDigit) throws CheckDigitException {
        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            int lth = code.length() + (includesCheckDigit ? 0 : 1);
            int leftPos  = i + 1;
            int rightPos = lth - i;
            int charValue = toInt(code.charAt(i), leftPos, rightPos);
            total += weightedValue(charValue, leftPos, rightPos);
        }
        if (total == 0) {
            throw new CheckDigitException("Invalid code, sum is zero");
        }
        return (total % modulus);
    }

   
    protected abstract int weightedValue(int charValue, int leftPos, int rightPos)
            throws CheckDigitException;


 
    protected int toInt(char character, int leftPos, int rightPos)
            throws CheckDigitException {
        if (Character.isDigit(character)) {
            return Character.getNumericValue(character);
        } else {
            throw new CheckDigitException("Invalid Character[" + 
                    leftPos + "] = '" + character + "'");
        }
    }


    protected String toCheckDigit(int charValue)
            throws CheckDigitException {
        if (charValue >= 0 && charValue <= 9) {
            return Integer.toString(charValue);
        } else {
            throw new CheckDigitException("Invalid Check Digit Value =" + 
                    + charValue);
        }
    }

 
    public static int sumDigits(int number) {
        int total = 0;
        int todo = number;
        while (todo > 0) {
            total += todo % 10;
            todo  = todo / 10;
        }
        return total;
    }

}
