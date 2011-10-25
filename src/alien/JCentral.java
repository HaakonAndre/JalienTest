package alien;

import java.security.KeyStoreException;
import java.util.logging.Level;
import java.util.logging.Logger;

import alien.api.DispatchSSLServer;
import alien.config.ConfigUtils;
import alien.user.JAKeyStore;


/**
 * @author ron
 * @since Jun 6, 2011
 */
public class JCentral {
	/**
	 * Logger
	 */
	static transient final Logger logger = ConfigUtils.getLogger(JCentral.class
			.getCanonicalName());

	/**
	 * @param args
	 * @throws KeyStoreException 
	 */
	public static void main(String[] args) throws KeyStoreException {
		
		try {
			JAKeyStore.loadServerKeyStorage();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		logger.setLevel(Level.WARNING);

		try {
//			SimpleCatalogueApiService catalogueAPIService = new SimpleCatalogueApiService();
//			catalogueAPIService.start();
			DispatchSSLServer.runService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}