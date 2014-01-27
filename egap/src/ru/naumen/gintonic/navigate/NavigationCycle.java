package ru.naumen.gintonic.navigate;

import java.util.List;

import ru.naumen.gintonic.source_reference.SourceCodeReference;

/**
 * A {@link NavigationCycle} can be used to jump from one {@link SourceCodeReference} to
 * another.
 * 
 * @author tmajunke
 */
public class NavigationCycle<T extends SourceCodeReference> {

	private List<T> sourceCodeReferences;

	/**
	 * The index so we know where to jump next.
	 */
	private int index = 0;

	public void setSourceCodeReferences(List<T> sourceCodeReferences) {
		this.sourceCodeReferences = sourceCodeReferences;
	}
	
	public int getIndex() {
		return index;
	}

	/**
	 * Jumps to the {@link SourceCodeReference} that is the follower of the given
	 * {@link SourceCodeReference}. It also sets the resource pointer to the follower.
	 * 
	 * To do so we must first check if the given reference is contained in this
	 * navigation cycle. The check compares the reference start position and
	 * qualified name. If the resource is not contained in this navigation cycle
	 * then nothing happens and the method returns false.
	 * 
	 * @param sourceCodeReference the {@link IJumpable}. May not be null.
	 * @return true, if the resource has been contained in this navigation
	 *         cycle. Otherwise false.
	 */
	public boolean jumpToFollower(T sourceCodeReference) {
		Integer resourceIndex = getResourceIndexFor(sourceCodeReference);
		if (resourceIndex != null) {
			this.index = resourceIndex;
			jumpToNext();
			return true;
		}
		return false;
	}

	/**
	 * Jumps to the next project resource. If we are already at the last project
	 * resource, then we jump to the first one (in other words we cycle through
	 * the project resources).
	 */
	public void jumpToNext() {
		increaseIndex();
		jumpToCurrent();
	}

	private void jumpToCurrent() {
		T jumpTarget = sourceCodeReferences.get(index);
		jumpTarget.jump();
	}

	private void increaseIndex() {
		int size = sourceCodeReferences.size();
		this.index = (index + 1) % size;
	}

	/**
	 * Returns true if the given sourceCodeReference is contained in this navigation
	 * cycle, otherwise false.
	 * 
	 * @param sourceCodeReferenceToFind the reference
	 * @return true if the given reference is contained in this navigation
	 *         cycle, otherwise false.
	 */
	private Integer getResourceIndexFor(SourceCodeReference sourceCodeReferenceToFind) {
		int i = 0;
		for (T sourceCodeReference : sourceCodeReferences){
			Integer startPosition = sourceCodeReference.getOffset();
			Integer startPositionToFind = sourceCodeReferenceToFind.getOffset();
			if (startPosition.equals(startPositionToFind)) {
				String typeNameFullyQualified = sourceCodeReference.getPrimaryTypeNameFullyQualified();
				String typeNameFullyQualifiedToFind = sourceCodeReferenceToFind.getPrimaryTypeNameFullyQualified();
				if (typeNameFullyQualified.equals(typeNameFullyQualifiedToFind)) {
					return i;
				}
			}
			i++;
		}

		return null;
	}

}
