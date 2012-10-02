package egap.utils;

/**
 * This enum holds the information from which of the three possible places
 * FIELD, SETTER or CONSTRUCTOR the injection information is attached to.
 * 
 * @author tmajunke
 */
public enum InjectionIsAttachedTo {

	/**
	 * The field declaration is annotated with @Inject.
	 * 
	 * <pre>
	 * @Inject
	 * @MySpecialService
	 * private Service service;
	 * 
	 * <pre>
	 */
	FIELD,

	/**
	 * The setters are annotated with @Inject.
	 * 
	 * <pre>
	 * private Service service;
	 * 
	 * @Inject
	 * public void setService(@MySpecialService Service service){
	 *  this.service = service;
	 * }
	 * 
	 * <pre>
	 */
	SETTER,

	/**
	 * The constructor is annotated with @Inject.
	 * 
	 * <pre>
	 * class ServiceUser{
	 * 	private Service service;
	 * 
	 * 	public ServiceUser(@MySpecialService Service service){
	 *  	this.service = service;
	 * 	}
	 * }
	 * 
	 * <pre>
	 */
	CONSTRUCTOR

}
