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
    public void testExecuteAnnotation() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/annotated").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "annotations");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }

    @Test
    public void testExecuteDoclet() throws Exception {
        String sourcePath = this.getClass().getResource("/pckg/doclet").getFile();
        mojo = new GenerateComponentXmlMojo("host", "port", "protocol", "username", "password", original, result, sourcePath, "doclets");
        mojo.execute();

        System.out.println(FileUtils.readFileToString(result));
    }
}
