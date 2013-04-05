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
import java.lang.reflect.Constructor;

import be.idamediafoundry.sofa.livecycle.dsc.util.AnnotationDrivenQDoxComponentInfoExtractor;
import be.idamediafoundry.sofa.livecycle.dsc.util.ComponentInfoExtractor;
import be.idamediafoundry.sofa.livecycle.dsc.util.DelegatingComponentGenerator;
import be.idamediafoundry.sofa.livecycle.dsc.util.DocletDrivenQDoxComponentInfoExtractor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import be.idamediafoundry.sofa.livecycle.dsc.util.ComponentGenerator;
import org.apache.maven.plugin.logging.Log;

/**
 * Mojo to generate a component XML file from java source code.
 * 
 * @goal generate-component-xml
 * @phase process-resources
 */
public class GenerateComponentXmlMojo extends AbstractLiveCycleMojo {

    private enum ExtractorType {
        ANNOTATIONS(AnnotationDrivenQDoxComponentInfoExtractor.class),
        DOCLETS(DocletDrivenQDoxComponentInfoExtractor.class);

        private Class<? extends ComponentInfoExtractor<?, ?, ?, ?, ?>> extractorType;

        private ExtractorType(Class<? extends ComponentInfoExtractor<?, ?, ?, ?, ?>> extractorType) {
            this.extractorType = extractorType;
        }

        public ComponentInfoExtractor<?, ?, ?, ?, ?> getExtractor(String sourcePath, Log log) {
            try {
                Constructor<? extends ComponentInfoExtractor<?, ?, ?, ?, ?>> constructor = extractorType.getConstructor(String.class, Log.class);
                return constructor.newInstance(sourcePath, log);
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate extractor for " + this);
            }
        }

        public static ExtractorType caseInsensitiveValueOf(String name) {
            return valueOf(name.toUpperCase());
        }
    }

    /**
     * The component xml file which will be written.
     * 
     * @parameter expression="${liveCycle.dsc.component.file}"
     *            default-value="${project.build.outputDirectory}/component.xml"
     */
    private File componentFile;

    /**
     * The component xml file which will be written.
     *
     * @parameter expression="${liveCycle.dsc.original.component.file}"
     *            default-value="${basedir}/src/main/resources/component.xml"
     */
    private File originalComponentFile;

    /**
     * The source path of the java code forming your DSC component. Each public non-abstract class will be configured as
     * a service within your component.
     * 
     * @parameter expression="${liveCycle.dsc.component.sourcePath}" default-value="${basedir}/src/main/java"
     */
    private String sourcePath;

    /**
     * The type of information the plugin should look for in the source code.
     *
     * Supported types:
     * <ul>
     *     <li>annotations</li>
     *     <li>doclets</li>
     * </ul>
     *
     * In order to use the annotations, you should include the livecycle-annotations-api artifact and annotate your classes!
     *
     * @parameter expression="${liveCycle.dsc.component.informationType}" default-value="annotations"
     */
    private String informationType;

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
     */
    public GenerateComponentXmlMojo(final String host, final String port, final String protocol, final String username,
        final String password, final File originalComponentFile, final File componentFile, final String sourcePath, final String informationType) {
        super(host, port, protocol, username, password);
        this.componentFile = componentFile;
        this.sourcePath = sourcePath;
        this.originalComponentFile = originalComponentFile;
        this.informationType = informationType;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!originalComponentFile.exists()) {
            throw new MojoFailureException("Could not generate component.xml, please make sure " + originalComponentFile.getAbsolutePath() + " exists, or change your configuration");
        }

        ComponentGenerator componentGenerator =
                new DelegatingComponentGenerator(ExtractorType.caseInsensitiveValueOf(informationType).getExtractor(sourcePath, getLog()));
        try {
            componentGenerator.generateComponentXML(originalComponentFile, componentFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException(e, "Could not generate component.xml", e.getMessage());
        }
    }
}
