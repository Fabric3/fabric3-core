(:
 Fabric3
 Copyright © 2008 Metaform Systems Limited

 This proprietary software may be used only connection with the Fabric3 license
 (the “License”), a copy of which is included in the software or may be
 obtained at: http://www.metaformsystems.com/licenses/license.html.

 Software distributed under the License is distributed on an “as is” basis,
 without warranties or conditions of any kind.  See the License for the
 specific language governing permissions and limitations of use of the software.
 This software is distributed in conjunction with other software licensed under
 different terms.  See the separate licenses for those programs included in the
 distribution for the permitted and restricted uses of such software.
:)
(:
    $Rev$ $Date$
:)

module namespace echoModule="http://fabric3.codehaus.org/xquery/test/module";

declare namespace echoService="sca:service:EchoService";
declare namespace java="sca:reference:java";
declare namespace message="sca:property:message";
declare namespace echo="http://www.example.org/echo";

declare variable $message:message external;

declare function echoService:hello($name) {
    <echo:message>{ concat(java:hello($name),$message:message) }</echo:message>
};
