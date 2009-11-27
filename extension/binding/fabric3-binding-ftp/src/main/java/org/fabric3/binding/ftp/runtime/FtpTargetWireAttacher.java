/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.ftp.runtime;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.net.SocketFactory;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class FtpTargetWireAttacher implements TargetWireAttacher<FtpTargetDefinition> {
    private ExpressionExpander expander;
    private FtpInterceptorMonitor monitor;

    public FtpTargetWireAttacher(@Reference ExpressionExpander expander, @Monitor FtpInterceptorMonitor monitor) {
        this.expander = expander;
        this.monitor = monitor;
    }

    public void attach(PhysicalSourceDefinition source, FtpTargetDefinition target, Wire wire) throws WiringException {

        InvocationChain invocationChain = wire.getInvocationChains().iterator().next();
        URI uri = expandUri(target.getUri());
        try {
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 23 : uri.getPort();
            InetAddress hostAddress = "localhost".equals(host) ? InetAddress.getLocalHost() : InetAddress.getByName(host);

            String remotePath = uri.getPath();
            String tmpFileSuffix = target.getTmpFileSuffix();

            FtpSecurity security = expandFtpSecurity(target.getSecurity());
            boolean active = target.isActive();
            int connectTimeout = target.getConectTimeout();
            SocketFactory factory = new ExpiringSocketFactory(connectTimeout);
            int socketTimeout = target.getSocketTimeout();
            List<String> cmds = target.getSTORCommands();
            FtpTargetInterceptor targetInterceptor =
                    new FtpTargetInterceptor(hostAddress, port, security, active, socketTimeout, factory, cmds, monitor);
            targetInterceptor.setTmpFileSuffix(tmpFileSuffix);
            targetInterceptor.setRemotePath(remotePath);

            invocationChain.addInterceptor(targetInterceptor);
        } catch (UnknownHostException e) {
            throw new WiringException(e);
        }

    }

    public void detach(PhysicalSourceDefinition source, FtpTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(FtpTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    /**
     * Expands the target URI if it contains an expression of the form ${..}.
     *
     * @param uri the target uri to expand
     * @return the expanded URI with sourced values for any expressions
     * @throws WiringException if there is an error expanding an expression
     */
    private URI expandUri(URI uri) throws WiringException {
        try {
            String decoded = URLDecoder.decode(uri.toString(), "UTF-8");
            return URI.create(expander.expand(decoded));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

    /**
     * Expands the FTP security if it contains an expression of the form ${..}.
     *
     * @param ftpSecurity FTP security which contains FTP authentication details
     * @return the expanded ftp security
     * @throws WiringException if there is an error expanding an expression
     */
    private FtpSecurity expandFtpSecurity(FtpSecurity ftpSecurity) throws WiringException {
        try {
            return new FtpSecurity(expander.expand(ftpSecurity.getUser()), expander.expand(ftpSecurity.getPassword()));
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

}
