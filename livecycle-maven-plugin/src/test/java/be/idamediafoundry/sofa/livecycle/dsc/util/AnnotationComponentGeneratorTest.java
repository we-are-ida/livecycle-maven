package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

public class AnnotationComponentGeneratorTest {
	
	private AnnotationComponentGenerator generator;
	
	@Before
	public void setUp() {
		generator = new AnnotationComponentGenerator("src/test/components", "componentId", "version", "componentCategory");
	}
	
	@Test
	public void testGenerateComponent() throws Exception {
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
