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
