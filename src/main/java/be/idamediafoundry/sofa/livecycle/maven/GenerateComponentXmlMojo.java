/*
 * Copyright 2012 iDA MediaFoundry (www.ida-mediafoundry.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.idamediafoundry.sofa.livecycle.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import be.idamediafoundry.sofa.livecycle.dsc.util.ComponentGenerator;

/**
 * Mojo to generate a component XML file from java source code.
 * 
 * @goal generate-component-xml
 * @phase process-resources
 */
public class GenerateComponentXmlMojo extends AbstractLiveCycleMojo {

    /**
     * The component xml file which will be (over)written.
     * 
     * @parameter expression="${liveCycle.dsc.component.file}"
     *            default-value="${project.build.outputDirectory}/component.xml"
     */
    private File componentFile;

    /**
     * The source path of the java code forming your DSC component. Each public non-abstract class will be configured as
     * a service within your component.
     * 
     * @parameter expression="${liveCycle.dsc.component.sourcePath}" default-value="${basedir}/src/main/java"
     */
    private String sourcePath;

    /**
     * The component id.
     * 
     * @parameter expression="${liveCycle.dsc.component.id}" default-value="${project.artifactId}"
     */
    private String componentId;

    /**
     * The component version.
     * 
     * @parameter expression="${liveCycle.dsc.component.version}" default-value="${project.version}"
     */
    private String componentVersion;

    /**
     * The component category.
     * 
     * @parameter expression="${liveCycle.dsc.component.category}" default-value="${project.artifactId}"
     */
    private String componentCategory;

    /**
     * Constructor.
     */
    public GenerateComponentXmlMojo() {
        super();
    }

    /**
     * Constructor setting all common properties for this LiveCycle Mojo.
     * 
     * @param host the LiveCycle server host
     * @param port the LiveCycle server port
     * @param protocol the LiveCycle communication protocol
     * @param username the LiveCycle server user name
     * @param password the LiveCycle server password
     * @param componentFile the component file
     * @param sourcePath the java source path
     * @param componentId the component id
     * @param componentVersion the component version
     * @param componentCategory the component services category
     */
    public GenerateComponentXmlMojo(final String host, final String port, final String protocol, final String username,
        final String password, final File componentFile, final String sourcePath, final String componentId,
        final String componentVersion, final String componentCategory) {
        super(host, port, protocol, username, password);
        this.componentFile = componentFile;
        this.sourcePath = sourcePath;
        this.componentId = componentId;
        this.componentVersion = componentVersion;
        this.componentCategory = componentCategory;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ComponentGenerator componentGenerator = new ComponentGenerator(getLog());
        try {
            componentGenerator.generateComponentXML(componentFile, sourcePath, componentId, componentVersion,
                componentCategory);
        } catch (Exception e) {
            throw new MojoFailureException(e, "Could not generate component.xml", e.getMessage());
        }
    }

}
