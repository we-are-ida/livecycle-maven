/*
 * Copyright 2012-2013 iDA MediaFoundry (www.ida-mediafoundry.be)
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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author mike
 */
public class GenerateComponentXmlMojoTest {
    private GenerateComponentXmlMojo mojo;

    private File result;
    private File original;


    @Before
    public void setUp() throws Exception {
        result = File.createTempFile("test", "xml");
        original = new File(this.getClass().getResource("/base/base-component.xml").getFile());
    }

    @Test
    public void testExecuteAnnotationWithBootstrapAndLifeCycleClass() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/annotatedbootstraplifecycle").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "annotations");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }

    @Test
    public void testExecuteDocletWithBootstrapAndLifeCycleClass() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/docletsbootstraplifecycle").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "doclets");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }

    @Test
    public void testExecuteAnnotationWithoutBootstrapAndLifeCycleClass() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/annotated").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "annotations");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }

    @Test
    public void testExecuteDocletWithoutBootstrapAndLifeCycleClass() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/doclets").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "doclets");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }
}
