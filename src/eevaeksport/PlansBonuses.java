/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class PlansBonuses {

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public PlansBonuses() {}

    /**
     * Method declaration
     *
     *
     * @param tootegrupp
     * @param plaan
     * @param bonus
     *
     * @see
     */
    public void add(int tootegrupp, double plaan, double bonus, double plaanDone, double bonusDone) {
	PlanBonusLine pbl = (PlanBonusLine)data.get(String.valueOf(tootegrupp));
        if (pbl == null){
          data.put(String.valueOf(tootegrupp), new PlanBonusLine(plaan, bonus, plaanDone, bonusDone));
        } else {
          pbl.setPlaan(plaan);
          pbl.setBonus(bonus);
          pbl.setPlaanDone(plaanDone);
          pbl.setBonusDone(bonusDone);
        }
    }

    public String getCSVData(){
      StringBuffer sb = new StringBuffer();
      Iterator iter = data.keySet().iterator();
      while(iter.hasNext()){
        String grp = (String)iter.next();
        int tootegrupp = Integer.parseInt(grp);
        PlanBonusLine line = (PlanBonusLine)data.get(grp);
        sb.append(tootegrupp + ";" + line.getPlaan() + ";" + line.getBonus() + ";");
        sb.append(line.getPlaanDone() + ";" + line.getBonusDone() + "\n");
      }

      return sb.toString();
    }


        private TreeMap data = new TreeMap();
}

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
class PlanBonusLine {

    /**
     * Constructor declaration
     *
     *
     * @param tootegrupp
     * @param plaan
     * @param bonus
     *
     * @see
     */
    public PlanBonusLine(double plaan, double bonus, double plaanDone, double bonusDone) {
      setPlaan(plaan);
	setBonus(bonus);
        setPlaanDone(plaanDone);
        setBonusDone(bonusDone);
    }

    private double plaan;
    private double bonus;
    private double plaanDone;
    private double bonusDone;

    /**
     *
     * @return the double value of plaanDone.
     */
    public double getPlaanDone() {
	return plaanDone;
    }

    /**
     *
     * @param aPlaanDone - the new value for plaanDone
     */
    public void setPlaanDone(double aPlaanDone) {
	plaanDone = aPlaanDone;
    }

    /**
     *
     * @return the double value of bonusDone.
     */
    public double getBonusDone() {
	return bonusDone;
    }

    /**
     *
     * @param aBonusDone - the new value for bonusDone
     */
    public void setBonusDone(double aBonusDone) {
	bonusDone = aBonusDone;
    }

   /**
     *
     * @return the double value of plaan.
     */
    public double getPlaan() {
	return plaan;
    }

    /**
     *
     * @param aPlaan - the new value for plaan
     */
    public void setPlaan(double aPlaan) {
	plaan = aPlaan;
    }

    /**
     *
     * @return the double value of bonus.
     */
    public double getBonus() {
	return bonus;
    }

    /**
     *
     * @param aBonus - the new value for bonus
     */
    public void setBonus(double aBonus) {
	bonus = aBonus;
    }

}



/*--- formatting done in "Sun Java Convention" style on 10-07-2002 ---*/

