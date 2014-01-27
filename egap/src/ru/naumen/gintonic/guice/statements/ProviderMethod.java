package ru.naumen.gintonic.guice.statements;


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
public class ProviderMethod extends BindingDefinition{

	private static final long serialVersionUID = -8535727988045135538L;

}
