package de.jaculon.egap.utils;

import org.eclipse.jdt.core.dom.ITypeBinding;

public interface IInjectionPoint {

	ITypeBinding getTargetTypeBinding();

}