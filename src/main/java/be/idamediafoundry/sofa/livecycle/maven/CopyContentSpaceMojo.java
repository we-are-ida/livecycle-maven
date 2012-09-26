/*
 * Copyright 2012 iDA MediaFoundry (www.ida-mediafoundry.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.idamediafoundry.sofa.livecycle.maven;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.livecycle.contentservices.client.CRCResult;
import com.adobe.livecycle.contentservices.client.DocumentManagementServiceClient;
import com.adobe.livecycle.contentservices.client.exceptions.CSAccessDeniedException;
import com.adobe.livecycle.contentservices.client.exceptions.CSAuthenticationException;
import com.adobe.livecycle.contentservices.client.exceptions.CSCommunicationException;
import com.adobe.livecycle.contentservices.client.exceptions.CSInvalidParameterException;
import com.adobe.livecycle.contentservices.client.exceptions.CSInvocationException;
import com.adobe.livecycle.contentservices.client.exceptions.ContentServicesException;
import com.adobe.livecycle.contentservices.client.impl.DocumentManagementServiceClientImpl;
import com.adobe.livecycle.contentservices.client.impl.UpdateVersionType;

/**
 * Recursively copies the content of the source server content space to the destination server content space, creating
 * the spaces as needed. The mojo will NOT overwrite any existing content.
 * 
 * @goal copy-contentspace
 */
public class CopyContentSpaceMojo extends AbstractLiveCycleMojo {
    /**
     * The content space folder type constant.
     */
    private static final String FOLDER_TYPE = "{http://www.alfresco.org/model/content/1.0}folder";

    /**
     * The host name/IP address of the source LiveCycle server.
     * 
     * @parameter expression="${liveCycle.source.host}"
     * @required
     */
    private String sourceHost;

    /**
     * The port number of the source LiveCycle server.
     * 
     * @parameter expression="${liveCycle.source.port}"
     * @required
     */
    private String sourcePort;

    /**
     * The protocol which should be used to communicate with the source LiveCycle server. This can be EJB of SOAP.
     * 
     * @parameter expression="${liveCycle.source.protocol}" default-value="SOAP"
     * @required
     */
    private String sourceProtocol;

    /**
     * The user name used to login to the destination LiveCycle server.
     * 
     * @parameter expression="${liveCycle.source.username}"
     * @required
     */
    private String sourceUsername;

    /**
     * The password used to login to the source LiveCycle server.
     * 
     * @parameter expression="${liveCycle.source.password}"
     * @required
     */
    private String sourcePassword;

    /**
     * The source server content space path. This is where copying will start, inclusive this folder. This should be a
     * folder!
     * 
     * @parameter expression="${liveCycle.source.contentspace.path}"
     * @required
     */
    private String sourceContentSpacePath;

    /**
     * The destination server content space path. This is where copies will end up. This should be a folder! If not set,
     * the same path as the sourceContentSpacePath is used.
     * 
     * @parameter expression="${liveCycle.contentspace.path}"
     */
    private String contentSpacePath;

    /**
     * Flag to mark if copying should overwrite existing content on the destination space (true means overwrite).
     * 
     * @parameter expression="${liveCycle.contentspace.overwrite}" default-value=false
     */
    private boolean overwrite;

    /**
     * Constructor.
     */
    public CopyContentSpaceMojo() {
        super();
    }

    /**
     * Constructor setting all common properties for LiveCycle Mojos.
     * 
     * @param host the destination LiveCycle server host
     * @param port the destination LiveCycle server port
     * @param protocol the destination LiveCycle communication protocol
     * @param username the destination LiveCycle server user name
     * @param password the destination LiveCycle server password
     * @param sourceHost the source LiveCycle server host
     * @param sourcePort the source LiveCycle server port
     * @param sourceProtocol the source LiveCycle communication protocol
     * @param sourceUsername the source LiveCycle server user name
     * @param sourcePassword the source LiveCycle server password
     * @param contentSpacePath the destination content space path
     * @param sourceContentSpacePath the source content space path
     * @param overwrite the overwrite flag
     */
    public CopyContentSpaceMojo(final String host, final String port, final String protocol, final String username,
        final String password, final String sourceHost, final String sourcePort, final String sourceProtocol,
        final String sourceUsername, final String sourcePassword, final String contentSpacePath,
        final String sourceContentSpacePath, final boolean overwrite) {
        super(host, port, protocol, username, password);
        this.sourceHost = sourceHost;
        this.sourcePassword = sourcePassword;
        this.sourcePort = sourcePort;
        this.sourceProtocol = sourceProtocol;
        this.sourceUsername = sourceUsername;
        this.contentSpacePath = contentSpacePath;
        this.sourceContentSpacePath = sourceContentSpacePath;
        this.overwrite = overwrite;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceContentSpacePath == null) {
            throw new MojoFailureException("The sourceContentSpacePath should be configured.");
        }

        if (contentSpacePath == null) {
            contentSpacePath = sourceContentSpacePath;
        }

        ServiceClientFactory sourceFactory = getFactory(sourceHost, sourcePort, sourceProtocol, sourceUsername,
            sourcePassword);
        ServiceClientFactory destinationFactory = getFactory();

        DocumentManagementServiceClient sourceDMSC = new DocumentManagementServiceClientImpl(sourceFactory);
        DocumentManagementServiceClient destinationDMSC = new DocumentManagementServiceClientImpl(destinationFactory);

        // TODO make params for storeName and versionLabel
        // Remove the content path on the destination server
        deepCopySpace(sourceContentSpacePath, contentSpacePath, sourceDMSC, destinationDMSC);
        // Start copying
        // CRCResult sourceContentCRCResult =
        // sourceDMSC.retrieveContent("SpacesStore", contentSpacePath, null);

        // System.out.println(sourceContentCRCResult.getNodeType());
    }

    /**
     * Deep copy the source path from the source content space to the destination path on the destination content space.
     * 
     * @param sourcePath the source path
     * @param destinationPath the destination path
     * @param sourceDMSC the source document management service (for reading)
     * @param destinationDMSC the destination document management service (for writing)
     * @throws MojoFailureException when copying fails
     */
    private void deepCopySpace(final String sourcePath, final String destinationPath,
        final DocumentManagementServiceClient sourceDMSC, final DocumentManagementServiceClient destinationDMSC)
        throws MojoFailureException {
        try {
            prepareDestination(destinationPath, destinationDMSC);

            List<CRCResult> spaceContents = sourceDMSC.getSpaceContents("SpacesStore", sourcePath, Boolean.FALSE);
            for (CRCResult crcResult : spaceContents) {
                if (crcResult.getNodeType().equals(FOLDER_TYPE)) {
                    deepCopySpace(sourcePath + "/" + crcResult.getNodeName(),
                        destinationPath + "/" + crcResult.getNodeName(), sourceDMSC, destinationDMSC);
                } else {
                    copyContent(destinationPath, destinationDMSC, crcResult);
                }
            }

        } catch (CSAccessDeniedException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (CSAuthenticationException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (CSCommunicationException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (CSInvalidParameterException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (ContentServicesException e) {
            throw new MojoFailureException(e.getMessage());
        }

    }

    /**
     * Copies the content from the CRC result to the destination path if it does not exist yet.
     * 
     * @param destinationPath the destination path
     * @param destinationDMSC the destination document management service (for writing)
     * @param crcResult the CRC result holding the source document to be written
     * @throws CSAccessDeniedException when access is denied on the destination
     * @throws CSAuthenticationException when authentication fails on the destination
     * @throws CSCommunicationException when communication fails to the destination
     * @throws CSInvalidParameterException when invalid parameters are being passed to the destination
     * @throws ContentServicesException when writing content fails
     */
    private void copyContent(final String destinationPath, final DocumentManagementServiceClient destinationDMSC,
        final CRCResult crcResult) throws CSAccessDeniedException, CSAuthenticationException, CSCommunicationException,
        CSInvalidParameterException, ContentServicesException {
        try {
            destinationDMSC.retrieveContent("SpacesStore", destinationPath + "/" + crcResult.getNodeName(), null);

            if (overwrite) {
                getLog().info(
                    destinationPath + "/" + crcResult.getNodeName() + " found on destination space, overwriting...");
                storeContent(destinationPath, destinationDMSC, crcResult);
            } else {
                getLog().info(
                    destinationPath + "/" + crcResult.getNodeName()
                        + " already found on destination space, skipped (overwrite flag is false).");
            }
        } catch (CSInvocationException csie) {
            // OK, it does not exist.

            getLog().info(
                destinationPath + "/" + crcResult.getNodeName() + " not found on destination space, copying...");
            storeContent(destinationPath, destinationDMSC, crcResult);
        }
    }

    /**
     * Store content to the content space destination path.
     * 
     * @param destinationPath the destination path
     * @param destinationDMSC the destination document management service
     * @param crcResult the CRD result holding the source document to be written
     */
    private void storeContent(final String destinationPath, final DocumentManagementServiceClient destinationDMSC,
        final CRCResult crcResult) {
        try {
            destinationDMSC.storeContent("SpacesStore", destinationPath, crcResult.getNodeName(),
                crcResult.getNodeType(), crcResult.getDocument(), "UTF-8", UpdateVersionType.KEEP_SAME_VERSION, null,
                crcResult.getAttributeMap());
            getLog().info(destinationPath + "/" + crcResult.getNodeName() + " copied.");
        } catch (Throwable t) {
            getLog().error(destinationPath + "/" + crcResult.getNodeName() + " not copied...", t);
        }
    }

    /**
     * Prepare the destinaton path for writing to it.
     * 
     * @param destinationPath the destination path to prepare
     * @param destinationDMSC the destination document management service
     * @throws CSAccessDeniedException when access is denied on the destination
     * @throws CSAuthenticationException when authentication fails on the destination
     * @throws CSCommunicationException when communication fails to the destination
     * @throws CSInvalidParameterException when invalid parameters are being passed to the destination
     * @throws ContentServicesException when writing content fails
     * @throws MojoFailureException when the path is not a folder
     * @throws CSInvocationException when content space invocation fails
     */
    private void prepareDestination(final String destinationPath, final DocumentManagementServiceClient destinationDMSC)
        throws CSAccessDeniedException, CSAuthenticationException, CSCommunicationException,
        CSInvalidParameterException, ContentServicesException, MojoFailureException, CSInvocationException {
        try {
            CRCResult pathResult = destinationDMSC.retrieveContent("SpacesStore", destinationPath, null);

            if (!pathResult.getNodeType().equals(FOLDER_TYPE)) {
                throw new MojoFailureException("The source content space path is not a folder!");
            }
            getLog().info(destinationPath + " found on destination space");
        } catch (CSInvocationException e) {
            // It does not exist.
            getLog().info(destinationPath + " not found on destination space, creating ...");
            destinationDMSC.createSpace("SpacesStore", destinationPath);
        }
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public String getSourceProtocol() {
        return sourceProtocol;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }
}
