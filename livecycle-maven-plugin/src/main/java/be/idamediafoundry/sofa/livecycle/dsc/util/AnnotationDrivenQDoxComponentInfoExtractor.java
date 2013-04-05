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

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.FactoryMethod;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Component;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.ConfigParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.FaultType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.InputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OutputParameterType;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service;
import be.idamediafoundry.sofa.livecycle.maven.component.configuration.Service.AutoDeploy;
import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;
import com.thoughtworks.qdox.model.annotation.AnnotationConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class AnnotationDrivenQDoxComponentInfoExtractor extends
        AbstractQDoxComponentInfoExtractor {

    public AnnotationDrivenQDoxComponentInfoExtractor(String sourcePath, Log log) {
        super(sourcePath, log);
    }

    public void populateComponent(Component component) {
        //TODO we might look for bootstrap and livecycle classes...
    }

    public boolean populateServices(Service service, JavaClass javaClass) {
        service.setName(javaClass.getName());
        service.setImplementationClass(javaClass.getFullyQualifiedName());

        be.idamediafoundry.sofa.livecycle.dsc.annotations.Service serviceAnnotation = findAnnotation(
                javaClass,
                be.idamediafoundry.sofa.livecycle.dsc.annotations.Service.class);
        if (StringUtils.isNotBlank(serviceAnnotation.smallIcon())) {
            service.setSmallIcon(serviceAnnotation.smallIcon());
        }
        if (StringUtils.isNotBlank(serviceAnnotation.largeIcon())) {
            service.setLargeIcon(serviceAnnotation.largeIcon());
        }

        String comment = javaClass.getComment();
        service.setHint(getFirstSentence(comment));
        service.setDescription(comment);

        // Find factory method.
        JavaMethod factoryMethod = findAnnotationOnMethods(javaClass,
                FactoryMethod.class);
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

        if (serviceAnnotation.requestProcessingStrategy() != be.idamediafoundry.sofa.livecycle.dsc.annotations.Service.RequestProcessingStrategy.NONE) {
            service.setRequestProcessingStrategy(serviceAnnotation.requestProcessingStrategy().name());
        }
        return true;
    }

    public boolean populateAutoDeploy(Component component, AutoDeploy autoDeploy, JavaClass javaClass) {
        be.idamediafoundry.sofa.livecycle.dsc.annotations.Service serviceAnnotation = findAnnotation(
                javaClass,
                be.idamediafoundry.sofa.livecycle.dsc.annotations.Service.class);
        if (serviceAnnotation.autoDeploy()) {
            autoDeploy.setServiceId(javaClass.getName());
            autoDeploy.setCategoryId(StringUtils.isBlank(serviceAnnotation.categoryId()) ? component.getComponentId() : serviceAnnotation.categoryId());

            Version versionAnnotation = serviceAnnotation.version();

            if (versionAnnotation != null) {
                if (versionAnnotation.major() > -1) {
                    autoDeploy.setMajorVersion(versionAnnotation.major());
                }
                if (versionAnnotation.minor() > -1) {
                    autoDeploy.setMinorVersion(versionAnnotation.minor());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean populateOperation(OperationType operation,
                                     JavaMethod javaMethod, List<String> existinOperationNames) {
        Operation operationAnnotation = findAnnotation(javaMethod,
                Operation.class);
        String suggestedName = operationAnnotation == null ? null
                : operationAnnotation.name();
        if (StringUtils.isBlank(suggestedName)) {
            suggestedName = null;
        }
        generateOperationNameMethodTitle(existinOperationNames, javaMethod,
                operation, suggestedName);

        String comment = javaMethod.getComment();
        operation.setHint(getFirstSentence(comment));
        operation.setDescription(comment);

        return true;
    }

    public boolean populateConfigParameter(ConfigParameterType configParameter,
                                           JavaMethod javaMethod) {
        String comment = javaMethod.getComment();
        String propertyName = javaMethod.getPropertyName();
        if (propertyName.length() > 100) {
            // Following spec: name must be no larger then 100 characters
            configParameter.setProperty(propertyName);
            propertyName = propertyName.substring(0, 100);
        }

        configParameter.setName(propertyName);
        configParameter.setType(getFullyQualifiedJavaType(javaMethod
                .getPropertyType()));
        configParameter.setHint(getFirstSentence(comment));
        configParameter.setDescription(comment);
        configParameter.setTitle(generateTitle(propertyName));

        ConfigParam configParam = findAnnotation(javaMethod, ConfigParam.class);
        if (configParam != null) {
            configParameter.setRequired(configParam.required());
            if (StringUtils.isNotBlank(configParam.defaultValue())) {
                configParameter.setDefaultValue(configParam.defaultValue());
            }
        }

        return true;
    }

    public boolean populateInputParameter(InputParameterType inputParameter,
                                          JavaMethod javaMethod, JavaParameter javaParameter) {
        Map<String, String> paramTagMap = getCommentMapForTag(javaMethod,
                PARAM_TAG);

        inputParameter.setName(javaParameter.getName());
        inputParameter.setType(getFullyQualifiedJavaType(javaParameter
                .getType()));

        String comment = paramTagMap.get(javaParameter.getName());
        inputParameter.setHint(getFirstSentence(comment));
        inputParameter.setDescription(comment);
        inputParameter.setTitle(generateTitle(javaParameter.getName()));
        return true;
    }

    public boolean populateOutputParameter(OutputParameterType outputParameter,
                                           JavaMethod javaMethod) {
        boolean result = false;
        Type methodResultType = javaMethod.getReturnType();
        if (!methodResultType.equals(Type.VOID)) {
            String outputParameterName = DEFAULT_OUT_PARAM_NAME;
            Operation operationAnnotation = findAnnotation(javaMethod,
                    Operation.class);
            if (operationAnnotation != null) {
                if (StringUtils.isNotBlank(operationAnnotation.outputName())) {
                    outputParameterName = operationAnnotation.outputName();
                }
            }
            outputParameter.setName(outputParameterName);
            outputParameter.setTitle(outputParameterName);
            outputParameter
                    .setType(getFullyQualifiedJavaType(methodResultType));

            DocletTag returnDocletTag = javaMethod.getTagByName(RETURN_TAG);
            if (returnDocletTag != null) {
                String comment = returnDocletTag.getValue();
                outputParameter.setHint(getFirstSentence(comment));
                outputParameter.setDescription(comment);
            }
            result = true;
        }
        return result;
    }

    public boolean populateFault(FaultType fault, JavaMethod javaMethod,
                                 Type exceptionType) {
        Map<String, String> exceptionTagMap = getCommentMapForTag(javaMethod,
                "throws");

        String name = exceptionType.getJavaClass().getName();
        fault.setName(name);
        fault.setType(exceptionType.getFullyQualifiedName());
        fault.setTitle(generateTitle(name));
        String comment = exceptionTagMap.get(name);
        fault.setHint(getFirstSentence(comment));
        fault.setDescription(comment);

        return true;
    }

    @Override
    public boolean acceptAsService(JavaClass javaClass) {
        boolean accept = false;

        be.idamediafoundry.sofa.livecycle.dsc.annotations.Service serviceAnnotation = findAnnotation(
                javaClass,
                be.idamediafoundry.sofa.livecycle.dsc.annotations.Service.class);
        if (serviceAnnotation != null) {
            if (!javaClass.isAbstract() && !javaClass.isInterface()
                    && javaClass.isPublic()
                    && !javaClass.isA("java.lang.Throwable")) {
                accept = true;
            } else {
                throw new RuntimeException(
                        "You should not annotate this class with @Service. Only public non-abstract classes are supported (exceptions excluded).");
            }
        }

        return accept;
    }

    @Override
    public boolean acceptAsOperation(JavaMethod javaMethod) {
        Type methodResultType = javaMethod.getReturnType();
        FactoryMethod factoryMethod = findAnnotation(javaMethod, FactoryMethod.class);

        return javaMethod.isPublic() && methodResultType != null
                && !javaMethod.isPropertyAccessor()
                && !javaMethod.isPropertyMutator()
                && factoryMethod == null;
    }

    @Override
    public boolean acceptAsConfigParameter(JavaMethod javaMethod) {
        return javaMethod.isPropertyMutator();
    }

    /**
     * Find an annotation of a given type on the given entity.
     *
     * @param entity The entity on which we are looking for the annotation
     * @param type   the type of the annotation
     * @return the annotation of the given type on the given entity, or null if
     *         none is found.
     */
    private <T extends java.lang.annotation.Annotation> T findAnnotation(
            AbstractJavaEntity entity, Class<T> type) {
        Annotation[] annotations = entity.getAnnotations();
        T result = null;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.getType().getFullyQualifiedName()
                        .equals(type.getName())) {
                    result = convertToJavaLang(annotation, type);
                    break;
                }
            }
        }
        return result;
    }

    private JavaMethod findAnnotationOnMethods(JavaClass javaClass,
                                               Class<? extends java.lang.annotation.Annotation> annotationClass) {
        JavaMethod result = null;
        JavaMethod[] methods = javaClass.getMethods();
        for (JavaMethod javaMethod : methods) {
            java.lang.annotation.Annotation factoryMethod = findAnnotation(
                    javaMethod, annotationClass);
            if (factoryMethod != null) {
                result = javaMethod;
                break;
            }
        }
        return result;
    }

    private <T extends java.lang.annotation.Annotation> T convertToJavaLang(
            final Annotation annotation, final Class<T> expectedType) {
        try {
            final Class<?> annotationClass = Class.forName(annotation.getType()
                    .getFullyQualifiedName());
            @SuppressWarnings("unchecked")
            T proxy = (T) Proxy.newProxyInstance(this.getClass()
                    .getClassLoader(), new Class[]{annotationClass},
                    new InvocationHandler() {

                        public Object invoke(Object instance, Method method,
                                             Object[] args) throws Throwable {
                            if (method.getName().equals("toString")) {
                                return "Proxied annotation of type "
                                        + annotationClass;
                            } else if (method.getName().equals("getClass")) {
                                return annotationClass;
                            }

                            Object value = annotation.getProperty(method
                                    .getName());
                            if (value == null) {
                                return method.getDefaultValue();
                            }
                            if (value instanceof Annotation) {
                                java.lang.annotation.Annotation sub = convertToJavaLang(
                                        (Annotation) value,
                                        java.lang.annotation.Annotation.class);
                                return sub;
                            } else {
                                AnnotationConstant constant = (AnnotationConstant) value;
                                value = constant.getValue();
                                return value;
                            }
                        }
                    });

            return proxy;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "The source code is annotated with a class that could not be found on your project's classpath, please fix this!",
                    e);
        }
    }

}
