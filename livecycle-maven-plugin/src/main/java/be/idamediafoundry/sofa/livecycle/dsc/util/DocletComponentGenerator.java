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

package be.idamediafoundry.sofa.livecycle.dsc.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

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

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

/**
 * Generates the component XML based on java source code, its java doc and xdoclet annotations.
 * 
 * You can and should use these tags in your java doc to override the default generated values:
 * 
 * <table>
 * 	<tr>
 *   <th>Doclet tag</th>
 *   <th>Class/Method</th>
 *   <th>Meaning</th>
 *   <th>Default</th>
 * 	</tr>
 * 	<tr>
 * 	 <td>@DSC</td>
 *   <td>Class</td>
 *   <td>Marks a class as being a service to be included as custom component. Only classes annotated with this doclet tag will be included as services!</td>
 *   <td>N/A</td>
 * 	</tr>
 * 	<tr>
 * 	 <td>@major</td>
 *   <td>Class</td>
 *   <td>Overrides the major configured component version for the specific service class. The value should be an integer!</td>
 *   <td>The first part of the configured component version (eg. if the version is 2.3, the major version is 2 by default)</td>
 * 	</tr>
 *  <tr>
 * 	 <td>@minor</td>
 *   <td>Class</td>
 *   <td>Overrides the minor configured component version for the specific service class. The value should be an integer!</td>
 *   <td>The second part of the configured component version (eg. if the version is 2.3, the minor version is 3 by default)</td>
 * 	</tr>
 * 	<tr>
 * 	 <td>@largeIcon</td>
 *   <td>Class</td>
 *   <td>The large icon that should be used for this service. The actual file should be a resource of the project, as it must be for custom component icons in general. The icon should ideally be 100x100 pixels.</td>
 *   <td>None</td>
 * 	</tr>
 * 	<tr>
 * 	 <td>@smallIcon</td>
 *   <td>Class</td>
 *   <td>The small icon that should be used for this service. The actual file should be a resource of the project, as it must be for custom component icons in general. The icon should ideally be 16x16 pixels.</td>
 *   <td>None</td>
 * 	</tr>
 *  <tr>
 * 	 <td>@operationName</td>
 *   <td>Method</td>
 *   <td>Overrides the default operation name derived from the method name. Operation names must be unique, a runtime exception will occur if duplicates are detected. In general, the eventual operation name is used to generate the title as well.</td>
 *   <td>The method name itself. If an overloaded method is detected, the following is done to avoid duplicates: The method name is appended with "With" and the parameter type for each parameter.</td>
 * 	</tr>
 *  <tr>
 * 	 <td>@param</td>
 *   <td>Method</td>
 *   <td>Uses the first part as the parameter name for an operation, and the second part as hint.</td>
 *   <td>N/A. In the current version you MUST have a  valid param doclet tag for each parameter, otherwise null pointer exceptions will be thrown.</td>
 * 	</tr>
 * <tr>
 * 	 <td>@outputParamName</td>
 *   <td>Method</td>
 *   <td>Overrides the default "out" as output parameter name.</td>
 *   <td>out</td>
 * 	</tr>
 * 	<tr>
 * 	 <td>@return</td>
 *   <td>Method</td>
 *   <td>The value is used as hint for the output parameter.</td>
 *   <td>None</td>
 * 	</tr>
 *  <tr>
 * 	 <td>@default</td>
 *   <td>Method (setter method)</td>
 *   <td>Specifies the default value for a configuration parameter of the service.</td>
 *   <td>None</td>
 * 	</tr>
 *  <tr>
 * 	 <td>@required</td>
 *   <td>Method (setter method)</td>
 *   <td>Specifies if a configuration parameter is required.</td>
 *   <td>false</td>
 * 	</tr>
 * 
 * </table>
 * 
 * @author Mike Seghers
 * @deprecated use the {@link DelegatingComponentGenerator} together with the {@link DocletDrivenQDoxComponentInfoExtractor} (still to be made)
 */
@Deprecated
public class DocletComponentGenerator implements ComponentGenerator {
	private static final String DEFAULT_OUT_PARAM_NAME = "out";
	private static final String JAXB_COMPONENT_CONTEXT_PATH = "be.idamediafoundry.sofa.livecycle.maven.component.configuration";
	private static final String COMPONENT_XSD_RESOURCE = "/component.xsd";

	private static final String DSC_TAG = "DSC";
	private static final String MAJOR_TAG = "major";
	private static final String MINOR_TAG = "minor";
	private static final String LARGE_ICON_TAG = "largeIcon";
	private static final String SMALL_ICON_TAG = "smallIcon";
	
	private static final String OPERATION_NAME_TAG = "operationName";
	private static final String PARAM_TAG = "param";
	private static final String OUTPUT_PARAM_NAME_TAG = "outputParamName";
	private static final String RETURN_TAG = "return";
	
	private static final String DEFAULT_TAG = "default";
	private static final String REQUIRED_TAG = "required";

	private Log log;

    public DocletComponentGenerator(Log log) {
		this.log = log;
	}

	/**
	 * {@inheritDoc}
	 */
    public void generateComponentXML(final File outputFile, final String sourcePath, final String componentId,
        final String version, final String componentCategory) throws Exception {

        ObjectFactory objectFactory = new ObjectFactory();
        Component component = objectFactory.createComponent();
        component.setComponentId(componentId);
        component.setVersion(version);

        Services services = objectFactory.createComponentServices();
        List<Service> serviceList = services.getService();

        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree(new File(sourcePath));

        JavaPackage[] packages = builder.getPackages();
        for (JavaPackage javaPackage : packages) {

            JavaClass[] classes = javaPackage.getClasses();
            for (JavaClass javaClass : classes) {
                if (javaClass.getTagByName(DSC_TAG) != null) {
                    generateServiceElement(objectFactory, serviceList, javaClass, componentCategory, version);
                }
            }
        }

        if (!serviceList.isEmpty()) {
            component.setServices(services);
        }

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(this.getClass().getResource(COMPONENT_XSD_RESOURCE));

        JAXBContext jaxbContext = JAXBContext
            .newInstance(JAXB_COMPONENT_CONTEXT_PATH);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setSchema(schema);
        marshaller.marshal(component, outputFile);
    }

    /**
     * Generates a service element from a java class.
     * 
     * @param objectFactory the object factory
     * @param serviceList the service list to add the generated service to
     * @param javaClass the java class
     * @param componentCategory the category
     * @param version The component version, used as default major/minor if not specified by doclet tags specifically
     */
    private void generateServiceElement(final ObjectFactory objectFactory, final List<Service> serviceList,
        final JavaClass javaClass, final String componentCategory, final String version) {
        if (!javaClass.isAbstract() && !javaClass.isInterface() && javaClass.isPublic()
            && !javaClass.isA("java.lang.Throwable")) {
            Service service = objectFactory.createService();
            service.setName(javaClass.getName());
            serviceList.add(service);

            service.setImplementationClass(javaClass.getFullyQualifiedName());

            AutoDeploy autoDeploy = objectFactory.createServiceAutoDeploy();
            autoDeploy.setServiceId(javaClass.getName());
            autoDeploy.setCategoryId(componentCategory);
            
            DocletTag major = javaClass.getTagByName(MAJOR_TAG);
            if (major != null) {
            	try {
            		autoDeploy.setMajorVersion(Integer.parseInt(major.getValue()));
            	} catch (NumberFormatException e) {
            		log.warn("The @major doclet tag should be an integer, but was \"" + major.getValue() + "\". This service will have a major version of 0!", e);
            	}
            } else {
            	String[] versionParts = version.split("\\.");
            	if (versionParts.length > 0) {
            		try {
            			int parsedMajor = Integer.parseInt(versionParts[0]);
                    	autoDeploy.setMajorVersion(parsedMajor);
                    	log.info("The @major doclet tag was not found on \"" + javaClass.getName() + "\", the major version was derived from the general component version: " + parsedMajor);	
                    } catch (NumberFormatException e) {
                    	log.info("The @major doclet tag was not found on \"" + javaClass.getName() + "\", and the specified general version \"" + version + "\" could not be parsed to detect this version. This service will have a major version of 0!");
                    }
            	} else {
            		log.info("The @major doclet tag was not found on \"" + javaClass.getName() + "\", and the specified general version \"" + version + "\" could not be parsed to detect this version. This service will have a major version of 0!");
            	}
            }
            
            DocletTag minor = javaClass.getTagByName(MINOR_TAG);
            if (minor != null) {
            	try {
            		autoDeploy.setMinorVersion(Integer.parseInt(minor.getValue()));
            	} catch (NumberFormatException e) {
            		log.warn("The @minor doclet tag should be an integer, but was \"" + minor.getValue() + "\". This service will have a minor version of 0!", e);
            	}
            } else {
            	String[] versionParts = version.split("\\.");
            	if (versionParts.length > 0) {
            		try {
            			int parsedMinor = Integer.parseInt(versionParts[1]);
                    	autoDeploy.setMinorVersion(parsedMinor);
                    	log.info("The @minor doclet tag was not found on \"" + javaClass.getName() + "\", the minor version was derived from the general component version: " + parsedMinor);	
                    } catch (NumberFormatException e) {
                    	log.info("The @minor doclet tag was not found on \"" + javaClass.getName() + "\", and the specified general version \"" + version + "\" could not be parsed to detect this version. This service will have a minor version of 0!");
                    }
            	} else {
            		log.info("The @minor doclet tag was not found on \"" + javaClass.getName() + "\", and the specified general version \"" + version + "\" could not be parsed to detect this version. This service will have a minor version of 0!");
            	}
            }
            
            
            service.setAutoDeploy(autoDeploy);

            Operations operations = objectFactory.createServiceOperations();
            List<OperationType> operationList = operations.getOperation();

            JavaMethod[] methods = javaClass.getMethods();
            for (JavaMethod javaMethod : methods) {
                generateOperationOrConfigurationElement(objectFactory, service, operationList, javaMethod);
            }

            if (!operationList.isEmpty()) {
                service.setOperations(operations);
            }

            DocletTag smallIconTag = javaClass.getTagByName(SMALL_ICON_TAG);
            DocletTag largeIconTag = javaClass.getTagByName(LARGE_ICON_TAG);

            if (smallIconTag != null) {
                service.setSmallIcon(smallIconTag.getValue());
            }

            if (largeIconTag != null) {
                service.setLargeIcon(largeIconTag.getValue());
            }
            
            
        } else {
            throw new RuntimeException(
                "You should not annotate this class with @DSC. Only public non-abstract classes are supported.");
        }
    }

    /**
     * Generate an operation or configuration element for a java method.
     * 
     * @param objectFactory the object factory
     * @param service the service to add configuration elements to
     * @param operationList the list of operations to add operations to
     * @param javaMethod the java method
     */
    private void generateOperationOrConfigurationElement(final ObjectFactory objectFactory, final Service service,
        final List<OperationType> operationList, final JavaMethod javaMethod) {
        Type methodResultType = javaMethod.getReturnType();

        if (javaMethod.isPropertyMutator()) {
            // When setter -> configuration parameter...
            ConfigParameterType configurationParameter = generateConfigurationElement(objectFactory, javaMethod);

            service.getConfigParameter().add(configurationParameter);
        } else if (javaMethod.isPublic() && methodResultType != null && !javaMethod.isPropertyAccessor()) {
            // When public, non-getter, non-constructor
            OperationType operation = generateOperationElement(objectFactory, operationList, javaMethod);

            operationList.add(operation);
        }
    }

    /**
     * Generate an operation element for the java method.
     * 
     * @param objectFactory the object factory
     * @param operationList the operation list, to check if overloaded methods are present
     * @param javaMethod the java method
     * @return the operation element
     */
    private OperationType generateOperationElement(final ObjectFactory objectFactory,
        final List<OperationType> operationList, final JavaMethod javaMethod) {

        OperationType operation = objectFactory.createOperationType();

        generateOperationNameMethodTitle(operationList, javaMethod, operation);

        // Define input paramters for the operation
        generateInputParameters(objectFactory, operation, javaMethod);

        generateOutputParameters(objectFactory, operation, javaMethod);

        generateFaultElements(objectFactory, operation, javaMethod);

        String comment = javaMethod.getComment();
        operation.setHint(comment);
        return operation;
    }

    /**
     * Generate fault elements for declared exceptions on the method.
     * 
     * @param objectFactory the object factory
     * @param operation the operation element to put the foult tag on
     * @param javaMethod the java method
     */
    private void generateFaultElements(final ObjectFactory objectFactory, final OperationType operation,
        final JavaMethod javaMethod) {
        Type[] exceptions = javaMethod.getExceptions();
        if (exceptions != null) {
            Faults faults = objectFactory.createOperationTypeFaults();
            List<FaultType> faultList = faults.getFault();

            Map<String, String> exceptionTagMap = getCommentMapForTag(javaMethod, "throws");

            for (Type type : exceptions) {
                FaultType fault = objectFactory.createFaultType();
                String name = type.getJavaClass().getName();
                fault.setName(name);
                fault.setType(type.getFullyQualifiedName());
                fault.setTitle(generateTitle(name));
                fault.setHint(exceptionTagMap.get(name));
                faultList.add(fault);
            }

            if (!faultList.isEmpty()) {
                operation.setFaults(faults);
            }
        }
    }

    /**
     * Generate the output parameters based on the method signature. At the moment, only the return value is looked at.
     * Future releases might provide a way to support more output parameters.
     * 
     * @param objectFactory the object factory
     * @param operation the operation to put the output parameter elements on
     * @param javaMethod the java method
     */
    private void generateOutputParameters(final ObjectFactory objectFactory, final OperationType operation,
        final JavaMethod javaMethod) {
        Type methodResultType = javaMethod.getReturnType();
        if (!methodResultType.equals(Type.VOID)) {
            // TODO support more output parameters.
            OutputParameterType outputParameterType = objectFactory.createOutputParameterType();
            DocletTag outputParamNameDocletTag = javaMethod.getTagByName(OUTPUT_PARAM_NAME_TAG);
            String outputParameterName = outputParamNameDocletTag == null ? DEFAULT_OUT_PARAM_NAME : outputParamNameDocletTag.getValue();
            outputParameterType.setName(outputParameterName);
            outputParameterType.setTitle(outputParameterName);
            outputParameterType.setType(getFullyQualifiedJavaType(methodResultType));

            DocletTag returnDocletTag = javaMethod.getTagByName(RETURN_TAG);
            if (returnDocletTag != null) {
                String comment = returnDocletTag.getValue();
                outputParameterType.setHint(comment);
            }

            operation.getOutputParameter().add(outputParameterType);
        }
    }

    /**
     * Generate the input parameters based on the java method signature.
     * 
     * @param objectFactory the object factory
     * @param operation the operation to put the input parameter elements on
     * @param javaMethod the java method
     */
    private void generateInputParameters(final ObjectFactory objectFactory, final OperationType operation,
        final JavaMethod javaMethod) {
        JavaParameter[] parameters = javaMethod.getParameters();
        Map<String, String> paramTagMap = getCommentMapForTag(javaMethod, PARAM_TAG);

        for (JavaParameter javaParameter : parameters) {
            InputParameterType inputParameterType = objectFactory.createInputParameterType();
            inputParameterType.setName(javaParameter.getName());
            inputParameterType.setType(getFullyQualifiedJavaType(javaParameter.getType()));

            String comment = paramTagMap.get(javaParameter.getName());
            inputParameterType.setHint(comment);
            inputParameterType.setTitle(generateTitle(javaParameter.getName()));

            operation.getInputParameter().add(inputParameterType);
        }
    }

    /**
     * Generate the operation name, method and title attributes. This method will check for duplicates (overloaded
     * methods) and generate a different method name for these overloaded methods, also setting the method attribute in
     * the process (otherwise not needed). If the method seems to be overloaded, then the operationName tag in the
     * javadoc is looked up. If it does not exist, a long name will be generated using the concatenated method name and
     * parameter names and their types.
     * 
     * @param operationList the operation list to check for overloaded methods
     * @param javaMethod the java method
     * @param operation the operation
     */
    private void generateOperationNameMethodTitle(final List<OperationType> operationList, final JavaMethod javaMethod,
        final OperationType operation) {
        JavaParameter[] parameters = javaMethod.getParameters();
        String methodName = javaMethod.getName();
        String operationName;
        DocletTag operationNameTag = javaMethod.getTagByName(OPERATION_NAME_TAG);
        if (isOverloadedMethod(methodName, operationList)) {
            // An overloaded method has been found, we will need to generate a name
            // Let's see if the developer specified his preference
            if (operationNameTag != null) {
                // Yes, he did!
                if (isOverloadedMethod(operationNameTag.getValue(), operationList)) {
                    throw new RuntimeException(
                        "Could not generate component XML, the method "
                            + methodName
                            + " in class "
                            + javaMethod.getParentClass().getName()
                            + " has no unique operation name, please check your @operationName tag and make sure you specify a unique name");
                }
                operationName = operationNameTag.getValue();
            } else {
                // Generate one, using the parameter names and types
                StringBuilder generated = new StringBuilder(methodName);
                generated.append("With");

                for (JavaParameter javaParameter : parameters) {
                    generated.append(StringUtils.capitalize(javaParameter.getName()));
                    generated.append("As");
                    generated.append(StringUtils.capitalize(javaParameter.getType().getJavaClass().getName()));
                }
                operationName = generated.toString();
                if (isOverloadedMethod(operationName, operationList)) {
                    throw new RuntimeException(
                        "Could not generate component XML, the system could not generate a unique operation name for method "
                            + methodName + " in class " + javaMethod.getParentClass().getName()
                            + ", please specify an @operationName tag and make sure you specify a unique name");
                }
            }
            operation.setMethod(methodName);
        } else {
            if (operationNameTag != null) {
            	operationName = operationNameTag.getValue();
            } else {
            	operationName = methodName;
            }
        }
        operation.setName(operationName);
        operation.setTitle(generateTitle(operationName));
    }

    /**
     * Get the comments on a doc tag in a map (key is the first value in the doc tag, the rest is the value).
     * 
     * @param javaMethod the java method in which to look for the tag
     * @param tagName the name of the tag
     * @return a map of comments for a certain tag name
     */
    private Map<String, String> getCommentMapForTag(final JavaMethod javaMethod, final String tagName) {
        DocletTag[] paramTags = javaMethod.getTagsByName(tagName);
        Map<String, String> paramTagMap = new HashMap<String, String>();
        for (DocletTag docletTag : paramTags) {
            String value = docletTag.getValue();
            paramTagMap.put(docletTag.getParameters()[0], value.substring(value.indexOf(' ') + 1));
        }
        return paramTagMap;
    }

    /**
     * Generate an appropriate title for an element holding "title". The title of an element is shown in the workbench
     * as label for the operation, configuration, input, output and fault elements. This method will make a sentence of
     * a camel cased string, transform it to lower case and finally capitalize the first letter again.
     * 
     * @param base the camel cased string
     * @return the sentence
     */
    private String generateTitle(final String base) {
        return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(base), ' ')
            .toLowerCase());
    }

    /**
     * Generate a configuration parameter for a service based on a mutator.
     * 
     * @param objectFactory the object factory
     * @param javaMethod the mutator
     * @return the configuration parameter element
     */
    private ConfigParameterType generateConfigurationElement(final ObjectFactory objectFactory,
        final JavaMethod javaMethod) {
        String comment = javaMethod.getComment();
        ConfigParameterType configurationParameter = objectFactory.createConfigParameterType();
        String propertyName = javaMethod.getPropertyName();
        if (propertyName.length() > 100) {
            // Following spec: name must be no larger then 100 characters
            configurationParameter.setProperty(propertyName);
            propertyName = propertyName.substring(0, 100);
        }

        configurationParameter.setName(propertyName);
        configurationParameter.setType(getFullyQualifiedJavaType(javaMethod.getPropertyType()));
        configurationParameter.setHint(comment);
        configurationParameter.setTitle(generateTitle(propertyName));

        if (javaMethod.getTagByName(REQUIRED_TAG) != null) {
            configurationParameter.setRequired(Boolean.TRUE);
        }

        if (javaMethod.getTagByName(DEFAULT_TAG) != null) {
            configurationParameter.setDefaultValue(javaMethod.getTagByName(DEFAULT_TAG).getValue());
        }
        return configurationParameter;
    }

    /**
     * Get the fully qualified name from a Type object. If the type is a generic, java.lang.Object is returned.
     * 
     * @param type the type
     * @return the fully qualified name
     */
    private String getFullyQualifiedJavaType(final Type type) {
        String strType;
        if (type.isResolved()) {
            strType = type.getFullyQualifiedName();
        } else {
            strType = "java.lang.Object";
        }
        return strType;
    }

    /**
     * Check if the method is overloading another method.
     * 
     * @param methodName the method name
     * @param operationList the operation list of already recorded operations
     * @return true if the method name is already in the list
     */
    private boolean isOverloadedMethod(final String methodName, final List<OperationType> operationList) {
        boolean overloaded = false;
        for (OperationType otherOperation : operationList) {
            if (otherOperation.getName().equals(methodName)) {
                overloaded = true;
                break;
            }
        }
        return overloaded;
    }

    /**
     * Main method to quickly test this generator.
     * 
     * @param args the arguments
     * @throws Exception when something fails
     */
    public static void main(final String[] args) throws Exception {
    	Log log = new SystemStreamLog();
        ComponentGenerator generator = new DocletComponentGenerator(log);
        generator.generateComponentXML(new File(
            "/Customers/Cronos/Project/livecycle-custom/dsc-common/target/classes/component.xml"),
            "/Customers/Cronos/Project/livecycle-custom/dsc-common/src/main/java",
            "be.idamediafoundry.sofa.livecycle.CommonUtilities", "2.3 ", "iDA");
    }
}
