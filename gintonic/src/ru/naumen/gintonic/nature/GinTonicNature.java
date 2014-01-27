package ru.naumen.gintonic.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import ru.naumen.gintonic.GinTonicIDs;
import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.guice.GuiceIndex;


public class GinTonicNature implements IProjectNature {

	private IProject project;

	public GinTonicNature() {
		super();
	}

	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(GinTonicIDs.BUILDER)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(GinTonicIDs.BUILDER);
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
			GinTonicPlugin.logInfo("Removed " + nrOfGuiceModulesRemoved
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
