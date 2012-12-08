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
}
