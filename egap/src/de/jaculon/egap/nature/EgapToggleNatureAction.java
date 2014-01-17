package de.jaculon.egap.nature;

import static de.jaculon.egap.utils.IProjectUtils.hasEgapNature;
import static de.jaculon.egap.utils.IProjectUtils.hasJavaNature;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
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

	@Override
	public void run(IAction action) {

		IProject project = getProject(selection);
		if (project != null) {

			try {
				if (hasEgapNature(project)) {
					IProjectUtils.removeEgapNature(project, null);
					action.setText(ADD);
				} else {
					IProjectUtils.addEgapNature(project, null);
					action.setText(REMOVE);
				}
			} catch (CoreException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		this.selection = selection;

		IProject project = getProject(selection);
		if (project != null) {
			try {
				if (hasEgapNature(project)) {
					action.setText(REMOVE);
				} else {
					action.setText(ADD);
				}
			} catch (CoreException e) {
				EgapPlugin.logException(e);
			}
		}

	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private static IProject getProject(ISelection selection){
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}

				if(project == null || !project.isAccessible() || !hasJavaNature(project)){
					return null;
				}
				return project;
			}
		}
		return null;
	}

}
