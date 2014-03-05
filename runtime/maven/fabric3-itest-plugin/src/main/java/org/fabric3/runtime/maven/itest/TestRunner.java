/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManagerFactory;
import org.apache.maven.surefire.report.RunStatistics;
import org.apache.maven.surefire.report.XMLReporter;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.fabric3.plugin.api.runtime.PluginRuntime;
import org.fabric3.runtime.maven.TestSuiteFactory;

/**
 * Executes integration tests.
 */
public class TestRunner {
    private File reportsDirectory;
    private Boolean trimStackTrace;
    private Log log;

    public TestRunner(File reportsDirectory, boolean trimStackTrace, Log log) {
        this.reportsDirectory = reportsDirectory;
        this.trimStackTrace = trimStackTrace;
        this.log = log;
    }

    public void executeTests(PluginRuntime runtime) throws MojoExecutionException, MojoFailureException {
        SurefireTestSuite testSuite =  runtime.getComponent(TestSuiteFactory.class).createTestSuite();
        log.info("Executing tests...");
        boolean success = runTests(testSuite);
        if (!success) {
            String msg = "There were test failures";
            throw new MojoFailureException(msg);
        }
    }

    @SuppressWarnings({"unchecked"})
    private boolean runTests(SurefireTestSuite suite) throws MojoExecutionException {
        try {
            List definitions = new ArrayList();
            Object[] params = new Object[]{reportsDirectory, trimStackTrace};
            definitions.add(new Object[]{XMLReporter.class.getName(), params});
            definitions.add(new Object[]{BriefFileReporter.class.getName(), params});
            definitions.add(new Object[]{BriefConsoleReporter.class.getName(), new Object[]{trimStackTrace}});
            ReporterManagerFactory factory = new ReporterManagerFactory(definitions, getClass().getClassLoader());
            suite.execute(factory, null);
            RunStatistics statistics = factory.getGlobalRunStatistics();
            return statistics.getErrorSources().isEmpty() && statistics.getFailureSources().isEmpty();
        } catch (ReporterException | TestSetFailedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}