package alien.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import alien.config.ConfigUtils;
import alien.user.JAKeyStore;

/**
 * @author costing
 *
 */
public class DispatchSSLClient extends Thread {

	/**
	 * Reset the object stream every this many objects sent
	 */
	private static final int RESET_OBJECT_STREAM_COUNTER = 1000;

	private int objectsSentCounter = 0;

	/**
	 * Logger
	 */
	static transient final Logger logger = ConfigUtils.getLogger(DispatchSSLClient.class.getCanonicalName());

	private static final int defaultPort = 5282;
	private static final String defaultHost = "localhost";
	private static String serviceName = "apiService";

	private static String addr = null;
	private static int port = 0;

	private final Socket connection;

	private final ObjectInputStream ois;
	private final ObjectOutputStream oos;

	private final OutputStream os;

	/**
	 * E.g. the CE proxy should act as a fowarding bridge between JA and central services
	 *
	 * @param servName
	 *            name of the config parameter for the host:port settings
	 */
	public static void overWriteServiceAndForward(final String servName) {
		// TODO: we could drop the serviceName overwrite, once we assume to run
		// not on one single host everything
		serviceName = servName;
	}

	/**
	 * @param connection
	 * @throws IOException
	 */
	protected DispatchSSLClient(final Socket connection) throws IOException {

		this.connection = connection;

		connection.setTcpNoDelay(true);
		connection.setTrafficClass(0x10);

		this.ois = new ObjectInputStream(connection.getInputStream());

		this.os = connection.getOutputStream();

		this.oos = new ObjectOutputStream(this.os);
		this.oos.flush();
	}

	@Override
	public String toString() {
		return this.connection.getInetAddress().toString();
	}

	@Override
	public void run() {
		// check
	}

	private static HashMap<Integer, DispatchSSLClient> instance = new HashMap<>(20);

	/**
	 * @param address
	 * @param p
	 * @return instance
	 * @throws IOException
	 */
	public static DispatchSSLClient getInstance(final String address, final int p) throws IOException {

		if (instance.get(Integer.valueOf(p)) == null) {
			// connect to the other end
			logger.log(Level.INFO, "Connecting to JCentral on " + address + ":" + p);
			System.out.println("Connecting to JCentral on " + address + ":" + p);

			Security.addProvider(new BouncyCastleProvider());

			try {
				// get factory
				final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");

				logger.log(Level.INFO, "Connecting with client cert: " + ((java.security.cert.X509Certificate) JAKeyStore.getKeyStore().getCertificateChain("User.cert")[0]).getSubjectDN());

				// initialize factory, with clientCert(incl. priv+pub)
				kmf.init(JAKeyStore.getKeyStore(), JAKeyStore.pass);

				java.lang.System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");
				final SSLContext ssc = SSLContext.getInstance("TLS");

				// initialize SSL with certificate and the trusted CA and pub
				// certs
				ssc.init(kmf.getKeyManagers(), JAKeyStore.trusts, new SecureRandom());

				final SSLSocketFactory f = ssc.getSocketFactory();

				@SuppressWarnings("resource")
				// this object is kept in the map, cannot be closed here
				final SSLSocket client = (SSLSocket) f.createSocket(address, p);

				// print info
				printSocketInfo(client);

				client.startHandshake();

				final X509Certificate[] peerCerts = client.getSession().getPeerCertificateChain();

				if (peerCerts != null) {

					logger.log(Level.INFO, "Printing peer's information:");

					for (final X509Certificate peerCert : peerCerts)
						logger.log(Level.INFO, "Peer's Certificate Information:\n" + Level.INFO, "- Subject: " + peerCert.getSubjectDN().getName() + "\n" + peerCert.getIssuerDN().getName() + "\n"
								+ Level.INFO + "- Start Time: " + peerCert.getNotBefore().toString() + "\n" + Level.INFO + "- End Time: " + peerCert.getNotAfter().toString());

					final DispatchSSLClient sc = new DispatchSSLClient(client);
					System.out.println("Connection to JCentral established.");
					instance.put(Integer.valueOf(p), sc);
				}
				else
					logger.log(Level.SEVERE, "We didn't get any peer/service cert. NOT GOOD!");

			} catch (final ConnectException e) {
				logger.log(Level.SEVERE, "Could not connect to JCentral: [" + e.getMessage() + "].");
				System.err.println("Could not connect to JCentral: [" + e.getMessage() + "].");
			} catch (final Throwable e) {
				logger.log(Level.SEVERE, "Could not initiate SSL connection to the server.", e);
				// e.printStackTrace();
				System.err.println("Could not initiate SSL connection to the server.");
			}

		}

		return instance.get(Integer.valueOf(p));
	}

	@SuppressWarnings("unused")
	private void close() {
		if (ois != null)
			try {
				ois.close();
			} catch (final IOException ioe) {
				// ignore
			}

		if (oos != null)
			try {
				oos.close();
			} catch (final IOException ioe) {
				// ignore
			}

		if (connection != null)
			try {
				connection.close();
			} catch (final IOException ioe) {
				// ignore
			}

		instance = null;
	}

	/**
	 * Total amount of time (in milliseconds) spent in writing objects to the socket.
	 */
	public static long lSerialization = 0;

	private static synchronized void initializeSocketInfo() {
		addr = ConfigUtils.getConfig().gets(serviceName).trim();

		if (addr.length() == 0) {
			addr = defaultHost;
			port = defaultPort;
		}
		else {

			final String address = addr;
			final int idx = address.indexOf(':');

			if (idx >= 0)
				try {
					port = Integer.parseInt(address.substring(idx + 1));
					addr = address.substring(0, idx);
				} catch (@SuppressWarnings("unused") final Exception e) {
					addr = defaultHost;
					port = defaultPort;
				}
		}
	}

	/**
	 * @param r
	 * @return the reply, or <code>null</code> in case of connectivity problems
	 * @throws ServerException
	 */
	public static synchronized <T extends Request> T dispatchRequest(final T r) throws ServerException {
		initializeSocketInfo();
		try {
			return dispatchARequest(r);
		} catch (@SuppressWarnings("unused") final IOException e) {
			// Now let's try, if we can reconnect
			instance.put(Integer.valueOf(port), null);
			try {
				return dispatchARequest(r);
			} catch (final IOException e1) {
				// This time we give up
				logger.log(Level.SEVERE, "Error running request, potential connection error.", e1);
				return null;
			}
		}

	}

	/**
	 * @param r
	 * @return the processed request, if successful
	 * @throws IOException
	 *             in case of connectivity problems
	 * @throws ServerException
	 *             if the server didn't like the request content
	 */
	public static synchronized <T extends Request> T dispatchARequest(final T r) throws IOException, ServerException {

		final DispatchSSLClient c = getInstance(addr, port);

		if (c == null)
			throw new IOException("Connection is null");

		final long lStart = System.currentTimeMillis();

		c.oos.writeObject(r);

		if (++c.objectsSentCounter >= RESET_OBJECT_STREAM_COUNTER) {
			c.oos.reset();
			c.objectsSentCounter = 0;
		}

		c.oos.flush();
		c.os.flush();

		lSerialization += System.currentTimeMillis() - lStart;

		Object o;
		try {
			o = c.ois.readObject();
		} catch (final ClassNotFoundException | IOException e) {
			throw new IOException(e.getMessage());
		}

		if (o == null) {
			logger.log(Level.WARNING, "Read a null object as reply to this request: " + r);
			return null;
		}

		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Got back an object of type " + o.getClass().getCanonicalName() + " : " + o);

		@SuppressWarnings("unchecked")
		final T reply = (T) o;

		final ServerException ex = reply.getException();

		if (ex != null)
			throw ex;

		return reply;
	}

	private static void printSocketInfo(final SSLSocket s) {

		logger.log(Level.INFO, "Remote address: " + s.getInetAddress().toString() + ":" + s.getPort());
		logger.log(Level.INFO, "   Local socket address = " + s.getLocalSocketAddress().toString());

		logger.log(Level.INFO, "   Cipher suite = " + s.getSession().getCipherSuite());
		logger.log(Level.INFO, "   Protocol = " + s.getSession().getProtocol());
	}

}
