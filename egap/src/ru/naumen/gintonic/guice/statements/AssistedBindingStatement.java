package ru.naumen.gintonic.guice.statements;

/**
 * 
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bind(RealPaymentFactory.class).toProvider(
 * 	FactoryProvider.newFactory(
 * 	RealPaymentFactory.class,
 * 	RealPayment.class));
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class AssistedBindingStatement extends BindingDefinition {

	private static final long serialVersionUID = -1246112508071550296L;
	
	private String modelTypeName;

	public void setModelTypeName(String modelTypeName) {
		this.modelTypeName = modelTypeName;
	}

	public String getModelTypeName() {
		return modelTypeName;
	}

}
