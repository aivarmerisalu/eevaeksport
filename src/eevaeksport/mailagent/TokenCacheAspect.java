package eevaeksport.mailagent;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.aad.msal4j.ITokenCacheAccessContext;


public class TokenCacheAspect {

	  private String data;

	    public TokenCacheAspect(String fileName) {
	        this.data = readDataFromFile(fileName);
	    }

	    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
	        iTokenCacheAccessContext.tokenCache().deserialize(data);
	    }

	
	    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
	        data = iTokenCacheAccessContext.tokenCache().serialize();
	        // you could implement logic here to write changes to file
	    }

	    private  String readDataFromFile(String resource) {
	        try {
	            //Determine if sample running from IDE (resource URI starts with 'file') or from a .jar (resource URI starts with 'jar'),
	            //  so that sample_cache.json is read properly
	            if (TokenCacheAspect.class.getResource("TokenCacheAspect.class").toString().startsWith("file")) {
	                URL path = TokenCacheAspect.class.getResource(resource);
	                return new String(
	                        Files.readAllBytes(
	                                Paths.get(path.toURI())));
	            }
	            else {
	                URI uri = TokenCacheAspect.class.getResource(resource).toURI();
	                Map<String, String> env = new HashMap<>();
	                env.put("create", "true");
	                FileSystem fs = FileSystems.newFileSystem(uri, env);
	                Path myFolderPath = Paths.get(uri);

	                return new String(Files.readAllBytes(myFolderPath));
	            }
	        } catch (Exception ex){
	            System.out.println("Error reading data from file: " + ex.toString());
	           throw new RuntimeException(ex);
	           
	        }
	    }
	

}
