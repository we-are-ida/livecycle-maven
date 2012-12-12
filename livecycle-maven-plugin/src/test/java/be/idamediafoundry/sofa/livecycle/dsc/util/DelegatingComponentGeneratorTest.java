package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
		generator = new DelegatingComponentGenerator<JavaClass, JavaMethod, JavaMethod, JavaParameter, Type>(new AnnotationDrivenQDoxComponentInfoExtractor("src/test/components", "componentId", "componentCategory", "version"));
	}

	@Test
	public void test() throws Exception {
		File file = File.createTempFile("test", "xml");
		generator.generateComponentXML(file);
		InputStream is = new FileInputStream(file);  
		  
		// initialize  
		byte[] buffer = new byte[4096]; // tweaking this number may increase performance  
		int len;  
		while ((len = is.read(buffer)) != -1)  
		{  
		    System.out.write(buffer, 0, len);  
		}  
		is.close();  
	}
}
