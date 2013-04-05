/*
 * Copyright 2012-2013 iDA MediaFoundry (www.ida-mediafoundry.be)
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

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.idp.dsc.clientsdk.ServiceClientFactoryProperties;

/**
 * Abstract LiveCycle mojo, defining connection parameters and providing an easy way to retrieve a
 * {@link ServiceClientFactory} instance.
 */
public abstract class AbstractLiveCycleMojo extends AbstractMojo {
    /**
     * The host name/IP address of the destination LiveCycle server.
     * 
     * @parameter expression="${liveCycle.host}"
     * @required
     */
    private String host;

    /**
     * The port number of the destination LiveCycle server.
     * 
     * @parameter expression="${liveCycle.port}"
     * @required
     */
    private String port;

    /**
     * The protocol which should be used to communicate with the destination LiveCycle server. This can be EJB of SOAP.
     * 
     * @parameter expression="${liveCycle.protocol}" default-value="SOAP"
     * @required
     */
    private String protocol;

    /**
     * The user name used to login to the destination LiveCycle server.
     * 
     * @parameter expression="${liveCycle.username}"
     * @required
     */
    private String username;

    /**
     * The password used to login to the source LiveCycle server.
     * 
     * @parameter expression="${liveCycle.password}"
     * @required
     */
    private String password;

    /**
     * Constructor.
     */
    public AbstractLiveCycleMojo() {
    }

    /**
     * Constructor setting all common properties for LiveCycle Mojos.
     * 
     * @param host the LiveCycle server host
     * @param port the LiveCycle server port
     * @param protocol the LiveCycle communication protocol
     * @param username the LiveCycle server user name
     * @param password the LiveCycle server password
     */
    public AbstractLiveCycleMojo(final String host, final String port, final String protocol, final String username,
        final String password) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
    }

    /**
     * Get the {@link ServiceClientFactory} for the configured properties.
     * 
     * @return the {@link ServiceClientFactory} for the configured properties
     * @throws MojoFailureException when one of the required parameters was not found.
     */
    protected ServiceClientFactory getFactory() throws MojoFailureException {
        return getFactory(host, port, protocol, username, password);
    }

    /**
     * Get the {@link ServiceClientFactory} for the given properties.
     * 
     * @param host The host name/IP address of the destination LiveCycle server.
     * @param port The port number of the destination LiveCycle server.
     * @param protocol The protocol which should be used to communicate with the LiveCycle server. This can be EJB of
     *            SOAP.
     * @param username The user name used to login to the LiveCycle server.
     * @param password The password used to login to the LiveCycle server.
     * @return the {@link ServiceClientFactory} for the given properties
     * @throws MojoFailureException when one of the required parameters was not found.
     */
    protected ServiceClientFactory getFactory(final String host, final String port, final String protocol,
        final String username, final String password) throws MojoFailureException {
        Properties connectionProps = new Properties();
        StringBuilder url = new StringBuilder();
        String protocolEndpointKey;
        if ("SOAP".equalsIgnoreCase(protocol)) {
            connectionProps.setProperty(ServiceClientFactoryProperties.DSC_TRANSPORT_PROTOCOL,
                ServiceClientFactoryProperties.DSC_SOAP_PROTOCOL);
            url.append("http://");

            protocolEndpointKey = ServiceClientFactoryProperties.DSC_DEFAULT_SOAP_ENDPOINT;
        } else if ("EJB".equalsIgnoreCase(protocol)) {
            connectionProps.setProperty(ServiceClientFactoryProperties.DSC_TRANSPORT_PROTOCOL,
                ServiceClientFactoryProperties.DSC_EJB_PROTOCOL);
            url.append("jnp://");

            protocolEndpointKey = ServiceClientFactoryProperties.DSC_DEFAULT_EJB_ENDPOINT;
        } else {
            throw new MojoFailureException(protocol + " unknown.");
        }

        url.append(host).append(":").append(port);

        connectionProps.setProperty(protocolEndpointKey, url.toString());
        connectionProps.setProperty(ServiceClientFactoryProperties.DSC_SERVER_TYPE,
            ServiceClientFactoryProperties.DSC_JBOSS_SERVER_TYPE);
        connectionProps.setProperty(ServiceClientFactoryProperties.DSC_CREDENTIAL_USERNAME, username);
        connectionProps.setProperty(ServiceClientFactoryProperties.DSC_CREDENTIAL_PASSWORD, password);

        return ServiceClientFactory.createInstance(connectionProps);
    }

    protected String getHost() {
        return host;
    }

    protected String getPort() {
        return port;
    }

    protected String getProtocol() {
        return protocol;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

}
