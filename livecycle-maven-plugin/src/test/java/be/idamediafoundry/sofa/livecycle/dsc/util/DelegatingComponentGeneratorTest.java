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
