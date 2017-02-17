package alien.catalogue.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import alien.catalogue.GUID;
import alien.catalogue.PFN;
import alien.config.ConfigUtils;
import alien.io.xrootd.envelopes.XrootDEnvelopeSigner;
import alien.se.SE;
import alien.user.AliEnPrincipal;
import alien.user.AuthorizationChecker;
import alien.user.JAKeyStore;
import alien.user.UserFactory;

/**
 * @author ron
 */
public final class AuthorizationFactory {

	/**
	 * Logger
	 */
	static transient final Logger logger = ConfigUtils.getLogger(AuthorizationFactory.class.getCanonicalName());

	private static AliEnPrincipal defaultAccount = null;

	static {
		final String file = ConfigUtils.getConfig().gets("user.cert.pub.location",
				System.getProperty("user.home") + System.getProperty("file.separator") + ".globus" + System.getProperty("file.separator") + "usercert.pem");

		logger.log(Level.FINE, "Trying to use " + file);

		final File f = new File(file);

		AliEnPrincipal user = null;

		if (f.exists() && f.isFile() && f.canRead())
			if (f.getName().endsWith("der"))
				try (InputStream is = new FileInputStream(f)) {

					final CertificateFactory cf = CertificateFactory.getInstance("X.509");
					final X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
					user = UserFactory.getByCertificate(new X509Certificate[] { cert });

				} catch (final Throwable t) {
					logger.log(Level.WARNING, "Could not read from " + file, t);
				}
			else
				try {
					final X509Certificate[] certChain = JAKeyStore.loadPubX509(file);

					if (certChain != null)
						user = UserFactory.getByCertificate(certChain);
				} catch (final Throwable t) {
					logger.log(Level.WARNING, "Exception reading X509 certificate from " + file, t);
				}

		setDefaultUser(user);
	}

	/**
	 * Set the default account of this environment
	 *
	 * @param account
	 */
	private static final void setDefaultUser(final AliEnPrincipal account) {
		defaultAccount = account;
	}

	/**
	 * @return default account for
	 */
	public static final AliEnPrincipal getDefaultUser() {
		return defaultAccount;
	}

	/**
	 * Request access to all PFNs of this GUID, in the name of the default identity of this JVM
	 *
	 * @param guid
	 * @param access
	 * @return the failure reason, if any, or <code>null</code> if the access was granted
	 */
	public static String fillAccess(final GUID guid, final AccessType access) {
		if (defaultAccount == null)
			return "There is no default account set";

		return fillAccess(defaultAccount, guid, access);
	}

	/**
	 * Request access to all PFNs of this GUID
	 *
	 * @param user
	 * @param guid
	 * @param access
	 * @return the failure reason, if any, or <code>null</code> if the access was granted
	 */
	public static String fillAccess(final AliEnPrincipal user, final GUID guid, final AccessType access) {
		final Set<PFN> pfns = guid.getPFNs();

		if (pfns == null || pfns.size() == 0)
			return null;

		String reason = null;

		for (final PFN pfn : pfns) {
			final Set<PFN> realPfns = pfn.getRealPFNs();

			if (realPfns == null || realPfns.size() == 0) {
				logger.log(Level.WARNING, "No real pfns for " + pfn.pfn);
				continue;
			}

			for (final PFN realPfn : realPfns) {
				// request access to this file
				reason = AuthorizationFactory.fillAccess(user, realPfn, access);

				if (reason != null) {
					logger.log(Level.WARNING, "Cannot grant access to " + realPfn.pfn + " : " + reason);

					// we don't have access to this file
					continue;
				}

				// System.err.println("Granted access to "+realPfn.pfn);
			}
		}

		return reason;
	}

	/**
	 * Request access to this GUID, with the privileges of the default account
	 *
	 * @param pfn
	 * @param access
	 * @return <code>null</code> if access was granted, otherwise the reason why the access was rejected
	 */
	public static String fillAccess(final PFN pfn, final AccessType access) {
		if (defaultAccount == null)
			return "There is no default account set";

		return fillAccess(defaultAccount, pfn, access);
	}

	/**
	 * Request access to this GUID
	 *
	 * @param user
	 * @param pfn
	 * @param access
	 * @return <code>null</code> if access was granted, otherwise the reason why the access was rejected
	 */
	public static String fillAccess(final AliEnPrincipal user, final PFN pfn, final AccessType access) {
		return fillAccess(user, pfn, access, false);
	}

	/**
	 * Request access to this GUID
	 *
	 * @param user
	 * @param pfn
	 * @param access
	 * @param skipSanityChecks
	 *            set to <code>true</code> for manual operations that would otherwise fail since the details are not consistent in the catalogue database
	 * @return <code>null</code> if access was granted, otherwise the reason why the access was rejected
	 */
	public static String fillAccess(final AliEnPrincipal user, final PFN pfn, final AccessType access, final boolean skipSanityChecks) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, pfn + ", user: " + user + ", access: " + access);

		final GUID guid = pfn.getGuid();

		if (guid == null)
			return "GUID is null for this object";

		final Set<PFN> pfns = guid.getPFNs();

		if (access == AccessType.WRITE) {
			// PFN must not be part of the ones already registered to the GUID

			if (!AuthorizationChecker.canWrite(guid, user))
				return "User (" + user + ") is not allowed to write this entry: " + guid;

			if (pfns != null && pfns.contains(pfn))
				return "PFN already associated to the GUID";
		}
		else
			if (access == AccessType.DELETE || access == AccessType.READ) {
				// PFN must be a part of the ones registered to the GUID

				if (access == AccessType.DELETE) {
					if (!AuthorizationChecker.canWrite(guid, user))
						return "User is not allowed to delete this entry";
				}
				else
					if (!AuthorizationChecker.canRead(guid, user))
						return "User is not allowed to read this entry";

				if (!skipSanityChecks && (pfns == null || !pfns.contains(pfn)))
					return "PFN is not registered";
			}
			else
				return "Unknown access type : " + access;

		final SE referenceSE = pfn.getSE();

		final XrootDEnvelope env = new XrootDEnvelope(access, pfn);

		try {
			if (pfn.getPFN().startsWith("root://"))
				if (referenceSE == null || referenceSE.needsEncryptedEnvelope)
					// System.out.println("SE needs encrypted envelope");
					XrootDEnvelopeSigner.encryptEnvelope(env);
				else
					XrootDEnvelopeSigner.signEnvelope(env);
		} catch (final GeneralSecurityException gse) {
			logger.log(Level.SEVERE, "Cannot sign and encrypt envelope", gse);
		}

		pfn.ticket = new AccessTicket(access, env);

		return null;
	}

}