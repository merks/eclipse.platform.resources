package org.eclipse.core.tests.resources.usecase;

import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public abstract class SnapshotTest extends EclipseWorkspaceTest {

	/** project names */
	static final String PROJECT_1 = "MyProject";
	static final String PROJECT_2 = "Project2";

	/** activities */
	static final String COMMENT_1 = "COMMENT ONE";
	static final String COMMENT_2 = "COMMENT TWO";
public SnapshotTest() {
}
public SnapshotTest(String name) {
	super(name);
}
}
