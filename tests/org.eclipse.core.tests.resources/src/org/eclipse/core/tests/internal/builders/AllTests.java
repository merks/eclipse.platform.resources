package org.eclipse.core.tests.internal.builders;

import junit.framework.*;


public class AllTests extends TestCase {
public AllTests() {
	super(null);
}
public AllTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(BuilderTest.suite());
	suite.addTest(BuildDeltaVerificationTest.suite());
	suite.addTest(MultiProjectBuildTest.suite());
	return suite;
}
}

