package alien.api.catalogue;

import java.util.UUID;

import alien.api.Request;
import alien.catalogue.LFNCSDUtils;
import alien.catalogue.LFN_CSD;
import alien.user.AliEnPrincipal;

/**
 *
 * @author mmmartin
 * @since November 27, 2018
 */
public class LFNCSDfromUUIDString extends Request {

	/**
	 *
	 */
	private static final long serialVersionUID = -3670065132137151045L;

	private LFN_CSD lfnc;
	private String uuid;

	/**
	 * @param user
	 * @param uuid
	 * 
	 */
	public LFNCSDfromUUIDString(final AliEnPrincipal user, final String uuid) {
		setRequestUser(user);
		this.uuid = uuid;
		this.lfnc = null;
	}

	@Override
	public void run() {
		try {
			UUID id = UUID.fromString(uuid);
			this.lfnc = LFNCSDUtils.guid2lfn(id);
		} catch (@SuppressWarnings("unused") Exception e) {
			this.lfnc = null;
		}
	}

	/**
	 * @return the requested LFNCSD
	 */
	public LFN_CSD getLFNCSD() {
		return this.lfnc;
	}

	@Override
	public String toString() {
		return "Asked for : " + this.uuid + ", reply is:\n" + this.lfnc;
	}
}
