package pckg.doclet;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.FactoryMethod;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;

/**
 * @DSC
 */
public class TestComponentWithFactoryMethod {
	
	private String config;
	
	public String operation(String param) {
		return "string";
	}
	
	public void setConfig(String config) {
		this.config = config;
	}

    /**
     * @factoryMethod
     */
	public static TestComponentWithFactoryMethod getInstance() {
		return new TestComponentWithFactoryMethod();
	}
}