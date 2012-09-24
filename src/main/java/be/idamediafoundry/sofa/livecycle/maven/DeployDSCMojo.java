package be.idamediafoundry.sofa.livecycle.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.adobe.idp.Document;
import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.idp.dsc.registry.RegistryException;
import com.adobe.idp.dsc.registry.component.DuplicateComponentException;
import com.adobe.idp.dsc.registry.component.client.ComponentRegistryClient;
import com.adobe.idp.dsc.registry.infomodel.Component;

/**
 * Mojo to deploy a DSC file to a LiveCycle server.
 * 
 * @goal deploy-dsc
 */
public class DeployDSCMojo extends AbstractLiveCycleMojo {

    /**
     * The DSC file which should be deployed. This file should be build as the LiveCycle specifications for Custom
     * Component packaging describe.
     * 
     * @parameter expression="${liveCycle.dsc.file}" default-value="${basedir}/src/main/lc/dsc.jar"
     * @required
     */
    private File dscFile;

    /**
     * Constructor.
     */
    public DeployDSCMojo() {
        super();
    }

    /**
     * Constructor setting all common properties for this LiveCycle Mojo.
     * 
     * @param host the LiveCycle server host
     * @param port the LiveCycle server port
     * @param protocol the LiveCycle communication protocol
     * @param username the LiveCycle server user name
     * @param password the LiveCycle server password
     * @param dscFile the jar file containing the DSC components and configuration
     */
    public DeployDSCMojo(final String host, final String port, final String protocol, final String username,
        final String password, final File dscFile) {
        super(host, port, protocol, username, password);
        this.dscFile = dscFile;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ServiceClientFactory factory = getFactory();
        ComponentRegistryClient componentRegistryClient = new ComponentRegistryClient(factory);

        try {
            Component component;
            try {
                component = componentRegistryClient.install(new Document(dscFile, false));
            } catch (DuplicateComponentException dce) {
                getLog().info(
                    "Component " + dce.getComponentId() + " is already installed, uninstalling and reinstalling");
                component = componentRegistryClient.getComponent(dce.getComponentId(), dce.getComponentVersion());
                componentRegistryClient.forceUninstall(component);
                component = componentRegistryClient.install(new Document(dscFile, false));
            }

            componentRegistryClient.start(component);
            getLog().info("Component " + component.getComponentId() + " is installed and started");

        } catch (RegistryException e) {
            getLog().debug(e);
            throw new MojoFailureException("Registry failure while deploying or configuring: " + e.getMessage());
        }
    }

    public File getDscFile() {
        return dscFile;
    }
}
