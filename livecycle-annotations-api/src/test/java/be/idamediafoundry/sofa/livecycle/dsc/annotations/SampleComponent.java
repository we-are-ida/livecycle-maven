package be.idamediafoundry.sofa.livecycle.dsc.annotations;

@Service(largeIcon = "largeIcon.jpg", smallIcon = "smallIcon.jpg", version = @Version(major = 2, minor = 1))
public class SampleComponent {
	@Operation(name = "methodOverride", outputName = "outOverride")
	public String method(int param) {
		return null;
	}

	@ConfigParam(required = true, defaultValue = "default")
	public void setConfig(String config) {

	}
}
