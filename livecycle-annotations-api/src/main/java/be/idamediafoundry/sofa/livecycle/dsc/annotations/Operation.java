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
 * Optional annotation on public DSC Service methods. You should add this
 * annotation on methods that need an default name override and/or a default
 * output name override.
 * 
 * @author Mike Seghers
 */
@Target(ElementType.METHOD)
public @interface Operation {
	/**
	 * Overrides the default operation name derived from the method name.
	 * Operation names must be unique, a runtime exception will occur if
	 * duplicates are detected. In general, the eventual operation name is used
	 * to generate the title as well.
	 */
	String name() default "";

	/**
	 * Overrides the default "out" as output parameter name.
	 */
	String outputName() default "";

    /**
     * The small icon that should be used for this operation. The actual file
     * should be a resource of the project, as it must be for custom component
     * icons in general. The icon should ideally be 16x16 pixels.
     */
    String smallIcon() default "";

    /**
     * The large icon that should be used for this operation. The actual file
     * should be a resource of the project, as it must be for custom component
     * icons in general. The icon should ideally be 100x100 pixels.
     */
    String largeIcon() default "";
}
