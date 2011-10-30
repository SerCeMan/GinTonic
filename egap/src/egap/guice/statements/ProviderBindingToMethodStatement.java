package egap.guice.statements;

import egap.guice.GuiceModule;
import egap.guice.annotations.GuiceAnnotation;

/**
 * 
 * <h5>Example using provider method:</h5>
 * 
 * <pre>
 * <code>
 * @Inject
 * private Provider<Customer> customerProvider;
 * 	
 * @SuppressWarnings("unused")
 * @Provides
 * private Customer provideCustomer() {
 * 	...
 * }
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class ProviderBindingToMethodStatement extends BindingStatement{

	private static final long serialVersionUID = -8535727988045135538L;
	
}
