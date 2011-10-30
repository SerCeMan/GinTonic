package egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class GuiceTypeInfo {
	private final ITypeBinding targetTypeBinding;
	private final IProjectResource origin;

	public GuiceTypeInfo(IProjectResource origin,
			ITypeBinding targetTypeBinding) {
		super();
		this.origin = origin;
		this.targetTypeBinding = targetTypeBinding;
	}
	
	public IProjectResource getOrigin() {
		return origin;
	}

	public ITypeBinding getTargetTypeBinding() {
		return targetTypeBinding;
	}

}