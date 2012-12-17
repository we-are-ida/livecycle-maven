package be.idamediafoundry.sofa.livecycle.dsc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a method as being a factory method for the service. This method should
 * be public and static, and should return an instance of the service itself.
 * You don't need to use a factory method, in which case the service will be
 * initialized using the default constructor. This factory method is usefull in
 * cases where no default constructor exists, or a singleton needs to be
 * enforced.
 * 
 * @author Mike Seghers
 */
@Target(ElementType.METHOD)
public @interface FactoryMethod {

}
