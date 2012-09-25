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
 * Generates the component XML based on java source code.
 * 
 * TODO: provide indepth taglet info
 * 
 * @author Mike Seghers
 */
public class ComponentGenerator {

    /**
     * Generate a compoent XML file based on the java code found at the source path and save it to the output file. Use
     * componentId and version in the component XML required tags. The component category will be used as category of
     * the generated services.
     * 
     * @param outputFile The output file to save the component XML in
     * @param sourcePath the source path where the java code is located
     * @param componentId the component id
     * @param version the component version
     * @param componentCategory the component category
     * @throws Exception TODO you know what!
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
                if (javaClass.getTagByName("DSC") != null) {
                    generateServiceElement(objectFactory, serviceList, javaClass, componentCategory);
                }
            }
        }

        if (!serviceList.isEmpty()) {
            component.setServices(services);
        }

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(this.getClass().getResource("/component.xsd"));

        JAXBContext jaxbContext = JAXBContext
            .newInstance("be.idamediafoundry.sofa.livecycle.maven.component.configuration");
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
     */
    private void generateServiceElement(final ObjectFactory objectFactory, final List<Service> serviceList,
        final JavaClass javaClass, final String componentCategory) {
        if (!javaClass.isAbstract() && !javaClass.isInterface() && javaClass.isPublic()
            && !javaClass.isA("java.lang.Throwable")) {
            Service service = objectFactory.createService();
            service.setName(javaClass.getName());
            serviceList.add(service);

            service.setImplementationClass(javaClass.getFullyQualifiedName());

            AutoDeploy autoDeploy = objectFactory.createServiceAutoDeploy();
            autoDeploy.setServiceId(javaClass.getName());
            autoDeploy.setCategoryId(componentCategory);
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

            DocletTag smallIconTag = javaClass.getTagByName("smallIcon");
            DocletTag largeIconTag = javaClass.getTagByName("largeIcon");

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
        Type methodResultType = javaMethod.getReturns();

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
        Type methodResultType = javaMethod.getReturns();
        if (!methodResultType.equals(Type.VOID)) {
            // TODO support more output parameters.
            OutputParameterType outputParameterType = objectFactory.createOutputParameterType();
            DocletTag outputParamNameDocletTag = javaMethod.getTagByName("outputParamName");
            String outputParameterName = outputParamNameDocletTag == null ? "out" : outputParamNameDocletTag.getValue();
            outputParameterType.setName(outputParameterName);
            outputParameterType.setTitle(outputParameterName);
            outputParameterType.setType(getFullyQualifiedJavaType(methodResultType));

            DocletTag returnDocletTag = javaMethod.getTagByName("returns");
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
        Map<String, String> paramTagMap = getCommentMapForTag(javaMethod, "param");

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
        if (isOverloadedMethod(methodName, operationList)) {
            // An overloaded method has been found, we will need to generate a name
            // Let's see if the developer specified his preference
            DocletTag operationNameTag = javaMethod.getTagByName("operationName");
            if (operationNameTag != null) {
                // Yes, he did!
                if (isOverloadedMethod(operationNameTag.getValue(), operationList)) {
                    throw new RuntimeException(
                        "Could not generate component XML, the method "
                            + methodName
                            + " in class "
                            + javaMethod.getParentClass().getName()
                            + " has no uniqe operation name, please check your @operationName tag and make sure you specify a unique name");
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
                        "Could not generate component XML, the system could not generate a uniqe operation name for method "
                            + methodName + " in class " + javaMethod.getParentClass().getName()
                            + ", please specify an @operationName tag and make sure you specify a unique name");
                }
            }
            operation.setMethod(methodName);
        } else {
            operationName = methodName;
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

        if (javaMethod.getTagByName("required") != null) {
            configurationParameter.setRequired(Boolean.TRUE);
        }

        if (javaMethod.getTagByName("default") != null) {
            configurationParameter.setDefaultValue(javaMethod.getTagByName("default").getValue());
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
        ComponentGenerator generator = new ComponentGenerator();
        generator.generateComponentXML(new File(
            "/Customers/Cronos/Project/SoFa/trunk/livecycle/dsc-common/target/classes/component.xml"),
            "/Customers/Cronos/Project/SoFa/trunk/livecycle/dsc-common/src/main/java",
            "be.idamediafoundry.sofa.livecycle.CommonUtilities", "1.0", "iDA");
    }

}
