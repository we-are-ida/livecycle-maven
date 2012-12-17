package pckg;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.FactoryMethod;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;

@Service
public class TestComponentWithFactoryMethod {
	
	private String config;
	
	public String operation(String param) {
		return "string";
	}
	
	public void setConfig(String config) {
		this.config = config;
	}
	
	@FactoryMethod
	public static TestComponentWithFactoryMethod getInstance() {
		return new TestComponentWithFactoryMethod();
	}
}