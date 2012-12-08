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
