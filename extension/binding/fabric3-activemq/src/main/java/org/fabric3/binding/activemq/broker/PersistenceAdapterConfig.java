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
package org.fabric3.binding.activemq.broker;

/**
 * Encapsulates persistence adapter configuration for a broker.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceAdapterConfig {
    private Long checkpointInterval;
    private Long cleanupInterval;
    private boolean disableLocking;
    private int indexBinSize;
    private int indexKeySize;
    private int indexPageSize;

    enum Type {
        AMQ, JDBC, JOURNAL, KAHA, MEMORY
    }

    private Type type;
    private boolean syncOnWrite;
    private String maxFileLength;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isSyncOnWrite() {
        return syncOnWrite;
    }

    public void setSyncOnWrite(boolean syncOnWrite) {
        this.syncOnWrite = syncOnWrite;
    }

    public String getMaxFileLength() {
        return maxFileLength;
    }

    public void setMaxFileLength(String maxFileLength) {
        this.maxFileLength = maxFileLength;
    }

    public Long getCheckpointInterval() {
        return checkpointInterval;
    }

    public void setCheckpointInterval(Long checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }

    public Long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public boolean isDisableLocking() {
        return disableLocking;
    }

    public void setDisableLocking(boolean disableLocking) {
        this.disableLocking = disableLocking;
    }

    public int getIndexBinSize() {
        return indexBinSize;
    }

    public void setIndexBinSize(int indexBinSize) {
        this.indexBinSize = indexBinSize;
    }

    public int getIndexKeySize() {
        return indexKeySize;
    }

    public void setIndexKeySize(int indexKeySize) {
        this.indexKeySize = indexKeySize;
    }

    public int getIndexPageSize() {
        return indexPageSize;
    }

    public void setIndexPageSize(int indexPageSize) {
        this.indexPageSize = indexPageSize;
    }
}
