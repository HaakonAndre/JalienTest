package alien.api.catalogue;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import alien.api.Request;
import alien.catalogue.Register;
import alien.config.ConfigUtils;
import alien.site.OutputEntry;
import alien.user.AliEnPrincipal;

/**
 * @author yuw
 *
 */
public class RegisterEntry extends Request {
	/**
	 * Logger
	 */
	static transient final Logger logger = ConfigUtils.getLogger(RegisterEntry.class.getCanonicalName());
	
	private static final long serialVersionUID = -2004904530203513524L;
	private OutputEntry entry;
	private String outputDir;

	/**
	 * @param entry lfn entry to register (converted to OutputEntry)
	 * @param outputDir absolute path
	 * @param user
	 */
	public RegisterEntry(final OutputEntry entry, final String outputDir, final AliEnPrincipal user) {
		setRequestUser(user);
		this.entry = entry;
		this.outputDir = outputDir;
	}

	@Override
	public void run() {
		if (entry != null && outputDir != null && outputDir.length() != 0) {
			try {
				Register.register(entry, outputDir, getEffectiveRequester());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not register entry " + entry.getName());
				e.printStackTrace();
			}
		} else {
			logger.log(Level.SEVERE, "Invalid arguments in RegisterLFN");
		}
	}
}
