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

import java.io.File;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import be.idamediafoundry.sofa.livecycle.maven.component.configuration.OperationType;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;
import org.apache.maven.plugin.logging.Log;

/**
 * Extracts component info from java classes using the QDox framework.
 * 
 * @author Mike Seghers
 * 
 */
public abstract class AbstractQDoxComponentInfoExtractor
		implements
		ComponentInfoExtractor<JavaClass, JavaMethod, JavaMethod, JavaParameter, Type> {
	protected static final String DEFAULT_OUT_PARAM_NAME = "out";
	protected static final String RETURN_TAG = "return";
	protected static final String PARAM_TAG = "param";

	private JavaDocBuilder builder;
    private Log log;

	public AbstractQDoxComponentInfoExtractor(String sourcePath, Log log) {
		this.builder = new JavaDocBuilder();
		this.builder.addSourceTree(new File(sourcePath));
        this.log = log;
	}

	final public List<JavaClass> getServicesInfo() {
		List<JavaClass> result = new ArrayList<JavaClass>();

		JavaPackage[] packages = builder.getPackages();
		for (JavaPackage javaPackage : packages) {

			JavaClass[] classes = javaPackage.getClasses();
			for (JavaClass javaClass : classes) {
				if (acceptAsService(javaClass)) {
					result.add(javaClass);
				}
			}
		}
		return result;
	}

    final public JavaClass lookUpJavaClassImplementing(Class<?> javaInterface) {
        JavaPackage[] packages = builder.getPackages();
        for (JavaPackage javaPackage : packages) {
            JavaClass[] classes = javaPackage.getClasses();
            for (JavaClass javaClass : classes) {
                if (javaClassImplements(javaClass, javaInterface)) {
                    return javaClass;
                }
            }
        }
        return null;
    }

    private boolean javaClassImplements(JavaClass actualClass, Class<?> javaInterface) {
        Type[]  types = actualClass.getImplements();
        int i = 0;
        while(i < types.length) {
            Type type = types[i];
            String fQname = getFullyQualifiedJavaType(type);
            if(fQname.matches(javaInterface.getCanonicalName())) {
                return true;
            }
            i++;
        }
        return false;
    }

	final public List<JavaMethod> getOperationsInfo(JavaClass serviceInfo) {
		List<JavaMethod> result = new ArrayList<JavaMethod>();
		JavaMethod[] methods = serviceInfo.getMethods();
		for (JavaMethod javaMethod : methods) {
			if (acceptAsOperation(javaMethod)) {
				result.add(javaMethod);
			}
		}
		return result;
	}

	final public List<JavaMethod> getConfigParametersInfo(JavaClass serviceInfo) {
		List<JavaMethod> result = new ArrayList<JavaMethod>();
		JavaMethod[] methods = serviceInfo.getMethods();
		for (JavaMethod javaMethod : methods) {
			if (acceptAsConfigParameter(javaMethod)) {
				result.add(javaMethod);
			}
		}
		return result;
	}

	final public List<JavaParameter> getOperationInputParameters(
			JavaMethod operationInfo) {
		List<JavaParameter> result = new ArrayList<JavaParameter>();
		JavaParameter[] parameters = operationInfo.getParameters();
		for (JavaParameter javaParameter : parameters) {
			result.add(javaParameter);
		}
		return result;
	}

	final public List<Type> getOperationFaults(JavaMethod operationInfo) {
		List<Type> result = new ArrayList<Type>();
		Type[] exceptions = operationInfo.getExceptions();
		for (Type exception : exceptions) {
			result.add(exception);
		}
		return result;
	}

	public abstract boolean acceptAsService(JavaClass javaClass);

	public abstract boolean acceptAsOperation(JavaMethod javaMethod);

	public abstract boolean acceptAsConfigParameter(JavaMethod javaMethod);

	/**
	 * Generate the operation name, method and title attributes. This method
	 * will check for duplicates (overloaded methods) and generate a different
	 * method name for these overloaded methods, also setting the method
	 * attribute in the process (otherwise not needed). If the method seems to
	 * be overloaded, then the operationName tag in the javadoc is looked up. If
	 * it does not exist, a long name will be generated using the concatenated
	 * method name and parameter names and their types.
	 * 
	 * @param operationList
	 *            the operation list to check for overloaded methods
	 * @param javaMethod
	 *            the java method
	 * @param operation
	 *            the operation
	 */
	final protected void generateOperationNameMethodTitle(
			final List<String> existingOperationNames,
			final JavaMethod javaMethod, final OperationType operation,
			String suggestedName) {
		JavaParameter[] parameters = javaMethod.getParameters();
		String methodName = javaMethod.getName();
		String operationName;

		if (existingOperationNames.contains(methodName)) {
			// An overloaded method has been found, we will need to generate a
			// name
			// Let's see if the developer specified his preference
			if (StringUtils.isNotBlank(suggestedName)) {
				// Yes, he did!
				if (existingOperationNames.contains(suggestedName)) {
					throw new RuntimeException(
							"Could not generate component XML, the method "
									+ methodName
									+ " in class "
									+ javaMethod.getParentClass().getName()
									+ " has no unique operation name, please check your definition and make sure you specify a unique name");
				}
				operationName = suggestedName;
			} else {
				// Generate one, using the parameter names and types
				StringBuilder generated = new StringBuilder(methodName);
				generated.append("With");

				for (JavaParameter javaParameter : parameters) {
					generated.append(StringUtils.capitalize(javaParameter
							.getName()));
					generated.append("As");
					generated.append(StringUtils.capitalize(javaParameter
							.getType().getJavaClass().getName()));
				}
				operationName = generated.toString();
				if (existingOperationNames.contains(operationName)) {
					throw new RuntimeException(
							"Could not generate component XML, the system could not generate a unique operation name for method "
									+ methodName
									+ " in class "
									+ javaMethod.getParentClass().getName()
									+ ", please check your definition and make sure you specify a unique name");
				}
			}
			operation.setMethod(methodName);
		} else {
			if (StringUtils.isNotBlank(suggestedName)) {
				operationName = suggestedName;
			} else {
				operationName = methodName;
			}
		}
		operation.setName(operationName);
		operation.setTitle(generateTitle(operationName));
	}

	/**
	 * Generate an appropriate title for an element holding "title". The title
	 * of an element is shown in the workbench as label for the operation,
	 * configuration, input, output and fault elements. This method will make a
	 * sentence of a camel cased string, transform it to lower case and finally
	 * capitalize the first letter again.
	 * 
	 * @param base
	 *            the camel cased string
	 * @return the sentence
	 */
	final protected String generateTitle(final String base) {
		return StringUtils.capitalize(StringUtils.join(
				StringUtils.splitByCharacterTypeCamelCase(base), ' ')
				.toLowerCase());
	}

	/**
	 * Get the fully qualified name from a Type object. If the type is a
	 * generic, java.lang.Object is returned.
	 * 
	 * @param type
	 *            the type
	 * @return the fully qualified name
	 */
	final protected String getFullyQualifiedJavaType(final Type type) {
		String strType;
		if (type.isResolved()) {
			strType = type.getFullyQualifiedName();
		} else {
			strType = "java.lang.Object";
		}
		return strType;
	}

	/**
	 * Get the comments on a doc tag in a map (key is the first value in the doc
	 * tag, the rest is the value).
	 * 
	 * @param javaMethod
	 *            the java method in which to look for the tag
	 * @param tagName
	 *            the name of the tag
	 * @return a map of comments for a certain tag name
	 */
	final protected Map<String, String> getCommentMapForTag(
			final JavaMethod javaMethod, final String tagName) {
		DocletTag[] paramTags = javaMethod.getTagsByName(tagName);
		Map<String, String> paramTagMap = new HashMap<String, String>();
		for (DocletTag docletTag : paramTags) {
			String value = docletTag.getValue();
			paramTagMap.put(docletTag.getParameters()[0],
					value.substring(value.indexOf(' ') + 1));
		}
		return paramTagMap;
	}

    final protected Log getLog() {
        return log;
    }

    final protected String getFirstSentence(String text) {
        String result = text;
        if (text != null) {
            BreakIterator iterator = BreakIterator.getSentenceInstance();
            iterator.setText(text);
            int start = iterator.first();
            int end = iterator.next();
            if (end != BreakIterator.DONE) {
                result = text.substring(start, end).trim();
            }
        }
        return result;
    }
}
