package de.jaculon.egap.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.project_builder.EgapBuilder;


public class EgapNature implements IProjectNature {

	private IProject project;

	public EgapNature() {
		super();
	}

	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(EgapBuilder.ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(EgapBuilder.ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		/*
		 * Javadoc says: The nature extension id is removed from the list of
		 * natures before this method is called, and need <b>not</b> be removed
		 * here.
		 */
		GuiceIndex guiceIndex = GuiceIndex.get();

		if (guiceIndex != null) {
			int nrOfGuiceModulesBefore = guiceIndex.getNrOfGuiceModules();
			String name = project.getName();
			guiceIndex.removeGuiceModulesByProjectName(name);
			int nrOfGuiceModulesAfter = guiceIndex.getNrOfGuiceModules();
			int nrOfGuiceModulesRemoved = nrOfGuiceModulesBefore
					- nrOfGuiceModulesAfter;
			EgapPlugin.logInfo("Removed " + nrOfGuiceModulesRemoved
					+ " guice modules from index.");
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
