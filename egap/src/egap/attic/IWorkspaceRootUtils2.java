package egap.attic;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Deprecated
public class IWorkspaceRootUtils2 {
	
	public static Predicate<IProject> PROJECT_IS_OPEN_PREDICATE = new Predicate<IProject>(){
		@Override
		public boolean apply(IProject project) {
			return project.isOpen();
		}
	};
	
	public static Iterable<IProject> getOpenProjects(IWorkspaceRoot root){
		IProject[] projects = root.getProjects();
		Iterable<IProject> filter = Iterables.filter(Arrays.asList(projects), PROJECT_IS_OPEN_PREDICATE);
		return filter;
	}
	
}
