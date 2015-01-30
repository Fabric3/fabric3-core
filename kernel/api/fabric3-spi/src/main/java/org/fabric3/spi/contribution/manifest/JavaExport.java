/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.contribution.manifest;

import javax.xml.namespace.QName;

import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.oasisopen.sca.Constants;

/**
 * Represents an <code>export.java</code> entry in a contribution manifest.
 */
public class JavaExport implements Export {
    private static final long serialVersionUID = -1362112844218693711L;

    private static final QName QNAME = new QName(Constants.SCA_NS, "export.java");

    private PackageInfo packageInfo;
    private boolean resolved;

    public JavaExport(PackageInfo packageInfo) {
        if (packageInfo == null) {
            throw new IllegalStateException("Package info cannot be null");
        }
        this.packageInfo = packageInfo;
    }

    public QName getType() {
        return QNAME;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public boolean match(Import imprt) {
        if (imprt instanceof JavaImport) {
            JavaImport javaImport = (JavaImport) imprt;
            if (javaImport.getPackageInfo().matches(packageInfo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        resolved = true;
    }

    public String toString() {
        return packageInfo.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaExport that = (JavaExport) o;

        return !(packageInfo != null ? !packageInfo.equals(that.packageInfo) : that.packageInfo != null);

    }

    @Override
    public int hashCode() {
        return packageInfo != null ? packageInfo.hashCode() : 0;
    }
}

