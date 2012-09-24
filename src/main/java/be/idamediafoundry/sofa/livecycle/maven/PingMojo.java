package be.idamediafoundry.sofa.livecycle.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.idp.um.api.AuthenticationManager;
import com.adobe.idp.um.api.UMException;
import com.adobe.livecycle.usermanager.client.AuthenticationManagerServiceClient;

/**
 * Fails the build if the server cannot be contacted. This mojo can be used to fail the build early on, when the
 * LiveCycle server is unreachable.
 * 
 * @goal ping
 */
public class PingMojo extends AbstractLiveCycleMojo {

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ServiceClientFactory serviceClientFactory = getFactory();
        if (serviceClientFactory == null) {
            throw new MojoFailureException("Failure - Server could not be contacted (" + getHost() + ", " + getPort()
                + ")");
        } else {
            AuthenticationManager am = new AuthenticationManagerServiceClient(serviceClientFactory);
            try {
                am.authenticate(getUsername(), getPassword().getBytes());

                getLog().info("Success " + getHost() + ", " + getPort());
            } catch (UMException e) {
                throw new MojoFailureException(e, "Unable to connect",
                    "Failure - Server could not be contacted due to exception (" + getHost() + ", " + getPort() + "): "
                        + e.getMessage());
            }

        }

    }

}
