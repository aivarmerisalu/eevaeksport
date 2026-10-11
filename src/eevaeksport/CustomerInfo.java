/*
 * Created on 19.02.2006
 */
package eevaeksport;
import java.util.Date;
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public class CustomerInfo {

	 public CustomerInfo(String id) {
	    this.id = id;
	  }

	private String id = null;
	private String  peatatus = " ";
	private String  geograafia  = " ";	
	private String  myygiregioon = " ";
	private String  lahtiolek = " ";
	private String  helistAlg = " ";
	private String  helistLopp = " ";
	private String  vastutajaPerson = " ";
	private String  vastutajaJob = " ";
	private String  vastutajaPhone = " ";
	private String  vastutajaEmail = " ";
	private Date  vastutajaSynna = null;
	private String inventar = " ";
	private String kaupluseLiik = " ";
	private String  hooajalisus = " ";
	private String  kliendiProfiil = " ";
	private String  otsustusvoime = " ";
	private String  myykPerioodis = " ";
	private String  kylastusPaev = " ";
	private String sep = ";";
	private String kylm = " ";
	private String selve = " ";
	private String grill = " ";
	private String teenindus = " ";
	private String helistPaev = " ";
	
	public String getStringForCustomerInfoFile(){
		String ss = id + sep  +
		            peatatus + sep  +
					geograafia + sep  +
					lahtiolek + sep  +
					kaupluseLiik + sep  + 
					hooajalisus + sep  +
					kliendiProfiil + sep  +
					myygiregioon + sep  +
					otsustusvoime + sep  +
					selve + sep +
					teenindus + sep +
					kylm + sep +
					grill + sep +
					myykPerioodis + sep  +
                    kylastusPaev + sep +
					helistPaev + sep +
					helistAlg + sep  +
					helistLopp + sep  +			
					inventar + sep  +
					vastutajaPerson + sep  +
					vastutajaJob + sep  +
					vastutajaPhone + sep  +
					vastutajaEmail + sep  +
					vastutajaSynna ;
		return ss;
	}
	/**
	 * @return Returns the geograafia.
	 */
	public String getGeograafia() {
		return geograafia;
	}
	/**
	 * @param geograafia The geograafia to set.
	 */
	public void setGeograafia(String geograafia) {
		this.geograafia = geograafia;
	}
	/**
	 * @return Returns the helistAlg.
	 */
	public String getHelistAlg() {
		return helistAlg;
	}
	/**
	 * @param helistAlg The helistAlg to set.
	 */
	public void setHelistAlg(String helistAlg) {
		this.helistAlg = helistAlg;
	}
	/**
	 * @return Returns the helistLopp.
	 */
	public String getHelistLopp() {
		return helistLopp;
	}
	/**
	 * @param helistLopp The helistLopp to set.
	 */
	public void setHelistLopp(String helistLopp) {
		this.helistLopp = helistLopp;
	}
	/**
	 * @return Returns the hoojalisus.
	 */
	public String getHooajalisus() {
		return hooajalisus;
	}
	/**
	 * @param hoojalisus The hoojalisus to set.
	 */
	public void setHooajalisus(String hoojalisus) {
		this.hooajalisus = hoojalisus;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the inventar.
	 */
	public String getInventar() {
		return inventar;
	}
	/**
	 * @param inventar The inventar to set.
	 */
	public void setInventar(String inventar) {
		this.inventar = inventar;
	}
	/**
	 * @return Returns the kaulpluseLiik.
	 */
	public String getKaupluseLiik() {
		return kaupluseLiik;
	}
	/**
	 * @param kaulpluseLiik The kaulpluseLiik to set.
	 */
	public void setKaupluseLiik(String kaulpluseLiik) {
		this.kaupluseLiik = kaulpluseLiik;
	}
	/**
	 * @return Returns the kliendiProfiil.
	 */
	public String getKliendiProfiil() {
		return kliendiProfiil;
	}
	/**
	 * @param kliendiProfiil The kliendiProfiil to set.
	 */
	public void setKliendiProfiil(String kliendiProfiil) {
		this.kliendiProfiil = kliendiProfiil;
	}
	/**
	 * @return Returns the kylastusPaev.
	 */
	public String getKylastusPaev() {
		return kylastusPaev;
	}
	/**
	 * @param kylastusPaev The kylastusPaev to set.
	 */
	public void setKylastusPaev(String kylastusPaev) {
		this.kylastusPaev = kylastusPaev;
	}
	/**
	 * @return Returns the lahtiolek.
	 */
	public String getLahtiolek() {
		return lahtiolek;
	}
	/**
	 * @param lahtiolek The lahtiolek to set.
	 */
	public void setLahtiolek(String lahtiolek) {
		this.lahtiolek = lahtiolek;
	}
	/**
	 * @return Returns the myygiregioon.
	 */
	public String getMyygiregioon() {
		return myygiregioon;
	}
	/**
	 * @param myygiregioon The myygiregioon to set.
	 */
	public void setMyygiregioon(String myygiregioon) {
		this.myygiregioon = myygiregioon;
	}
	/**
	 * @return Returns the myykPerioodis.
	 */
	public String getMyykPerioodis() {
		return myykPerioodis;
	}
	/**
	 * @param myykPerioodis The myykPerioodis to set.
	 */
	public void setMyykPerioodis(String myykPerioodis) {
		this.myykPerioodis = myykPerioodis;
	}
	/**
	 * @return Returns the otsustusvoime.
	 */
	public String getOtsustusvoime() {
		return otsustusvoime;
	}
	/**
	 * @param otsustusvoime The otsustusvoime to set.
	 */
	public void setOtsustusvoime(String otsustusvoime) {
		this.otsustusvoime = otsustusvoime;
	}
	/**
	 * @return Returns the peatatus.
	 */
	public String getPeatatus() {
		return peatatus;
	}
	/**
	 * @param peatatus The peatatus to set.
	 */
	public void setPeatatus(String peatatus) {
		this.peatatus = peatatus;
	}
	/**
	 * @return Returns the vastutajaEmail.
	 */
	public String getVastutajaEmail() {
		return vastutajaEmail;
	}
	/**
	 * @param vastutajaEmail The vastutajaEmail to set.
	 */
	public void setVastutajaEmail(String vastutajaEmail) {
		this.vastutajaEmail = vastutajaEmail;
	}
	/**
	 * @return Returns the vastutajaJob.
	 */
	public String getVastutajaJob() {
		return vastutajaJob;
	}
	/**
	 * @param vastutajaJob The vastutajaJob to set.
	 */
	public void setVastutajaJob(String vastutajaJob) {
		this.vastutajaJob = vastutajaJob;
	}
	/**
	 * @return Returns the vastutajaPerson.
	 */
	public String getVastutajaPerson() {
		return vastutajaPerson;
	}
	/**
	 * @param vastutajaPerson The vastutajaPerson to set.
	 */
	public void setVastutajaPerson(String vastutajaPerson) {
		this.vastutajaPerson = vastutajaPerson;
	}
	/**
	 * @return Returns the vastutajaPhone.
	 */
	public String getVastutajaPhone() {
		return vastutajaPhone;
	}
	/**
	 * @param vastutajaPhone The vastutajaPhone to set.
	 */
	public void setVastutajaPhone(String vastutajaPhone) {
		this.vastutajaPhone = vastutajaPhone;
	}
	/**
	 * @return Returns the vastutajaSynna.
	 */
	public Date getVastutajaSynna() {
		return vastutajaSynna;
	}
	/**
	 * @param vastutajaSynna The vastutajaSynna to set.
	 */
	public void setVastutajaSynna(long synna) {
		Date neww = new Date();
		neww.setTime(synna);
		this.vastutajaSynna = neww;	
	 }		
	/**
	 * @return Returns the grill.
	 */
	public String getGrill() {
		return grill;
	}
	/**
	 * @param grill The grill to set.
	 */
	public void setGrill(String grill) {
		this.grill = grill;
	}
	/**
	 * @return Returns the kylm.
	 */
	public String getKylm() {
		return kylm;
	}
	/**
	 * @param kylm The kylm to set.
	 */
	public void setKylm(String kylm) {
		this.kylm = kylm;
	}
	/**
	 * @return Returns the selve.
	 */
	public String getSelve() {
		return selve;
	}
	/**
	 * @param selve The selve to set.
	 */
	public void setSelve(String selve) {
		this.selve = selve;
	}
	/**
	 * @return Returns the teenindus.
	 */
	public String getTeenindus() {
		return teenindus;
	}
	/**
	 * @param teenindus The teenindus to set.
	 */
	public void setTeenindus(String teenindus) {
		this.teenindus = teenindus;
	}
	/**
	 * @param vastutajaSynna The vastutajaSynna to set.
	 */
	public void setVastutajaSynna(Date vastutajaSynna) {
		this.vastutajaSynna = vastutajaSynna;
	}
	/**
	 * @return Returns the helistPaev.
	 */
	public String getHelistPaev() {
		return helistPaev;
	}
	/**
	 * @param helistPaev The helistPaev to set.
	 */
	public void setHelistPaev(String helistPaev) {
		this.helistPaev = helistPaev;
	}
	}
