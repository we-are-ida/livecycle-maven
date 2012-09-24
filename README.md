Adobe LiveCycle Maven plugin

A Maven plugin to control various Adobe LiveCycle-related actions through a Maven workflow.   

Adobe LiveCycle has a Java API which allows to perform several service operations, such as programmatically deploying a LiveCycle application and so on, instead of going through the LiveCycle Administration Console. This Maven plugin builds on top of the Adobe LiveCycle Java API, and enables you to integrate several LiveCycle operations in your Maven work flows.  
This is especially handy if your Adobe LiveCycle project is part of a bigger (Java-based) system where you already use Maven to orchestrate the builds and deployments.  

The plugin facilitates deployment on different environments through the use of Maven profiles.  


The available mojos are:
* Configuration Mojo: configures LiveCycle services   
* Deploy DSC Mojo: builds and deploys a DSC file (a custom LiveCycle component) to a LiveCycle server   
* Deploy LCA Mojo: deploys an LCA file (a LiveCycle application) to a LiveCycle server  
* Copy Content Space Mojo: recursilvely copies the content of one content space to another content space  
* Ping Mojo: mojo that can fail the build if the server cannot be contacted  
* Generate Component XML Mojo: generates a component.xml file from the comments in the source code    


Created by iDA MediaFoundry

