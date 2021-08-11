package eevaeksport.util;

import java.io.Serializable;


public class MyJSON  implements Serializable {
	private static final long serialVersionUID = 8697996896683644760L;
private String json = null;
public String getJson() {
	return json;
}
public void setJson(String json) {
	this.json = json;
}
public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
private String url = null;
private boolean sendfile = false;
public boolean isSendfile() {
	return sendfile;
}
public void setSendfile(boolean sendfile) {
	this.sendfile = sendfile;
}

}
