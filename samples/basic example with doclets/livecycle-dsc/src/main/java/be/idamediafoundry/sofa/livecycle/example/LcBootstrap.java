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

package be.idamediafoundry.sofa.livecycle.example;

import com.adobe.idp.dsc.component.Bootstrap;
import com.adobe.idp.dsc.component.BootstrapContext;

/**
 * @author Gert
 *
 * This class will be called by LiveCycle when you install or uninstall your component
 */
public class LcBootstrap implements Bootstrap  {

    private String component;

    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.component = bootstrapContext.getComponent().getComponentId() + "-" +
                bootstrapContext.getComponent().getVersion();
    }

    public void onInstall() {
        System.out.println("Installing " + component);
    }

    public void onUnInstall() {
        System.out.println("Uninstalling " + component);
    }
}
