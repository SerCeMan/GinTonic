package de.jaculon.egap.nature;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.utils.IProjectUtils;


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

			try {
				boolean hasEgapNature = IProjectUtils.hasEgapNature(project);
				if (hasEgapNature) {
					IProjectUtils.removeEgapNature(project, null);
					action.setText(ADD);
				}else{
					IProjectUtils.addEgapNature(project, null);
					action.setText(REMOVE);
				}
			} catch (CoreException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {

		this.selection = selection;

		final IProject project = getProject(selection);
		if (project != null) {
			try {
				boolean hasEgapNature = IProjectUtils.hasEgapNature(project);
				if (hasEgapNature) {
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

	private static IProject getProject(final ISelection selection){
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

				if(!project.isAccessible()){
					return null;
				}

				try {
					if(!project.hasNature(JavaCore.NATURE_ID)){
						return null;
					}
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}

				return project;
			}
		}
		return null;
	}

}
