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

package pckg.annotatedbootstraplifecycle;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;

/**
 * Test component sentence one. Test component sentence two. 
 * 
 * Paragraph.
 * 
 * @author Mike Seghers
 */
@Service(largeIcon = "large.ico", smallIcon = "small.ico", version = @Version(major = 2, minor = 4), categoryId = "service-cat")
public class TestComponentOne {
	
	/**
	 * Config param.
	 */
	private String config;
	
	/**
	 * Operation java doc sentence one. Operation java doc sentence two.
	 * 
	 * Paragraph.
	 *
	 * @param param Operation parameter java doc
	 * @return Return java doc
	 */
	@Operation(name = "operationOverride", outputName = "outOverride", smallIcon = "small.ico", largeIcon = "large.ico")
	public String operation(String param) {
		return "string";
	}
	
	/**
	 * Setter java doc sentence one. Setter java doc sentence two.
	 * 
	 * Paragraph.
	 * 
	 * @param config Setter parameter java doc
	 */
	@ConfigParam(defaultValue = "testDefault", required = true)
	public void setConfig(String config) {
		this.config = config;
	}
}