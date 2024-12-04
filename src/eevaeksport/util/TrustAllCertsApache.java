package eevaeksport.util;


	import org.apache.http.conn.ssl.NoopHostnameVerifier;
	import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
	import org.apache.http.conn.ssl.TrustStrategy;
	import org.apache.http.impl.client.CloseableHttpClient;
	import org.apache.http.impl.client.HttpClients;
	import org.apache.http.ssl.SSLContextBuilder;

	import javax.net.ssl.SSLContext;
	import java.security.cert.X509Certificate;

	public class TrustAllCertsApache {
		 public static CloseableHttpClient createHttpClient_AcceptsUntrustedCerts() throws Exception {
		 // Create a trust strategy that accepts all certificates
		 TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		 // Create an SSL context with our trust strategy
		 SSLContext sslContext = SSLContextBuilder.create()
		 .loadTrustMaterial(null, acceptingTrustStrategy)
		 .build();

		 // Create an SSL socket factory with our SSL context
		 
		 SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		 // Create an HttpClient with the SSL socket factory
		 return HttpClients.custom()
		 .setSSLSocketFactory(sslsf)
		 .build();
		 }
		 }
