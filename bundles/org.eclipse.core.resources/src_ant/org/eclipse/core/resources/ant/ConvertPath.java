/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * An Ant task which allows to switch from a file system path to a resource path, 
 * and vice versa, and store the result in a user property which name is set by the user. If the 
 * resource does not exist, the property is set to "false".
 * <p>
 * The attribute "property" must be specified, as well as only one of "fileSystemPath" or "resourcePath".
 * <p><p>
 * Example:<p>
 *	&lt;eclipse.convertPath fileSystemPath="D:\MyWork\MyProject" property="myProject.resourcePath"/&gt;
 */
public class ConvertPath extends Task {
	
	/**
	 * The file system path.
	 */
	private IPath fileSystemPath = null;
	
	/**
	 * The resource path.
	 */
	private IPath resourcePath = null;
	
	/**
	 * The name of the property where the result may be stored.
	 */
	private String property = null;
	
	/**
	 * The id of the new Path object that may be created.
	 */
	private String pathID = null;


/**
 * Constructs a new <code>ConvertPath</code> instance.
 */
public ConvertPath() {
	super();
}

/**
 * Performs the path conversion operation.
 * 
 * @exception BuildException thrown if a problem occurs during execution.
 */
public void execute() throws BuildException {
	validateAttributes();
	if (fileSystemPath == null) 
		// here, resourcePath is not null
		convertResourcePathToFileSystemPath(resourcePath);
	else
		convertFileSystemPathToResourcePath(fileSystemPath);
}

protected void convertFileSystemPathToResourcePath(IPath path) {
	IResource resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
	if (resource == null)
		throw new BuildException(Policy.bind("exception.noProjectMatchThePath", fileSystemPath.toOSString())); //$NON-NLS-1$
	if (property != null)
		project.setUserProperty(property, resource.getFullPath().toString());
	if (pathID != null) {
		Path newPath = new Path(project, resource.getFullPath().toString());
		project.addReference(pathID, newPath);
	}
}

protected void convertResourcePathToFileSystemPath(IPath path) {
	IResource resource = null;
	if (path.segmentCount() == 1)
		resource = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
	else
		resource = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		
	if (resource.getLocation() == null)
		// can occur if the first segment is not a project
		throw new BuildException(Policy.bind("exception.pathNotValid", path.toString())); //$NON-NLS-1$
	
	if (property != null)
		project.setUserProperty(property, resource.getLocation().toOSString());
	if (pathID != null) {
		Path newPath = new Path(project, resource.getLocation().toOSString());
		project.addReference(pathID, newPath);
	}
}

/**
 * Sets the file system path.
 * 
 * @param value the file corresponding to the path supplied by the user
 */
public void setFileSystemPath(File value) {
	if (resourcePath != null)
		throw new BuildException(Policy.bind("exception.cantUseBoth")); //$NON-NLS-1$
	fileSystemPath = new org.eclipse.core.runtime.Path(value.toString());
}

/**
 * Sets the resource path.
 * 
 * @param value the path	
 */
public void setResourcePath(String value) {
	if (fileSystemPath != null)
		throw new BuildException(Policy.bind("exception.cantUseBoth")); //$NON-NLS-1$
	resourcePath = new org.eclipse.core.runtime.Path(value);
}

/**
 * Sets the name of the property where the result may stored.
 * 
 * @param value the name of the property		
 */
public void setProperty(String value) {
	property = value;

}

/**
 * Sets the id for the path where the result may be stored
 * 
 * @param value the id of the path
 */
public void setPathId(String value) {
	pathID = value;
}

/**
 * Performs a validation of the receiver.
 * 
 * @exception BuildException thrown if a problem occurs during validation.
 */
protected void validateAttributes() throws BuildException {
	if (property == null && pathID == null) 
		throw new BuildException(Policy.bind("exception.propertyAndPathIdNotSpecified")); //$NON-NLS-1$

	if (resourcePath != null && (!resourcePath.isValidPath(resourcePath.toString()) || resourcePath.isEmpty())) 
		throw new BuildException(Policy.bind("exception.invalidPath", resourcePath.toOSString())); //$NON-NLS-1$
	else if (fileSystemPath != null && !fileSystemPath.isValidPath(fileSystemPath.toOSString())) 
		throw new BuildException(Policy.bind("exception.invalidPath", fileSystemPath.toOSString())); //$NON-NLS-1$
		
	if (resourcePath == null && fileSystemPath == null)
		throw new BuildException(Policy.bind("exception.mustHaveOneAttribute")); //$NON-NLS-1$
}
}
