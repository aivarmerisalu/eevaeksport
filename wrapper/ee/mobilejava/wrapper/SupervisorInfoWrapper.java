/*
 * Created on 8.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ee.mobilejava.wrapper;

import ee.merisoft.sm.SupervisorInfo;

/**
 * @author ttubro
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SupervisorInfoWrapper {
	private SupervisorInfo info = null;
	
	public SupervisorInfoWrapper(Object o){
		this.info = (SupervisorInfo)o;
	}
	
	public SupervisorInfo getInfo(){
		return info;
	}
}
