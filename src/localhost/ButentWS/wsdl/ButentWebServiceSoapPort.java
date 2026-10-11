package localhost.ButentWS.wsdl;

public interface ButentWebServiceSoapPort extends java.rmi.Remote {
    public java.lang.String login(java.lang.String usr, java.lang.String pwd) throws java.rmi.RemoteException;
    public java.lang.String process(java.lang.String mthd, java.lang.String prm) throws java.rmi.RemoteException;
    public java.lang.String logout() throws java.rmi.RemoteException;
}
