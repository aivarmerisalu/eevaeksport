/*
 * Created on 05.06.2008
 */
package eevaeksport.routines;

import java.io.Serializable;

public final class EAN13CheckDigit extends ModulusCheckDigit implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Singleton EAN-13 Check Digit instance */
    public static final CheckDigit INSTANCE = new EAN13CheckDigit();

    private static final int[] POSITION_WEIGHT = new int[] {3, 1};

    public EAN13CheckDigit() {
        super(10);
    }

    protected int weightedValue(int charValue, int leftPos, int rightPos) {
        int weight = POSITION_WEIGHT[rightPos % 2];
        return (charValue * weight);
    }
}
