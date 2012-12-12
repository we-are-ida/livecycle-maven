package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component.Services;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ConfigParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.FaultType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.InputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OutputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType.Faults;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.AutoDeploy;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.Operations;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ObjectFactory;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service;

public class DelegatingComponentGenerator<S, O, C, P, F> implements
		ComponentGenerator {
	private static final String JAXB_COMPONENT_CONTEXT_PATH = "be.idamediafoundry.sofa.livecycle.maven.component.configuration";
	private static final String COMPONENT_XSD_RESOURCE = "/component.xsd";

	private ComponentInfoExtractor<S, O, C, P, F> extractor;

	public DelegatingComponentGenerator(
			ComponentInfoExtractor<S, O, C, P, F> extractor) {
		this.extractor = extractor;
	}

	public void generateComponentXML(File outputFile, String sourcePath,
			String componentId, String version, String componentCategory)
			throws Exception {
		ObjectFactory objectFactory = new ObjectFactory();
		Component component = objectFactory.createComponent();
		extractor.populateComponent(component);

		// component.setComponentId(componentId);
		// component.setVersion(version);

		Services services = objectFactory.createComponentServices();
		List<Service> serviceList = services.getService();
		List<S> servicesInfo = extractor.getServicesInfo();
		for (S serviceInfo : servicesInfo) {
			Service service = objectFactory.createService();
			if (extractor.populateServices(service, serviceInfo)) {
				AutoDeploy autoDeploy = objectFactory.createServiceAutoDeploy();
				if (extractor.populateAutoDeploy(autoDeploy, serviceInfo)) {
					service.setAutoDeploy(autoDeploy);
				}

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

						OutputParameterType outputParameterType = objectFactory
								.createOutputParameterType();
						if (extractor.populateOutputParameter(
								outputParameterType, operationInfo)) {
							operation.getOutputParameter().add(
									outputParameterType);
						}
						operationsList.add(operation);
					}

				}
				if (!operationsList.isEmpty()) {
					service.setOperations(operations);
				}

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
}
