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