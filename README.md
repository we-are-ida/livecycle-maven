# Adobe LiveCycle Maven plugin


A Maven plugin to control various Adobe LiveCycle related actions through a Maven workflow.    


The available features (Maven Mojos) are:  


* **Configuration** Mojo: configures LiveCycle services   
* **Deploy DSC** Mojo: builds and deploys a DSC file (a custom LiveCycle component) to a LiveCycle server   
* **Deploy LCA** Mojo: deploys an LCA file (a LiveCycle application) to a LiveCycle server  
* **Copy Content Space** Mojo: recursilvely copies the content of one content space to another content space  
* **Ping** Mojo: mojo that can fail the build if the server cannot be contacted  
* **Generate Component XML** Mojo: generates a component.xml file from the comments in the source code    


Documentation
-------------

[Usage - general overview of the features](https://github.com/idamediafoundry/livecycle-maven/wiki/Usage)

[Application Deployment](https://github.com/idamediafoundry/livecycle-maven/wiki/LCA)

[Custom Component Deployment](https://github.com/idamediafoundry/livecycle-maven/wiki/DSC_Custom_Component)

[Auto Generation of component.xml](https://github.com/idamediafoundry/livecycle-maven/wiki/Component_XML_Generation)

[Configuration](https://github.com/idamediafoundry/livecycle-maven/wiki/Configuration)

[Copy Content Space](https://github.com/idamediafoundry/livecycle-maven/wiki/Content-space)

[Profiles - deploy to different environments](https://github.com/idamediafoundry/livecycle-maven/wiki/Profiles)

[Dependencies](https://github.com/idamediafoundry/livecycle-maven/wiki/Dependencies)


License
-------

Copyright 2012-2013 iDA MediaFoundry [www.ida-mediafoundry.be](http://www.ida-mediafoundry.be/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Credits
-------


Created by [iDA MediaFoundry](http://www.ida-mediafoundry.be/)

![iDA MediaFoundry](https://raw.github.com/wiki/idamediafoundry/livecycle-maven/img_ida_mediafoundry_logo.png "iDA MediaFoundry")
