package alien.catalogue.access;

import java.util.Set;

import alien.catalogue.CatalogEntity;



/**
 * @author ron
 *
 */
public class CatalogueAccessDENIED extends CatalogueAccess{
	
	
	private String name;
	
	/**
	 * Delete access to the catalogue object indicated by this GUID.
	 * This constructor is package protected, the objects should be created only by {@link AuthorizationFactory}
	 * @param entity 
	 */
	CatalogueAccessDENIED(CatalogEntity entity){
		super(null);
		name = entity.getName();
		super.access = INVALID;
	}
	CatalogueAccessDENIED(String name){
		super(null);
		this.name = name;
		super.access = INVALID;
	}


	public void addEnvelope(final XrootDEnvelope envelope){
	}
	
	public Set<XrootDEnvelope> getEnvelopes(){
		return null;
	}
}
