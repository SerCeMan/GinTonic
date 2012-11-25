package de.jaculon.egap.project_builder;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.BuildState;
import de.jaculon.egap.guice.GuiceIndex;
import de.jaculon.egap.guice.GuiceIndexSerializer;
import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.guice.analyzer.GuiceAnalyzer;
import de.jaculon.egap.utils.DateUtils;
import de.jaculon.egap.utils.IPathUtils;
import de.jaculon.egap.utils.IProjectUtils;


/**
 * The {@link EgapProjectBuilder} creates and updates the the {@link GuiceIndex}.
 *
 * @author tmajunke
 */
public class EgapProjectBuilder extends IncrementalProjectBuilder {

	private static GuiceAnalyzer guiceIndexer = new GuiceAnalyzer();

	public EgapProjectBuilder() {
		super();
	}

	@Override
	protected IProject[] build(int kind,
			@SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();

		switch (kind) {
		case IncrementalProjectBuilder.FULL_BUILD:
			fullBuild(project, monitor);
			break;
		case IncrementalProjectBuilder.AUTO_BUILD:
			autoBuild(project, monitor);
			break;
		case IncrementalProjectBuilder.CLEAN_BUILD:
			cleanBuild();
			break;
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
			incrementalBuild(project, monitor);
			break;
		default:
			throw new RuntimeException("Unknown build type " + kind + "");
		}

		return null;
	}

	private void autoBuild(IProject project, IProgressMonitor monitor)
			throws CoreException {
		incrementalBuild(project, monitor);
	}

	private void cleanBuild() {
		EgapPlugin.logInfo("Clean build triggered!");
		GuiceIndex.rebuild();
		GuiceIndexSerializer.clear();
	}

	private void incrementalBuild(IProject project, IProgressMonitor monitor)
			throws CoreException {
		IResourceDelta delta = getDelta(project);
		delta.accept(new IndexGuiceModulesDeltaBuild(project, monitor));
	}

	private void fullBuild(IProject project, IProgressMonitor monitor)
			throws CoreException {
		/* Reset the identified guice modules on full build. */
		monitor.subTask("Indexing project " + project.getName() + ".");

		long now = System.currentTimeMillis();

		GuiceIndex guiceIndex = GuiceIndex.get();
		guiceIndex.removeGuiceModulesByProjectName(project.getName());

		int nrOfGuiceModulesBefore = guiceIndex.getNrOfGuiceModules();

		List<IFile> files = IProjectUtils.getFilesInProjectSkippingBinary(project);
		for (IFile iFile : files) {
			if (monitor.isCanceled()) {
				guiceIndex.setBuildState(BuildState.BUILD_INCOMPLETE);
				EgapPlugin.logWarning("Indexing Guice modules canceled by user request leaving the index in an incomplete state.");
				break;
			}
			GuiceModule guiceModule = guiceIndexer.index(project, iFile);
			if (guiceModule != null) {
				guiceIndex.addGuiceModuleDontLog(guiceModule);
			}
		}

		if (guiceIndex.getBuildState() != BuildState.BUILD_INCOMPLETE) {
			guiceIndex.setBuildState(BuildState.BUILD_COMPLETE);
		}

		long then = System.currentTimeMillis();
		long elapsed = then - now;
		int nrOfGuiceModulesAdded = guiceIndex.getNrOfGuiceModules()
				- nrOfGuiceModulesBefore;
		String message = "Indexing " + project.getName() + " finished in "
				+ DateUtils.formatMilliseconds(elapsed) + " (+" + nrOfGuiceModulesAdded + " modules => "
				+ guiceIndex.getNrOfGuiceModules() + " modules total).";

		EgapPlugin.logInfo(message);

		monitor.done();
	}

	static class IndexGuiceModulesDeltaBuild implements IResourceDeltaVisitor {

		private IProject project;
		private final IProgressMonitor monitor;

		private IndexGuiceModulesDeltaBuild(IProject project,
				IProgressMonitor monitor) {
			super();
			this.project = project;
			this.monitor = monitor;
		}

		private static boolean VISIT_CHILDREN = true;

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			int resourceType = resource.getType();

			/*
			 * We only process file changes... Wait! What if someone deletes a
			 * folder? No problem as we also get a notification for every
			 * deleted file.
			 */
			if (resourceType != IResource.FILE) {
				return VISIT_CHILDREN;
			}
			IFile file = (IFile) resource;
			IJavaElement javaElement = JavaCore.create(file);
			if (javaElement == null) {
				return VISIT_CHILDREN;
			}

			int elementType = javaElement.getElementType();
			if (elementType == IJavaElement.COMPILATION_UNIT) {
				int kind = delta.getKind();

				GuiceIndex guiceIndex = GuiceIndex.get();
				GuiceModule guiceModule;

				switch (kind) {
				case IResourceDelta.ADDED:
					guiceModule = guiceIndexer.index(
							project,
							(IFile) resource);
					if (guiceModule != null) {
						monitor.subTask("Added Guice module "
								+ guiceModule.getTypeNameFullyQualified()
								+ " to index.");
						guiceIndex.addGuiceModule(guiceModule, true);
					}
					break;
				case IResourceDelta.REMOVED:
					removeModuleFromIndex(delta, guiceIndex);
					break;
				case IResourceDelta.CHANGED:
					/*
					 * We have to check the module as maybe it's contents have
					 * significant changes (e.g removed an install(Module)
					 * statement).
					 */
					guiceModule = guiceIndexer.index(
							project,
							(IFile) resource);
					if (guiceModule != null) {
						monitor.subTask("Updated Guice module "
								+ guiceModule.getTypeNameFullyQualified() + ".");
						guiceIndex.updateGuiceModule(guiceModule);
					}
					else {
						/*
						 * Maybe we could not analyze the module then we must
						 * remove it from the index. Otherwise we work on stale
						 * data.
						 */
						removeModuleFromIndex(delta, guiceIndex);
					}
					break;
				}
			}

			return VISIT_CHILDREN;
		}

		public void removeModuleFromIndex(final IResourceDelta delta,
				GuiceIndex guiceIndex) {
			IPath projectRelativePath = delta.getProjectRelativePath();
			String javaClasspathString = IPathUtils.getRelativePathToJavaClasspathString(projectRelativePath);
			guiceIndex.removeGuiceModule(javaClasspathString, true);
			monitor.subTask("Removed Guice module " + javaClasspathString
					+ " from index.");
		}
	}

}
