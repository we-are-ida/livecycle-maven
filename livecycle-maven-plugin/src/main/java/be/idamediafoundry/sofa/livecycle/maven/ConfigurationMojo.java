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

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Element;

import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointType;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointWithSettingsAndMappingType;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointWithSettingsType;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.InputParameterMappingType;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.OutputParameterMappingType;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config.Service;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config.Service.Configuration;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config.Service.Endpoints;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config.Service.Security;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.Config.Service.Configuration.Property;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointWithSettingsAndMappingType.InputParameterMapping;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointWithSettingsAndMappingType.OutputParameterMapping;
import be.idamediafoundry.sofa.livecycle.maven.lca.configuration.EndpointWithSettingsType.Settings;

import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.idp.dsc.registry.EndpointCategoryNotFoundException;
import com.adobe.idp.dsc.registry.RegistryException;
import com.adobe.idp.dsc.registry.endpoint.CreateEndpointCategoryInfo;
import com.adobe.idp.dsc.registry.endpoint.CreateEndpointInfo;
import com.adobe.idp.dsc.registry.endpoint.ModifyEndpointInfo;
import com.adobe.idp.dsc.registry.endpoint.client.EndpointRegistryClient;
import com.adobe.idp.dsc.registry.infomodel.Endpoint;
import com.adobe.idp.dsc.registry.infomodel.EndpointCategory;
import com.adobe.idp.dsc.registry.infomodel.ServiceConfiguration;
import com.adobe.idp.dsc.registry.service.ModifyServiceConfigurationInfo;
import com.adobe.idp.dsc.registry.service.ModifyServiceInfo;
import com.adobe.idp.dsc.registry.service.client.ServiceRegistryClient;

/**
 * Mojo to configure LiveCycle services. The configuration file contains information for setting/changing service
 * configuration and security options as well as add and/or change end point definitions.
 * 
 * @goal configure
 */
public class ConfigurationMojo extends AbstractLiveCycleMojo {

    /**
     * The configuration file which should be used to configure the deployed services and their end points.
     * 
     * @parameter expression="${liveCycle.lca.configurationFile}" default-value="${basedir}/src/main/lc/config.xml"
     * @required
     */
    private File configurationFile;

    /**
     * Constructor.
     */
    public ConfigurationMojo() {
        super();
    }

    /**
     * Constructor setting all common properties for configuring LiveCycle services.
     * 
     * @param host the LiveCycle server host
     * @param port the LiveCycle server port
     * @param protocol the LiveCycle communication protocol
     * @param username the LiveCycle server user name
     * @param password the LiveCycle server password
     * @param configurationFile the configuration XML file holding the configuration parameters to be used to configure
     *            the services.
     */
    public ConfigurationMojo(final String host, final String port, final String protocol, final String username,
        final String password, final File configurationFile) {
        super(host, port, protocol, username, password);
        this.configurationFile = configurationFile;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (configurationFile.exists()) {

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Config.class.getPackage().getName());
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Config config = (Config) unmarshaller.unmarshal(configurationFile);
                ServiceClientFactory serviceClientFactory = getFactory();

                List<Service> services = config.getService();
                getLog().info("Setting configuration properties from " + configurationFile);
                for (Service service : services) {
                    getLog().info("Configuring service " + service.getName());

                    ServiceRegistryClient serviceReg = new ServiceRegistryClient(serviceClientFactory);
                    ServiceConfiguration serviceConfiguration = serviceReg.getHeadServiceConfiguration(service
                        .getName());

                    ModifyServiceConfigurationInfo modifyServiceConfigurationInfo = new ModifyServiceConfigurationInfo();
                    String serviceId = serviceConfiguration.getServiceId();
                    modifyServiceConfigurationInfo.setServiceId(serviceId);
                    modifyServiceConfigurationInfo.setMajorVersion(serviceConfiguration.getMajorVersion());
                    modifyServiceConfigurationInfo.setMinorVersion(serviceConfiguration.getMinorVersion());

                    handleSecurity(service, serviceReg, modifyServiceConfigurationInfo, serviceId);

                    handleConfigurationParameters(service, modifyServiceConfigurationInfo);

                    getLog().info("Consolidating for service " + serviceId);
                    serviceReg.modifyConfiguration(modifyServiceConfigurationInfo);

                    handleEndpointConfiguration(serviceClientFactory, service.getEndpoints(), serviceId);
                }
            } catch (RegistryException e) {
                getLog().debug(e);
                throw new MojoFailureException("Application manager failure while configuring: " + e.getMessage());
            } catch (JAXBException e) {
                getLog().debug(e);
                throw new MojoFailureException("Configuration file could not be parsed: " + e.getMessage());
            }
        } else {
            throw new MojoFailureException("No configuration file found (I was looking at " + configurationFile
                + "), no configuration set");
        }
    }

    /**
     * Handle configuration parameters, setting them on the modify service configuration info as needed.
     * 
     * @param service the service
     * @param modifyServiceConfigurationInfo the modify service condiguration info
     */
    private void handleConfigurationParameters(final Service service,
        final ModifyServiceConfigurationInfo modifyServiceConfigurationInfo) {
        Configuration configuration = service.getConfiguration();
        if (configuration != null) {
            List<Property> properties = configuration.getProperty();
            for (Property property : properties) {
                modifyServiceConfigurationInfo.setConfigParameterAsText(property.getName(), property.getValue());
            }
        }
    }

    /**
     * Handle security configuration on a service.
     * 
     * @param service the service element holding configuration parameters
     * @param serviceReg the service registry client
     * @param modifyServiceConfigurationInfo the modify service configuration info
     * @param serviceId the service identifier
     * @throws RegistryException when changing the security configuration fails
     */
    private void handleSecurity(final Service service, final ServiceRegistryClient serviceReg,
        final ModifyServiceConfigurationInfo modifyServiceConfigurationInfo, final String serviceId)
        throws RegistryException {
        Security security = service.getSecurity();
        if (security != null) {
            Boolean disableSecurity = security.isDisableSecurity();
            if (disableSecurity == null) {
                disableSecurity = Boolean.FALSE;
            }
            ModifyServiceInfo modifyServiceInfo = new ModifyServiceInfo();
            modifyServiceInfo.setId(serviceId);
            modifyServiceInfo.setSecurityEnabled(!disableSecurity.booleanValue());
            serviceReg.modifyService(modifyServiceInfo);

            String runAs = security.getRunAs();
            if (runAs != null) {
                if ("invoker".equalsIgnoreCase(runAs)) {
                    modifyServiceConfigurationInfo.setRunAsConfiguration(ServiceConfiguration.RUN_AS_INVOKER);
                } else if ("system".equalsIgnoreCase(runAs)) {
                    modifyServiceConfigurationInfo.setRunAsConfiguration(ServiceConfiguration.RUN_AS_SYSTEM);
                } else {
                    modifyServiceConfigurationInfo.setRunAsConfiguration(runAs);
                }
            }
        }
    }

    /**
     * Handle end point configuration for the service identified by id with the given end point elements.
     * 
     * @param serviceClientFactory the service client factory
     * @param endpoints the end point element container
     * @param serviceId the service identifier
     */
    private void handleEndpointConfiguration(final ServiceClientFactory serviceClientFactory,
        final Endpoints endpoints, final String serviceId) {
        if (endpoints != null) {
            Boolean mustDeleteExistingEndpoints = endpoints.isRemoveExisting();
            if (mustDeleteExistingEndpoints == null) {
                mustDeleteExistingEndpoints = Boolean.FALSE;
            }

            EndpointRegistryClient endPointClient = new EndpointRegistryClient(serviceClientFactory);
            getLog().info("Configuring endpoints for " + serviceId);

            @SuppressWarnings("unchecked")
            List<Endpoint> serviceEndpoints = endPointClient.getServiceEndpoints(serviceId, null, null);
            if (mustDeleteExistingEndpoints.booleanValue()) {
                for (Endpoint existingEndpoint : serviceEndpoints) {
                    endPointClient.remove(existingEndpoint);
                }
                serviceEndpoints = null;
            }

            handleEndpointTypes(serviceId, endPointClient, serviceEndpoints, endpoints.getEjb(), "EJB");
            handleEndpointTypes(serviceId, endPointClient, serviceEndpoints, endpoints.getSoap(), "SOAP");
            handleEndpointTypes(serviceId, endPointClient, serviceEndpoints, endpoints.getRest(), "REST");
            handleEndpointTypes(serviceId, endPointClient, serviceEndpoints, endpoints.getRemote(), "Remoting");

            handleEndpointWithSettingsTypes(serviceId, endPointClient, serviceEndpoints, endpoints.getTaskManager(),
                "TaskManagerConnector");

            handleEndpointWithSettingsAndMapping(serviceId, endPointClient, serviceEndpoints, endpoints.getEmail(),
                "Email");
            handleEndpointWithSettingsAndMapping(serviceId, endPointClient, serviceEndpoints,
                endpoints.getWatchedFolder(), "WatchedFolder");
        } else {
            getLog().info("No endpoint configuration found for " + serviceId);
        }

    }

    /**
     * Handle end point types, modifies/configures the end points on the service.
     * 
     * @param serviceId the service id
     * @param endPointClient the end point client
     * @param serviceEndpoints the existing end points
     * @param endpointTypes the end point elements holding configuration parameters
     * @param type the type of the end point
     */
    private void handleEndpointTypes(final String serviceId, final EndpointRegistryClient endPointClient,
        final List<Endpoint> serviceEndpoints, final List<EndpointType> endpointTypes, final String type) {
        if (endpointTypes != null) {
            for (EndpointType endpointType : endpointTypes) {
                String endpointName = calculateEndpointName(serviceId, endpointType);
                try {
                    Endpoint endpoint = getExistingEndpointByNameAndType(serviceEndpoints, endpointName, type);

                    if (endpoint == null) {
                        CreateEndpointInfo createEndpointInfo = handleGeneralInfo(serviceId, type, endpointName,
                            endpointType.getOperation(), endpointType.getDescription());

                        endpoint = endPointClient.createEndpoint(createEndpointInfo);
                    } else {
                        ModifyEndpointInfo endpointInfo = handleGeneralInfo(endpointName, endpointType.getOperation(),
                            endpointType.getDescription(), endpoint);

                        endpoint = endPointClient.modifyEndpoint(endpointInfo);
                    }

                    endPointClient.enable(endpoint);
                } catch (RegistryException e) {
                    getLog().error("Could not configure endpoint " + endpointName + " for service " + serviceId, e);
                }
            }
        }
    }

    /**
     * Handle end points that need settings for the service identified by service id.
     * 
     * @param serviceId the service id
     * @param endPointClient the end point client
     * @param serviceEndpoints the existing end points
     * @param endpointWithSettingTypes the end point elements holding configuration parameters
     * @param type the end point type
     */
    private void handleEndpointWithSettingsTypes(final String serviceId, final EndpointRegistryClient endPointClient,
        final List<Endpoint> serviceEndpoints, final List<EndpointWithSettingsType> endpointWithSettingTypes,
        final String type) {
        if (endpointWithSettingTypes != null) {
            for (EndpointWithSettingsType endpointWithSettingsType : endpointWithSettingTypes) {
                String endpointName = calculateEndpointName(serviceId, endpointWithSettingsType);
                String category = endpointWithSettingsType.getCategory();

                EndpointCategory endpointCategory = null;
                try {
                    endpointCategory = handleCategory(endPointClient, category);
                } catch (RegistryException e) {
                    getLog().error(
                        "Could not configure category " + category + " for endpoint " + endpointName + " for service "
                            + serviceId, e);
                }

                try {
                    Endpoint endpoint = getExistingEndpointByNameAndType(serviceEndpoints, endpointName, type);

                    if (endpoint == null) {
                        CreateEndpointInfo createEndpointInfo = handleGeneralInfo(serviceId, type, endpointName,
                            endpointWithSettingsType.getOperation(), endpointWithSettingsType.getDescription());
                        if (endpointCategory != null) {
                            createEndpointInfo.setCategoryId(endpointCategory.getId());
                        }

                        Settings settings = endpointWithSettingsType.getSettings();
                        handleSettings(createEndpointInfo, settings);

                        endpoint = endPointClient.createEndpoint(createEndpointInfo);
                    } else {
                        ModifyEndpointInfo endpointInfo = handleGeneralInfo(endpointName,
                            endpointWithSettingsType.getOperation(), endpointWithSettingsType.getDescription(),
                            endpoint);
                        if (endpointCategory != null) {
                            endpointInfo.setCategoryId(endpointCategory.getId());
                        }

                        Settings settings = endpointWithSettingsType.getSettings();
                        handleSettings(endpointInfo, settings);

                        endpoint = endPointClient.modifyEndpoint(endpointInfo);
                    }

                    endPointClient.enable(endpoint);
                } catch (RegistryException e) {
                    getLog().error("Could not configure endpoint " + endpointName + " for service " + serviceId, e);
                }
            }
        }
    }

    /**
     * Handle end points that need settings and parameters for the service identified by service id.
     * 
     * @param serviceId the service id
     * @param endPointClient the end point client
     * @param serviceEndpoints the existing end points
     * @param endpointWithSettingsAndMappingTypes the end point elements holding configuration parameters
     * @param type the end point type
     */
    private void handleEndpointWithSettingsAndMapping(final String serviceId,
        final EndpointRegistryClient endPointClient, final List<Endpoint> serviceEndpoints,
        final List<EndpointWithSettingsAndMappingType> endpointWithSettingsAndMappingTypes, final String type) {
        if (endpointWithSettingsAndMappingTypes != null) {
            for (EndpointWithSettingsAndMappingType endpointWithSettingsAndMappingType : endpointWithSettingsAndMappingTypes) {
                String endpointName = calculateEndpointName(serviceId, endpointWithSettingsAndMappingType);
                String operation = endpointWithSettingsAndMappingType.getOperation();
                String description = endpointWithSettingsAndMappingType.getDescription();

                try {
                    Endpoint endpoint = getExistingEndpointByNameAndType(serviceEndpoints, endpointName, type);

                    if (endpoint == null) {
                        // Create new endpoint
                        CreateEndpointInfo createEndpointInfo = handleGeneralInfo(serviceId, type, endpointName,
                            operation, description);

                        handleSettings(createEndpointInfo, endpointWithSettingsAndMappingType.getSettings());

                        handleInputParameterMapping(createEndpointInfo,
                            endpointWithSettingsAndMappingType.getInputParameterMapping());

                        handleOutputParameterMapping(createEndpointInfo,
                            endpointWithSettingsAndMappingType.getOutputParameterMapping());

                        endpoint = endPointClient.createEndpoint(createEndpointInfo);
                    } else {
                        ModifyEndpointInfo endpointInfo = handleGeneralInfo(endpointName, operation, description,
                            endpoint);

                        handleSettings(endpointInfo, endpointWithSettingsAndMappingType.getSettings());

                        handleInputParameterMapping(endpointInfo,
                            endpointWithSettingsAndMappingType.getInputParameterMapping());

                        handleOutputParameterMapping(endpointInfo,
                            endpointWithSettingsAndMappingType.getOutputParameterMapping());

                        endpoint = endPointClient.modifyEndpoint(endpointInfo);
                    }

                    endPointClient.enable(endpoint);
                } catch (RegistryException e) {
                    getLog().error("Could not configure endpoint " + endpointName + " for service " + serviceId, e);
                }
            }
        }
    }

    /**
     * Retrieve an end point category based on the category name. If the category does not exist, a new one will be
     * created.
     * 
     * @param endPointClient the end point client to retrieve or save the newly created category
     * @param category the name of the category to retrieve or create.
     * @return the end point category
     * @throws RegistryException when creating a new category fails
     */
    private EndpointCategory handleCategory(final EndpointRegistryClient endPointClient, final String category)
        throws RegistryException {
        EndpointCategory endpointCategory = null;
        if (category != null && !"".equals(category)) {
            try {
                endpointCategory = endPointClient.getEndpointCategory(category);
            } catch (EndpointCategoryNotFoundException e) {
                CreateEndpointCategoryInfo catInfo = new CreateEndpointCategoryInfo(category, "");
                try {
                    endpointCategory = endPointClient.createEndpointCategory(catInfo);
                } catch (RegistryException re) {
                    throw re;
                }
            }
        }
        return endpointCategory;
    }

    /**
     * Calculate the end point name. If the end point type contains a name, this is used, otherwise the service id is
     * used.
     * 
     * @param serviceId the service id
     * @param endpointType the end point type
     * @return the name
     */
    private String calculateEndpointName(final String serviceId, final EndpointType endpointType) {
        String endpointName = endpointType.getName();
        if (endpointName == null) {
            endpointName = serviceId;
        }
        return endpointName;
    }

    /**
     * Generate modify end point info.
     * 
     * @param endpointName the end point name
     * @param operation the operation
     * @param description the description
     * @param endpoint the end point element holding condiguration parameters
     * @return the modify end point info
     */
    private ModifyEndpointInfo handleGeneralInfo(final String endpointName, final String operation,
        final String description, final Endpoint endpoint) {
        ModifyEndpointInfo endpointInfo = new ModifyEndpointInfo();

        endpointInfo.setId(endpoint.getId());
        endpointInfo.setDescription(description);
        endpointInfo.setName(endpointName);
        endpointInfo.setOperationName(operation);
        return endpointInfo;
    }

    /**
     * Generate create end point info.
     * 
     * @param serviceId the service id
     * @param type the end point type
     * @param endpointName the end point name
     * @param operation the operation
     * @param description the description
     * @return the create end point info
     */
    private CreateEndpointInfo handleGeneralInfo(final String serviceId, final String type, final String endpointName,
        final String operation, final String description) {
        CreateEndpointInfo createEndpointInfo = new CreateEndpointInfo();
        createEndpointInfo.setConnectorId(type);
        createEndpointInfo.setDescription(description);
        createEndpointInfo.setName(endpointName);
        createEndpointInfo.setServiceId(serviceId);
        createEndpointInfo.setOperationName(operation);
        return createEndpointInfo;
    }

    /**
     * Add output parameter mappings to the modify end point info.
     * 
     * @param endpointInfo the modify end point info
     * @param outputParameterMapping the output parameter mappings
     */
    private void handleOutputParameterMapping(final ModifyEndpointInfo endpointInfo,
        final OutputParameterMapping outputParameterMapping) {
        if (outputParameterMapping != null) {
            List<OutputParameterMappingType> parameters = outputParameterMapping.getParameter();
            for (OutputParameterMappingType parameter : parameters) {
                endpointInfo.setOutputParameterMapping(parameter.getName(), parameter.getDataType(),
                    parameter.getValue());
            }
        }
    }

    /**
     * Add output parameter mappings to the create end point info.
     * 
     * @param createEndpointInfo the create end point info
     * @param outputParameterMapping the output parameter mappings
     */
    private void handleOutputParameterMapping(final CreateEndpointInfo createEndpointInfo,
        final OutputParameterMapping outputParameterMapping) {
        if (outputParameterMapping != null) {
            List<OutputParameterMappingType> parameters = outputParameterMapping.getParameter();
            for (OutputParameterMappingType parameter : parameters) {
                createEndpointInfo.setOutputParameterMapping(parameter.getName(), parameter.getDataType(),
                    parameter.getValue());
            }
        }
    }

    /**
     * Add input parameter mappings to the modify end point info.
     * 
     * @param endpointInfo the modify end point info
     * @param inputParameterMapping the input parameter mappings
     */
    private void handleInputParameterMapping(final ModifyEndpointInfo endpointInfo,
        final InputParameterMapping inputParameterMapping) {
        if (inputParameterMapping != null) {
            List<InputParameterMappingType> parameters = inputParameterMapping.getParameter();
            for (InputParameterMappingType parameter : parameters) {
                endpointInfo.setInputParameterMapping(parameter.getName(), parameter.getDataType(),
                    parameter.getMappingType(), parameter.getValue());
            }
        }
    }

    /**
     * Add input parameter mappings to the create end point info.
     * 
     * @param createEndpointInfo the create end point info
     * @param inputParameterMapping the input parameter mappings
     */
    private void handleInputParameterMapping(final CreateEndpointInfo createEndpointInfo,
        final InputParameterMapping inputParameterMapping) {
        if (inputParameterMapping != null) {
            List<InputParameterMappingType> parameters = inputParameterMapping.getParameter();
            for (InputParameterMappingType parameter : parameters) {
                createEndpointInfo.setInputParameterMapping(parameter.getName(), parameter.getDataType(),
                    parameter.getMappingType(), parameter.getValue());
            }
        }
    }

    /**
     * Add setting to the modify end point info.
     * 
     * @param endpointInfo the modify end point info
     * @param settings the settings
     */
    private void handleSettings(final ModifyEndpointInfo endpointInfo, final Settings settings) {
        if (settings != null) {
            List<Element> settingElements = settings.getAny();
            for (Element element : settingElements) {
                String name = element.getNodeName();
                String value = element.getTextContent();
                endpointInfo.setConfigParameterAsText(name, value);
            }
        }
    }

    /**
     * Add settings to the create end point info.
     * 
     * @param createEndpointInfo the create end point info
     * @param settings the settings
     */
    private void handleSettings(final CreateEndpointInfo createEndpointInfo, final Settings settings) {
        if (settings != null) {
            List<Element> settingElements = settings.getAny();
            for (Element element : settingElements) {
                String name = element.getNodeName();
                String value = element.getTextContent();
                createEndpointInfo.setConfigParameterAsText(name, value);
            }
        }
    }

    /**
     * Get an existing end point by name and type.
     * 
     * @param existingEndpoints all existing end points
     * @param endpointName the end point name
     * @param type the type
     * @return The exiting end point matching the name and type, or null if not found.s
     */
    private Endpoint getExistingEndpointByNameAndType(final List<Endpoint> existingEndpoints,
        final String endpointName, final String type) {
        Endpoint endpoint = null;
        if (existingEndpoints != null) {
            for (Endpoint existingEndpoint : existingEndpoints) {
                if (existingEndpoint.getConnectorId().equals(type) && existingEndpoint.getName().equals(endpointName)) {
                    // Modify existing endpoint
                    endpoint = existingEndpoint;
                    break;
                }
            }
        }
        return endpoint;
    }

}
