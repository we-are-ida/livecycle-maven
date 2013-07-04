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
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ConfigParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.FaultType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.InputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OutputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * @author mike
 */
public class DocletDrivenQDoxComponentInfoExtractor extends AbstractQDoxComponentInfoExtractor {
    private static final String MAJOR_TAG = "major";
    private static final String MINOR_TAG = "minor";
    private static final String LARGE_ICON_TAG = "largeIcon";
    private static final String SMALL_ICON_TAG = "smallIcon";
    private static final String FACTORY_METHOD_TAG = "factoryMethod";
    private static final String OUTPUT_PARAM_NAME_TAG = "outputParamName";
    private static final String DEFAULT_TAG = "default";
    private static final String REQUIRED_TAG = "required";
    private static final String OPERATION_NAME_TAG = "operationName";


    public DocletDrivenQDoxComponentInfoExtractor(String sourcePath, Log log) {
        super(sourcePath, log);
    }

    @Override
    public boolean acceptAsService(JavaClass javaClass) {
        //Naive implementation, kept as is for reverse compatibility (takes all public non-abstract, non-exception classes as service)
        return (!javaClass.isAbstract() && !javaClass.isInterface() && javaClass.isPublic()
                && !javaClass.isA("java.lang.Throwable"));
    }

    @Override
    public boolean acceptAsOperation(JavaMethod javaMethod) {
        Type methodResultType = javaMethod.getReturnType();
        DocletTag factoryMethod = javaMethod.getTagByName(FACTORY_METHOD_TAG);

        return javaMethod.isPublic() && methodResultType != null && !javaMethod.isPropertyAccessor() && !javaMethod.isPropertyMutator() && factoryMethod == null;
    }

    @Override
    public boolean acceptAsConfigParameter(JavaMethod javaMethod) {
        return javaMethod.isPropertyMutator();
    }

    public void populateComponent(Component component) {
        String bootStrapClass = super.lookUpLCBootstrapClass();
        if(StringUtils.isNotBlank(bootStrapClass))  {
            component.setBootstrapClass(bootStrapClass);
        }

        String lifeCycleClass = super.lookUpLCLifeCycleClass();
        if(StringUtils.isNotBlank(lifeCycleClass)) {
            component.setLifecycleClass(lifeCycleClass);
        }
    }

    public boolean populateServices(Service service, JavaClass serviceInfo) {
        service.setName(serviceInfo.getName());
        service.setImplementationClass(serviceInfo.getFullyQualifiedName());

        DocletTag smallIconTag = serviceInfo.getTagByName(SMALL_ICON_TAG);
        DocletTag largeIconTag = serviceInfo.getTagByName(LARGE_ICON_TAG);

        if (smallIconTag != null) {
            service.setSmallIcon(smallIconTag.getValue());
        }

        if (largeIconTag != null) {
            service.setLargeIcon(largeIconTag.getValue());
        }

        String comment = serviceInfo.getComment();
        service.setHint(getFirstSentence(comment));
        service.setDescription(comment);

        // Find factory method.

        JavaMethod factoryMethod = findDocletOnMethods(serviceInfo,
                FACTORY_METHOD_TAG);
        if (factoryMethod != null) {
            if (factoryMethod.isAbstract() || factoryMethod.isConstructor()
                    || !factoryMethod.isPublic()
                    || factoryMethod.isPropertyAccessor()
                    || factoryMethod.isPropertyMutator()
                    || !factoryMethod.isStatic()) {
                throw new IllegalStateException(
                        "You should not annotate "
                                + factoryMethod.getName()
                                + " as FactoryMethod, it is not a valid factory method!");
            }

            service.setFactoryMethod(factoryMethod.getName());

        }

        return true;
    }

    public boolean populateAutoDeploy(Component component, Service.AutoDeploy autoDeploy, JavaClass serviceInfo) {
        autoDeploy.setServiceId(serviceInfo.getName());
        autoDeploy.setCategoryId(component.getComponentId());

        DocletTag major = serviceInfo.getTagByName(MAJOR_TAG);
        if (major != null) {
            try {
                autoDeploy.setMajorVersion(Integer.parseInt(major.getValue()));
            } catch (NumberFormatException e) {
                getLog().warn("The @major doclet tag should be an integer, but was \"" + major.getValue() + "\". This service will have a major version of 0!", e);
            }
        }

        DocletTag minor = serviceInfo.getTagByName(MINOR_TAG);
        if (minor != null) {
            try {
                autoDeploy.setMinorVersion(Integer.parseInt(minor.getValue()));
            } catch (NumberFormatException e) {
                getLog().warn("The @minor doclet tag should be an integer, but was \"" + minor.getValue() + "\". This service will have a minor version of 0!", e);
            }
        }

        return true;
    }

    public boolean populateOperation(OperationType operation, JavaMethod operationInfo, List<String> existingOperationNames) {
        DocletTag operationNameTag = operationInfo.getTagByName(OPERATION_NAME_TAG);
        String suggestedName = (operationNameTag == null ? null : operationNameTag.getValue());

        generateOperationNameMethodTitle(existingOperationNames, operationInfo, operation, suggestedName);

        DocletTag operationSmallIconTag = operationInfo.getTagByName(SMALL_ICON_TAG);

        if(operationSmallIconTag != null && StringUtils.isNotBlank(operationSmallIconTag.getValue())) {
            operation.setSmallIcon(operationSmallIconTag.getValue());
        }

        DocletTag operationLargeIconTag = operationInfo.getTagByName(LARGE_ICON_TAG);

        if(operationLargeIconTag != null && StringUtils.isNotBlank(operationLargeIconTag.getValue())) {
            operation.setLargeIcon(operationLargeIconTag.getValue());
        }

        String comment = operationInfo.getComment();
        operation.setHint(comment);
        return true;
    }

    public boolean populateInputParameter(InputParameterType inputParameter, JavaMethod operationInfo, JavaParameter parameterInfo) {
        Map<String, String> paramTagMap = getCommentMapForTag(operationInfo,
                PARAM_TAG);

        inputParameter.setName(parameterInfo.getName());
        inputParameter.setType(getFullyQualifiedJavaType(parameterInfo.getType()));

        String comment = paramTagMap.get(parameterInfo.getName());
        inputParameter.setHint(getFirstSentence(comment));
        inputParameter.setDescription(comment);
        inputParameter.setTitle(generateTitle(parameterInfo.getName()));

        return true;
    }

    public boolean populateOutputParameter(OutputParameterType outputParameter, JavaMethod operationInfo) {
        Type methodResultType = operationInfo.getReturnType();
        if (!methodResultType.equals(Type.VOID)) {
            DocletTag outputParamNameDocletTag = operationInfo.getTagByName(OUTPUT_PARAM_NAME_TAG);
            String outputParameterName = outputParamNameDocletTag == null ? DEFAULT_OUT_PARAM_NAME : outputParamNameDocletTag.getValue();
            outputParameter.setName(outputParameterName);
            outputParameter.setTitle(outputParameterName);
            outputParameter.setType(getFullyQualifiedJavaType(methodResultType));

            DocletTag returnDocletTag = operationInfo.getTagByName(RETURN_TAG);
            if (returnDocletTag != null) {
                String comment = returnDocletTag.getValue();
                outputParameter.setHint(comment);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean populateFault(FaultType fault, JavaMethod operationInfo, Type faultInfo) {
        Map<String, String> exceptionTagMap = getCommentMapForTag(operationInfo,
                "throws");

        String name = faultInfo.getJavaClass().getName();
        fault.setName(name);
        fault.setType(faultInfo.getFullyQualifiedName());
        fault.setTitle(generateTitle(name));
        String comment = exceptionTagMap.get(name);
        fault.setHint(getFirstSentence(comment));
        fault.setDescription(comment);

        return true;
    }

    public boolean populateConfigParameter(ConfigParameterType configParameter, JavaMethod configParameterInfo) {
        String comment = configParameterInfo.getComment();
        String propertyName = configParameterInfo.getPropertyName();
        if (propertyName.length() > 100) {
            // Following spec: name must be no larger then 100 characters
            configParameter.setProperty(propertyName);
            propertyName = propertyName.substring(0, 100);
        }

        configParameter.setName(propertyName);
        configParameter.setType(getFullyQualifiedJavaType(configParameterInfo.getPropertyType()));
        configParameter.setHint(comment);
        configParameter.setTitle(generateTitle(propertyName));

        if (configParameterInfo.getTagByName(REQUIRED_TAG) != null) {
            configParameter.setRequired(Boolean.TRUE);
        }

        if (configParameterInfo.getTagByName(DEFAULT_TAG) != null) {
            configParameter.setDefaultValue(configParameterInfo.getTagByName(DEFAULT_TAG).getValue());
        }
        return true;
    }

    private JavaMethod findDocletOnMethods(JavaClass javaClass, String doclet) {
        JavaMethod result = null;
        JavaMethod[] methods = javaClass.getMethods();
        for (JavaMethod javaMethod : methods) {
            DocletTag tag = javaMethod.getTagByName(doclet);
            if (tag != null) {
                result = javaMethod;
                break;
            }
        }
        return result;
    }
}
