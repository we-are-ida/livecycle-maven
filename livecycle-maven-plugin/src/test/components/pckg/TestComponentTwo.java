package pckg;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;

@Service
public class TestComponentTwo {
	
	private String config;
	
	@Operation
	public String operation(String param) {
		return "string";
	}
	
	@ConfigParam
	public void setConfig(String config) {
		this.config = config;
	}
}