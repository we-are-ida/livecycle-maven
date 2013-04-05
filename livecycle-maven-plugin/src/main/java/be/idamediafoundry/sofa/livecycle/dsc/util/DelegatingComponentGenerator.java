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

package be.idamediafoundry.sofa.livecycle.dsc.util;

import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component.Services;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ConfigParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.FaultType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.InputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ObjectFactory;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType.Faults;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OutputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.AutoDeploy;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.Operations;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DelegatingComponentGenerator<S, O, C, P, F> implements
        ComponentGenerator {
    private static final String JAXB_COMPONENT_CONTEXT_PATH = "be.idamediafoundry.sofa.livecycle.maven.component.configuration";
    private static final String COMPONENT_XSD_RESOURCE = "/component.xsd";

    private ComponentInfoExtractor<S, O, C, P, F> extractor;

    public DelegatingComponentGenerator(
            ComponentInfoExtractor<S, O, C, P, F> extractor) {
        this.extractor = extractor;
    }

    public void generateComponentXML(File inputFile, File outputFile)
            throws Exception {
        ObjectFactory objectFactory = new ObjectFactory();
        Component component;
        if (inputFile != null && inputFile.exists()) {
            JAXBContext context = JAXBContext.newInstance(Component.class);
            component = (Component) context.createUnmarshaller().unmarshal(inputFile);
        } else {
            component = objectFactory.createComponent();
        }


        extractor.populateComponent(component);

        // component.setComponentId(componentId);
        // component.setVersion(version);

        Services services = objectFactory.createComponentServices();
        List<Service> serviceList = services.getService();
        List<S> servicesInfo = extractor.getServicesInfo();
        for (S serviceInfo : servicesInfo) {
            Service service = objectFactory.createService();
            if (extractor.populateServices(service, serviceInfo)) {
                addAutoDeployElement(objectFactory, component, serviceInfo, service);

                addOperationBlock(objectFactory, serviceInfo, service);

                addConfigurationParameters(objectFactory, serviceInfo, service);

                serviceList.add(service);
            }
        }

        if (!serviceList.isEmpty()) {
            component.setServices(services);
        }

        SchemaFactory sf = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(this.getClass().getResource(
                COMPONENT_XSD_RESOURCE));

        JAXBContext jaxbContext = JAXBContext
                .newInstance(JAXB_COMPONENT_CONTEXT_PATH);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setSchema(schema);
        marshaller.marshal(component, outputFile);
    }

    private void addConfigurationParameters(ObjectFactory objectFactory,
                                            S serviceInfo, Service service) {
        List<ConfigParameterType> configParameter = service
                .getConfigParameter();
        List<C> configParamsInfo = extractor
                .getConfigParametersInfo(serviceInfo);
        for (C configParameterInfo : configParamsInfo) {
            ConfigParameterType configParam = objectFactory
                    .createConfigParameterType();
            if (extractor.populateConfigParameter(configParam,
                    configParameterInfo)) {
                configParameter.add(configParam);
            }
        }
    }

    private void addOperationBlock(ObjectFactory objectFactory, S serviceInfo,
                                   Service service) {
        List<O> operationsInfo = extractor
                .getOperationsInfo(serviceInfo);
        Operations operations = objectFactory.createServiceOperations();
        List<OperationType> operationsList = operations.getOperation();
        List<String> existingOperationNames = new ArrayList<String>();
        for (O operationInfo : operationsInfo) {
            OperationType operation = objectFactory
                    .createOperationType();
            if (extractor.populateOperation(operation, operationInfo,
                    existingOperationNames)) {
                existingOperationNames.add(operation.getName());

                addInputParameters(objectFactory, operationInfo, operation);

                addFaults(objectFactory, operationInfo, operation);

                addOutputParameters(objectFactory, operationInfo, operation);
                operationsList.add(operation);
            }

        }
        if (!operationsList.isEmpty()) {
            service.setOperations(operations);
        }
    }

    private void addOutputParameters(ObjectFactory objectFactory,
                                     O operationInfo, OperationType operation) {
        OutputParameterType outputParameterType = objectFactory
                .createOutputParameterType();
        if (extractor.populateOutputParameter(
                outputParameterType, operationInfo)) {
            operation.getOutputParameter().add(
                    outputParameterType);
        }
    }

    private void addFaults(ObjectFactory objectFactory, O operationInfo,
                           OperationType operation) {
        Faults faults = objectFactory
                .createOperationTypeFaults();
        List<FaultType> faultList = faults.getFault();
        List<F> operationFaults = extractor
                .getOperationFaults(operationInfo);
        for (F operationFault : operationFaults) {
            FaultType fault = objectFactory.createFaultType();
            if (extractor.populateFault(fault, operationInfo,
                    operationFault)) {
                faultList.add(fault);
            }
        }
        if (!faultList.isEmpty()) {
            operation.setFaults(faults);
        }
    }

    private void addInputParameters(ObjectFactory objectFactory,
                                    O operationInfo, OperationType operation) {
        List<P> operationInputParameters = extractor
                .getOperationInputParameters(operationInfo);
        List<InputParameterType> inputParameterList = operation
                .getInputParameter();
        for (P operationInputParameter : operationInputParameters) {
            InputParameterType inputParam = objectFactory
                    .createInputParameterType();
            if (extractor.populateInputParameter(inputParam,
                    operationInfo, operationInputParameter)) {
                inputParameterList.add(inputParam);
            }
        }
    }

    private void addAutoDeployElement(ObjectFactory objectFactory,
                                      Component component, S serviceInfo, Service service) {
        AutoDeploy autoDeploy = objectFactory.createServiceAutoDeploy();
        if (extractor.populateAutoDeploy(component, autoDeploy, serviceInfo)) {
            service.setAutoDeploy(autoDeploy);
        }
    }
}
