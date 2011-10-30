package egap.command;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.google.common.collect.Iterators;

import egap.guice.GuiceIndex;
import egap.guice.annotations.GuiceAnnotation;
import egap.utils.EditorUtils;
import egap.utils.GuiceTypeInfo;
import egap.utils.GuiceTypeWithAnnotation;
import egap.utils.IProjectResource;
import egap.utils.IProjectResourceUtils;
import egap.utils.ITypeBindingUtils;

public class CycleBindingsHandler extends AbstractHandler {
	
	private Iterator<? extends IProjectResource> iterator;
	
	private void gotoCurrentBindingStatement(){
		if(iterator.hasNext()){
			IProjectResource navigationEndpoint = iterator.next();
			IProjectResourceUtils.openEditorWithStatementDeclaration(navigationEndpoint);
		}
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		GuiceTypeInfo guiceTypeInfo = EditorUtils.getGuiceTypeInfoOfSelectedFieldInActiveEditor();
		
		if(guiceTypeInfo == null){
			if(iterator != null){
				gotoCurrentBindingStatement();
			}
			return null;
		}
		
//		List<? extends IProjectResource> navigationCycle = null;
		List navigationCycle = null;
		ITypeBinding typeBinding = guiceTypeInfo.getTargetTypeBinding();
		GuiceIndex guiceIndex = GuiceIndex.get();
		ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);
		if (guiceTypeInfo instanceof GuiceTypeWithAnnotation) {
			GuiceTypeWithAnnotation annotatedThing = (GuiceTypeWithAnnotation) guiceTypeInfo;
			GuiceAnnotation guiceAnnotation = annotatedThing.getGuiceAnnotation();
			navigationCycle =  guiceIndex.getBindingsByTypeAndAnnotation(
					typeBindingWithoutProvider,
					guiceAnnotation);
		}else{
			/* We only have the type. */
			navigationCycle = guiceIndex.getBindingsByType(
					typeBindingWithoutProvider);
		}
		
		IProjectResource origin = guiceTypeInfo.getOrigin();
		navigationCycle.add(origin);
		
		iterator = Iterators.cycle(navigationCycle);
		gotoCurrentBindingStatement();

		return null;
	}

}
