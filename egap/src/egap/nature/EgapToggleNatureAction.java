package egap.nature;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import egap.EgapPlugin;
import egap.utils.IProjectUtils;

public class EgapToggleNatureAction implements IObjectActionDelegate {

	private static final String ADD = "Add Egap Nature";
	private static final String REMOVE = "Remove Egap Nature";
	private ISelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction action) {
		
		final IProject project = getProject(selection);
		if (project != null) {
			boolean natureToggled = IProjectUtils.toggleNature(project, EgapNature.ID);
			if (natureToggled) {
				action.setText(REMOVE);
			} else {
				action.setText(ADD);
			}
			try {
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (final CoreException e) {
				EgapPlugin.logException(e);
			}
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		
		this.selection = selection;

		final IProject project = getProject(selection);
		if (project != null) {
			try {
				if (project.hasNature(EgapNature.ID)) {
					action.setText(REMOVE);
				} else {
					action.setText(ADD);
				}
			} catch (final CoreException e) {
				EgapPlugin.logException(e);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	private static IProject getProject(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (final Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				final Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				
				if(project == null){
					return null;
				}
				
				if(!project.isOpen()){
					return null;
				}
				
				return project;
			}
		}
		return null;
	}

}
