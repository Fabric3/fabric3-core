/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.admin.interpreter.command;

import java.io.PrintStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.management.contribution.ArtifactErrorInfo;
import org.fabric3.management.contribution.ErrorInfo;
import org.fabric3.management.contribution.InvalidContributionException;

/**
 * @version $Rev$ $Date$
 */
public class CommandHelper {

    private CommandHelper() {
    }

    /**
     * Derives a contribution name from a URL by selecting the path part following the last '/'.
     *
     * @param contribution the contribution URL
     * @return the contribution name
     */
    public static URI parseContributionName(URL contribution) {
        String contributionName;
        String path = contribution.getPath();
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            contributionName = path;
        } else if (pos == path.length() - 1) {
            String substr = path.substring(0, pos);
            pos = substr.lastIndexOf('/');
            if (pos < 0) {
                contributionName = substr;
            } else {
                contributionName = path.substring(pos + 1, path.length() - 1);
            }
        } else {
            contributionName = path.substring(pos + 1);
        }
        return URI.create(contributionName);
    }

    public static void printErrors(PrintStream out, InvalidContributionException e) {
        for (ErrorInfo info : e.getErrors()) {
            if (info instanceof ArtifactErrorInfo) {
                ArtifactErrorInfo aei = (ArtifactErrorInfo) info;
                out.println("Errors in " + aei.getName() + " \n");
                for (ErrorInfo errorInfo : aei.getErrors()) {
                    out.println("  " + errorInfo.getError() + "\n");
                }
            } else {
                out.println(info.getError());
            }
        }

    }
}
