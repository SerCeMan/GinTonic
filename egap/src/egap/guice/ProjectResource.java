package egap.guice;

import java.io.Serializable;
import java.util.List;


import egap.utils.IProjectResource;
import egap.utils.StringUtils;

public class ProjectResource implements Serializable, IProjectResource {

	private static final long serialVersionUID = 8853803394114472544L;

	private String projectName;
	private List<String> srcFolderPathComponents;
	private List<String> packageNameComponents;
	private String typeName;
	protected Integer startPosition;
	protected Integer length;

	public ProjectResource() {
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setSrcFolderPathComponents(List<String> srcFolderPathComponents) {
		this.srcFolderPathComponents = srcFolderPathComponents;
	}

	public void setPackage(List<String> packageFullyQualified) {
		this.packageNameComponents = packageFullyQualified;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public List<String> getSrcFolderPathComponents() {
		return srcFolderPathComponents;
	}

	@Override
	public List<String> getPackage() {
		return packageNameComponents;
	}
	

	@Override
	public String getPackageFullyQualified() {
		return StringUtils.join('.', packageNameComponents);
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public Integer getStartPosition() {
		return startPosition;
	}

	@Override
	public Integer getLength() {
		return length;
	}

	@Override
	public String getTypeNameFullyQualified() {
		return packageNameComponents + "." + typeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((length == null) ? 0 : length.hashCode());
		result = prime
				* result
				+ ((packageNameComponents == null) ? 0
						: packageNameComponents.hashCode());
		result = prime * result
				+ ((projectName == null) ? 0 : projectName.hashCode());
		result = prime
				* result
				+ ((srcFolderPathComponents == null) ? 0
						: srcFolderPathComponents.hashCode());
		result = prime * result
				+ ((startPosition == null) ? 0 : startPosition.hashCode());
		result = prime * result
				+ ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectResource other = (ProjectResource) obj;
		if (length == null) {
			if (other.length != null)
				return false;
		}
		else if (!length.equals(other.length))
			return false;
		if (packageNameComponents == null) {
			if (other.packageNameComponents != null)
				return false;
		}
		else if (!packageNameComponents.equals(other.packageNameComponents))
			return false;
		if (projectName == null) {
			if (other.projectName != null)
				return false;
		}
		else if (!projectName.equals(other.projectName))
			return false;
		if (srcFolderPathComponents == null) {
			if (other.srcFolderPathComponents != null)
				return false;
		}
		else if (!srcFolderPathComponents.equals(other.srcFolderPathComponents))
			return false;
		if (startPosition == null) {
			if (other.startPosition != null)
				return false;
		}
		else if (!startPosition.equals(other.startPosition))
			return false;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		}
		else if (!typeName.equals(other.typeName))
			return false;
		return true;
	}
	
	

}
