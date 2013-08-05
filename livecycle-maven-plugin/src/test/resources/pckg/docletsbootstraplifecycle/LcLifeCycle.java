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

package pckg.docletsbootstraplifecycle;

import com.adobe.idp.dsc.component.ComponentContext;
import com.adobe.idp.dsc.component.LifeCycle;

/**
 * @author Gert
 *
 *  This class will be called by LiveCycle when you start or stop your component
 *
 */
public class LcLifeCycle implements LifeCycle {

    private String component;

    public void setComponentContext(ComponentContext componentContext) {
        this.component = componentContext.getComponent().getComponentId() + "-" +
                componentContext.getComponent().getVersion();
    }

    public void onStart() {
        System.out.println("Starting " + component);
    }

    public void onStop() {
        System.out.println("Stopping " + component);
    }
}
