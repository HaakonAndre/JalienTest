package alien.api;

import lazyj.cache.ExpirationCache;

import java.lang.ref.WeakReference;

import alien.config.ConfigUtils;

/**
 * @author costing
 * @since 2011-03-04
 */
public class Dispatcher {

	private static final boolean useParallelConnections = false;

	private static final ExpirationCache<String, WeakReference<Request>> cache = new ExpirationCache<>(10240);

	/**
	 * @param r
	 *            request to execute
	 * @return the processed request
	 * @throws ServerException
	 *             exception thrown by the processing
	 */
	public static <T extends Request> T execute(final T r) throws ServerException {
		return execute(r, false);
	}

	/**
	 * @param r
	 *            request to execute
	 * @param forceRemote
	 *            request to force remote execution
	 * @return the processed request
	 * @throws ServerException
	 *             exception thrown by the processing
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Request> T execute(final T r, final boolean forceRemote) throws ServerException {
		if (ConfigUtils.isCentralService() && !forceRemote) {
			// System.out.println("Running centrally: " + r.toString());
			r.authorizeUserAndRole();
			r.run();
			return r;
		}

		if (r instanceof Cacheable) {
			final Cacheable c = (Cacheable) r;

			final String key = r.getClass().getCanonicalName() + "#" + c.getKey();

			final WeakReference<Request> cachedObject = cache.get(key);

			final Object cachedValue;

			if (cachedObject != null && (cachedValue = cachedObject.get()) != null) {
				return (T) cachedValue;
			}

			final T ret = dispatchRequest(r);

			if (ret != null)
				cache.put(key, new WeakReference<Request>(ret), ((Cacheable) ret).getTimeout());

			return ret;
		}

		return dispatchRequest(r);
	}

	private static <T extends Request> T dispatchRequest(final T r) throws ServerException {

		// return DispatchSSLClient.dispatchRequest(r);
		return useParallelConnections ? DispatchSSLMTClient.dispatchRequest(r) : DispatchSSLClient.dispatchRequest(r);
	}

}
