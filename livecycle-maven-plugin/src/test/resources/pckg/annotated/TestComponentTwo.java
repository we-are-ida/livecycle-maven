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

package pckg.annotated;

import be.idamediafoundry.sofa.livecycle.dsc.annotations.ConfigParam;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Operation;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Service;
import be.idamediafoundry.sofa.livecycle.dsc.annotations.Version;

@Service
public class TestComponentTwo {
	
	private String config;
	
	@Operation
	public String operation(String param) {
		return "string";
	}
	
	@ConfigParam
	public void setConfig(String config) {
		this.config = config;
	}
}