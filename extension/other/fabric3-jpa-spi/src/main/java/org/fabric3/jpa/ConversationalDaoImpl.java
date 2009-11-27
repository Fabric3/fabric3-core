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
package org.fabric3.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.osoa.sca.annotations.Scope;

/**
 * @version $Revision$ $Date$
 */
@Scope("CONVERSATION")
public class ConversationalDaoImpl<ENTITY, KEY> implements ConversationalDao<ENTITY, KEY> {

    protected EntityManager entityManager;

    public void close() {
        // No op
    }

    public ENTITY findById(Class<ENTITY> entityClass, KEY key) {
        return entityManager.find(entityClass, key);
    }

    public ENTITY merge(ENTITY entity) {
        return entityManager.merge(entity);
    }

    public void persist(ENTITY entity) {
        entityManager.persist(entity);
    }

    public void refresh(ENTITY entity) {
        entityManager.refresh(entity);
    }

    public void remove(ENTITY entity) {
        entityManager.remove(entity);
    }

    @SuppressWarnings("unchecked")
    public List<ENTITY> findByNamedQuery(String namedQuery, Class<ENTITY> entityClass, Object... args) {

        Query query = entityManager.createNamedQuery(namedQuery);
        int index = 0;
        for (Object arg : args) {
            query.setParameter(++index, arg);
        }
        return (List<ENTITY>) query.getResultList();
    }

    public void flush() {
        entityManager.flush();
    }

    public void lock(ENTITY entity, LockModeType lockModeType) {
        entityManager.lock(entity, lockModeType);
    }

}
