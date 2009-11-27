I. INSTALLATION

The Fabric3 Tomcat runtime supports Tomcat 6.0.20 and later. To install the Fabric3 runtime:

1. Install Tomcat
2. Copy the contents of /lib to <Tomcat directory>/lib
3. Copy /fabric3 to  <Tomcat directory>/fabric3
4. Add the following to the Tomcat server.xml configuration:

  <Listener className="org.fabric3.runtime.tomcat.Fabric3Listener"/>

5. Start Tomcat

-----------------------------------------------------------------------

II. DEPLOYING APPLICATIONS

SCA contribution JARs and web applications are deployed to the Tomcat runtime by copying the archives to /fabric3/deploy.
Note that only standard, non-SCA web applications are deployed to the Tomcat /webapps directory. If a contribution JAR or
SCA web application WAR is copied to /webapps, it will not be processed by the Fabric3 runtime.   