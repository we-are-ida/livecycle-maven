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

public interface ComponentGenerator {

	/**
	 * Generate a component XML file based on the java code found at the source path and save it to the output file. Use
	 * componentId and version in the component XML required tags. The component category will be used as category of
	 * the generated services.
	 *
     * @param inputFile The original component XML file, if any.
	 * @param outputFile The output file to save the component XML in
	 * @throws Exception TODO you know what!
	 */
	void generateComponentXML(File inputFile, File outputFile)
			throws Exception;

}