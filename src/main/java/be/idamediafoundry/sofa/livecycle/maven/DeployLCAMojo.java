package be.idamediafoundry.sofa.livecycle.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.adobe.idp.Document;
import com.adobe.idp.applicationmanager.application.ApplicationManagerException;
import com.adobe.idp.applicationmanager.application.ApplicationStatus;
import com.adobe.idp.applicationmanager.client.ApplicationManager;
import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;

/**
 * Mojo to deploy an LCA file to a LiveCycle server.
 * 
 * @goal deploy-lca
 */
public class DeployLCAMojo extends AbstractLiveCycleMojo {

    /**
     * The LCA file which should be deployed.
     * 
     * @parameter expression="${liveCycle.lca.file}" default-value="${basedir}/src/main/lc/project.lca"
     * @required
     */
    private File lcaFile;

    /**
     * Constructor.
     */
    public DeployLCAMojo() {
        super();
    }

    /**
     * Constructor setting all common properties for LiveCycle Mojos.
     * 
     * @param host the LiveCycle server host
     * @param port the LiveCycle server port
     * @param protocol the LiveCycle communication protocol
     * @param username the LiveCycle server user name
     * @param password the LiveCycle server password
     * @param lcaFile the LCA file
     */
    public DeployLCAMojo(final String host, final String port, final String protocol, final String username,
        final String password, final File lcaFile) {
        super(host, port, protocol, username, password);
        this.lcaFile = lcaFile;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ServiceClientFactory serviceClientFactory = getFactory();

        if (lcaFile == null) {
            throw new MojoFailureException("The LCA file should be configured.");
        }

        if (!lcaFile.exists()) {
            throw new MojoFailureException("Could not find LCA file: " + lcaFile);
        }

        try {
            FileInputStream fileApp = new FileInputStream(lcaFile);
            Document lcApp = new Document(fileApp);

            // Create an ApplicationManager object
            ApplicationManager appManager = new ApplicationManager(serviceClientFactory);

            // Import the application into the production server
            ApplicationStatus appStatus = appManager.importApplicationArchive(lcApp);
            int status = appStatus.getStatusCode();

            // Determine if the application was successfully deployed
            if (status == ApplicationStatus.STATUS_IMPORT_COMPLETE) {
                getLog().info("The application was successfully deployed");
            } else {
                throw new MojoFailureException("Deployment failed, status = " + appStatus.getErrorName() + ", "
                    + appStatus.getErrorString());
            }
        } catch (IOException e) {
            getLog().debug(e);
            throw new MojoFailureException("IOException: " + e.getMessage());
        } catch (ApplicationManagerException e) {
            getLog().debug(e);
            throw new MojoFailureException("Application manager failure while deploying: " + e.getMessage());
        }

    }
}
