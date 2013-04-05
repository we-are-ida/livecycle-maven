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

package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

public class DelegatingComponentGeneratorTest {
	
	private DelegatingComponentGenerator<JavaClass, JavaMethod, JavaMethod, JavaParameter, Type> generator;
	
	@Before
	public void setUp() {
		generator = new DelegatingComponentGenerator<JavaClass, JavaMethod, JavaMethod, JavaParameter, Type>(new AnnotationDrivenQDoxComponentInfoExtractor(this.getClass().getResource("/pckg").getFile(), new SystemStreamLog()));
	}

	@Test
	public void test() throws Exception {
		File file = File.createTempFile("test", "xml");
        File original = new File(this.getClass().getResource("/base/base-component.xml").getFile());
		generator.generateComponentXML(original, file);
        System.out.println(FileUtils.readFileToString(file));
    }
}
