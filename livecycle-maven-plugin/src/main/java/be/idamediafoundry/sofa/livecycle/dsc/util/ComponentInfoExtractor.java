package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.util.List;

import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ConfigParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.FaultType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.InputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OutputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.AutoDeploy;

/**
 * Interface defining extraction methods to populate the component model. Only
 * simple attributes and text values should be populated, nested elements should
 * be populated in their own specific methods.
 * 
 * @author Mike Seghers
 * 
 */
public interface ComponentInfoExtractor<S, O, C, P, F> {
	List<S> getServicesInfo();
	List<O> getOperationsInfo(S serviceInfo);
	List<C> getConfigParametersInfo(S serviceInfo);
	List<P> getOperationInputParameters(O operationInfo);
	List<F> getOperationFaults(O operationInfo);
	
	
	void populateComponent(Component component);
	
	/**
	 * Implementation should populate the service's simple attributes and elements.
	 * @param service
	 */
	boolean populateServices(Service service, S serviceInfo);
	boolean populateAutoDeploy(Component component, AutoDeploy autoDeploy, S serviceInfo);
	
	boolean populateOperation(OperationType operation, O operationInfo, List<String> existingOperationNames);
	boolean populateInputParameter(InputParameterType inputParameter, O operationInfo, P parameterInfo);
	boolean populateOutputParameter(OutputParameterType outputParameter, O operationInfo);
	boolean populateFault(FaultType fault, O operationInfo, F faultInfo);
	
	boolean populateConfigParameter(ConfigParameterType configParameter, C configParameterInfo);
}
