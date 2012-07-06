I. INSTALLATION

The Fabric3 Tomcat runtime supports Tomcat 7 and later. To install the Fabric3 runtime:

1. Install Tomcat
2. Copy /fabric3 to <Tomcat directory>/fabric3
3. Deploy the Fabric3 web application in fabric3/webapp to Tomcat
4. Start Tomcat

-----------------------------------------------------------------------

II. DEPLOYING APPLICATIONS

SCA contribution JARs and web applications are deployed to the Tomcat runtime by copying the archives to /fabric3/runtimes/<runtime name>/deploy.
Note that only standard, non-SCA web applications are deployed to the Tomcat /webapps directory. If a contribution JAR or SCA web application WAR
is copied to /webapps, it will not be processed by the Fabric3 runtime.