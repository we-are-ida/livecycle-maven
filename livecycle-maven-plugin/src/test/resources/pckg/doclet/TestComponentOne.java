package pckg.doclet;

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
 * @DSC
 * @smallIcon icon_small.png
 * @largeIcon icon_large.png
 * @major 1
 * @minor 2
 */
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
     * @outputParamName capitalizedString
	 */
	public String operation(String param) {
		return "string";
	}
	
	/**
	 * Setter java doc sentence one. Setter java doc sentence two.
	 * 
	 * Paragraph.
	 * 
	 * @param config Setter parameter java doc
     * @required
	 */
	public void setConfig(String config) {
		this.config = config;
	}
}