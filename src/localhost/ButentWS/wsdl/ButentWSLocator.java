/**
 * ButentWSLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package localhost.ButentWS.wsdl;

public class ButentWSLocator extends org.apache.axis.client.Service implements localhost.ButentWS.wsdl.ButentWS {

    public ButentWSLocator() {
    }


    public ButentWSLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ButentWSLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ButentWebServiceSoapPort
    private java.lang.String ButentWebServiceSoapPort_address = "http://10.10.10.201:8080/ButentWS/ButentWS.WSDL";

    public java.lang.String getButentWebServiceSoapPortAddress() {
        return ButentWebServiceSoapPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ButentWebServiceSoapPortWSDDServiceName = "ButentWebServiceSoapPort";

    public java.lang.String getButentWebServiceSoapPortWSDDServiceName() {
        return ButentWebServiceSoapPortWSDDServiceName;
    }

    public void setButentWebServiceSoapPortWSDDServiceName(java.lang.String name) {
        ButentWebServiceSoapPortWSDDServiceName = name;
    }

    public localhost.ButentWS.wsdl.ButentWebServiceSoapPort getButentWebServiceSoapPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ButentWebServiceSoapPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getButentWebServiceSoapPort(endpoint);
    }

    public localhost.ButentWS.wsdl.ButentWebServiceSoapPort getButentWebServiceSoapPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            localhost.ButentWS.wsdl.ButentWebServiceSoapBindingStub _stub = new localhost.ButentWS.wsdl.ButentWebServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getButentWebServiceSoapPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setButentWebServiceSoapPortEndpointAddress(java.lang.String address) {
        ButentWebServiceSoapPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (localhost.ButentWS.wsdl.ButentWebServiceSoapPort.class.isAssignableFrom(serviceEndpointInterface)) {
                localhost.ButentWS.wsdl.ButentWebServiceSoapBindingStub _stub = new localhost.ButentWS.wsdl.ButentWebServiceSoapBindingStub(new java.net.URL(ButentWebServiceSoapPort_address), this);
                _stub.setPortName(getButentWebServiceSoapPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ButentWebServiceSoapPort".equals(inputPortName)) {
            return getButentWebServiceSoapPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost/ButentWS/wsdl/", "ButentWS");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://localhost/ButentWS/wsdl/", "ButentWebServiceSoapPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ButentWebServiceSoapPort".equals(portName)) {
            setButentWebServiceSoapPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
