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

package be.idamediafoundry.sofa.livecycle.dsc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a class as being a service to be included as custom component. Only
 * classes annotated with this annotation will be included as services in the
 * component xml! For further reading, please check the adobe documentation.
 * 
 * @author Mike Seghers
 * 
 */
@Target(ElementType.TYPE)
public @interface Service {


    public enum RequestProcessingStrategy {
		NONE, SINGLE_INSTANCE, INSTANCE_PER_REQUEST, POOLED_INSTANCE;
	}

	/**
	 * The small icon that should be used for this service. The actual file
	 * should be a resource of the project, as it must be for custom component
	 * icons in general. The icon should ideally be 16x16 pixels.
	 */
	String smallIcon() default "";

	/**
	 * The large icon that should be used for this service. The actual file
	 * should be a resource of the project, as it must be for custom component
	 * icons in general. The icon should ideally be 100x100 pixels.
	 */
	String largeIcon() default "";

    /**
     * Flag to indicate whether the auto-deploy element should be generated or not.
     */
    boolean autoDeploy() default true;

	/**
	 * Overrides the configured component version for the specific service
	 * class.
	 */
	Version version() default @Version();

    /**
     * The id of the category in which the service should be deployed.
     */
    String categoryId() default "";

	/**
	 * The request processing strategy for this service.
	 */
	RequestProcessingStrategy requestProcessingStrategy() default RequestProcessingStrategy.NONE;
}
