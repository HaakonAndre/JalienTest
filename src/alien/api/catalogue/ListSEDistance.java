package alien.api.catalogue;

import alien.api.Request;
import alien.catalogue.LFN;
import alien.catalogue.LFNUtils;
import alien.catalogue.PFN;
import alien.config.ConfigUtils;
import alien.se.SE;
import alien.se.SEUtils;
import alien.user.AliEnPrincipal;

import java.util.ArrayList;
import java.util.List;


/**
 * Get the LFN object for this path
 * 
 * @author costing
 * @since 2011-03-04
 */
public class ListSEDistance extends Request {	
	private static final long serialVersionUID = 726995834931008148L;
	private LFN lfn;
	private boolean write;
	private String site;
	private List<SE> ses;
	
	/**
	 * @param user 
	 * @param role 
	 * @param path
	 * @param evenIfDoesNotExist
	 */
	public ListSEDistance(final AliEnPrincipal user, 
							final String role,
							final String site,
							final boolean write, 
							final String lfn){
		setRequestUser(user);
		setRoleRequest(role);
		if( lfn!=null && lfn.length()!=0 )
			this.lfn = LFNUtils.getLFN(lfn);
		this.write = write;		
		if(site==null || site.length()==0)
			this.site = ConfigUtils.getConfig().gets("alice_close_site", "CERN").trim();
		else
			this.site = site; 
	}
	
	@Override
	public void run() {
		// this is for write
		if( this.write ){
			this.ses = SEUtils.getClosestSEs(this.site, this.write);
			return;
		}
				
		//for read with lfn specified
		List<PFN> lp = SEUtils.sortBySite( lfn.whereis(), site, true, false);
		this.ses = new ArrayList<SE>();
		for( PFN p : lp ){
			this.ses.add( p.getSE() );
		}
	}
	
	/**
	 * @return the requested LFN
	 */
	public List<SE> getSE(){
		return this.ses;
	}		
}
