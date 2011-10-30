package egap.guice.statements;

import egap.guice.GuiceModule;
import egap.guice.annotations.GuiceAnnotation;

/**
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * MapBinder&lt;Class&lt;? extends Customer&gt;, IBarkeeper&gt; mapBinder = MapBinder.newMapBinder(
 * binder(),
 * new TypeLiteral&lt;Class&lt;? extends Customer&gt;&gt;() {},
 * new TypeLiteral&lt;IBarkeeper&gt;() {} );
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class MapBinderCreateStatement extends BindingStatement {

	private static final long serialVersionUID = 4460613372835297220L;

}
