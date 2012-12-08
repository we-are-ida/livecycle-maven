package be.idamediafoundry.sofa.livecycle.dsc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Overrides the configured component version for the specific service class.
 * 
 * @author Mike Seghers
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface Version {
	/**
	 * The major version override.
	 */
	int major() default -1;
	/**
	 * The minor version override.
	 */
	int minor() default -1;
}
