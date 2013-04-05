package pckg.annotated;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;

/**
 * Test component sentence one. Test component sentence two. 
 * 
 * Paragraph.
 * 
 * @author Mike Seghers
 */
@Service(largeIcon = "large.ico", smallIcon = "small.ico", version = @Version(major = 2, minor = 4), categoryId = "service-cat")
public class TestComponentOne {
	
	/**
	 * Config param.
	 */
	private String config;
	
	/**
	 * Operation java doc sentence one. Operation java doc sentence two.
	 * 
	 * Paragraph.
	 *
	 * @param param Operation parameter java doc
	 * @return Return java doc
	 */
	@Operation(name = "operationOverride", outputName = "outOverride")
	public String operation(String param) {
		return "string";
	}
	
	/**
	 * Setter java doc sentence one. Setter java doc sentence two.
	 * 
	 * Paragraph.
	 * 
	 * @param config Setter parameter java doc
	 */
	@ConfigParam(defaultValue = "testDefault", required = true)
	public void setConfig(String config) {
		this.config = config;
	}
}