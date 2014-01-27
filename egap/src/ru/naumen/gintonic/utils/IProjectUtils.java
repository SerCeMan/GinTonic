package ru.naumen.gintonic.utils;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import ru.naumen.gintonic.EgapIDs;

public class IProjectUtils {

    public static boolean hasJavaNature(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            throw new RuntimeException("Exception while trying hasNature", e);
        }
    }

    public static LinkedList<IFile> getFilesInProjectSkippingBinary(IProject project) throws CoreException {
        List<IPath> sourceFolders = getSourceFolders(project);
        CollectFiles fileCollector = new CollectFiles(sourceFolders);
        project.accept(fileCollector);
        return fileCollector.files;
    }

    /**
     * Returns the source packages as {@link IPath}es of the project assuming
     * the project to be a Java project.
     * 
     * @param project
     *            the project
     * @return the src folders as {@link IPath}es.
     * @throws CoreException
     */
    public static List<IPath> getSourceFolders(final IProject project) throws CoreException {
        Assert.isNotNull(project);
        final List<IPath> srcFolders = ListUtils.newArrayListWithCapacity(30);
        IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
        final IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
        for (final IPackageFragmentRoot pfr : packageFragmentRoots) {
            if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
                IPath path = pfr.getPath();
                srcFolders.add(path);
            }
        }

        return srcFolders;
    }

    /**
     * Adds the Egap nature to the projects natures.
     */
    public static void addEgapNature(IProject project, IProgressMonitor progressMonitor) throws CoreException {
        changeNature(project, progressMonitor, new IFunction<HashSet<String>>() {
            @Override
            public void apply(HashSet<String> natures) {
                natures.add(EgapIDs.NATURE);
            }
        });
    }

    private static void changeNature(IProject project, IProgressMonitor progressMonitor,
            IFunction<HashSet<String>> function) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final HashSet<String> natures = new HashSet<String>(asList(description.getNatureIds()));
        natures.addAll(asList(project.getDescription().getNatureIds()));
        function.apply(natures);
        description.setNatureIds(natures.toArray(new String[] {}));
        project.setDescription(description, progressMonitor);
    }

    public static void removeEgapNature(IProject project, IProgressMonitor progressMonitor) throws CoreException {
        changeNature(project, progressMonitor, new IFunction<HashSet<String>>() {
            @Override
            public void apply(HashSet<String> natures) {
                natures.remove(EgapIDs.NATURE);
                natures.remove(EgapIDs.NATURE_OLD);
            }
        });
    }

    public static List<IProject> getAccessibleProjectsWithEgapNature() {
        final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> projectsWithNature = ListUtils.newArrayList();

        for (IProject iProject : projects) {
            try {
                if (iProject.isAccessible() && hasEgapNature(iProject)) {
                    projectsWithNature.add(iProject);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        return projectsWithNature;
    }

    public static boolean hasEgapNature(IProject project) throws CoreException {
        return project.hasNature(EgapIDs.NATURE) || project.hasNature(EgapIDs.NATURE_OLD);
    }

    private static final class CollectFiles implements IResourceVisitor {

        private List<IPath> sourceFolders;
        private LinkedList<IFile> files = ListUtils.newLinkedList();

        private CollectFiles(List<IPath> sourceFolders) {
            super();
            this.sourceFolders = sourceFolders;
        }

        @Override
        public boolean visit(IResource resource) throws CoreException {

            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                if (IFolderUtils.isSourceFolder(folder, sourceFolders)) {
                    return true;
                }
                /*
                 * We must always descend into every folder, as otherwise
                 * folders with more segments (like maven projects with a src
                 * folder src/main/java) are not correctly scanned for files
                 * (Bug in Rev 3).
                 */
                return true;
            } else if (resource instanceof IFile) {
                files.add((IFile) resource);
            }

            return true;
        }
    }

}
