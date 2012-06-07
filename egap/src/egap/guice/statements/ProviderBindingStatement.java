package egap.guice.statements;


/**
 * 
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bind(Date.class).
 *  annotatedWith(CurrentTimeProvider.class).
 *  toProvider(CurrentTimeProviderImpl.class);
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class ProviderBindingStatement extends LinkedBindingStatement{

	private static final long serialVersionUID = -8535727988045135538L;
	
	private String providerClassType;
	
	public String getProviderClassType() {
		return providerClassType;
	}

	public void setProviderClassType(String providerClassType) {
		this.providerClassType = providerClassType;
	}

}
