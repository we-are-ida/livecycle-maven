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
 * Optional annotation on mutators for making it required and / or to set a
 * default.
 * 
 * @author Mike Seghers
 */
@Target(ElementType.METHOD)
public @interface ConfigParam {
	/**
	 * Specifies the default value for a configuration parameter of the service.
	 */
	String defaultValue() default "";

	/**
	 * Specifies if a configuration parameter is required.
	 */
	boolean required() default false;
}
