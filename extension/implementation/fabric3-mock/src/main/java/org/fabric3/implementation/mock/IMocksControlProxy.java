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
package org.fabric3.implementation.mock;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;
import org.easymock.IMocksControl;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class IMocksControlProxy implements IMocksControl {

    private IMocksControl delegate;
    private Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

    @Init
    public void init() {
        delegate = EasyMock.createControl();
    }

    public void checkOrder(boolean state) {
        delegate.checkOrder(state);
    }

    public <T> T createMock(Class<T> toMock) {
        Object mock = mocks.get(toMock);
        if (mock == null) {
            mock = delegate.createMock(toMock);
            mocks.put(toMock, mock);
        }
        return toMock.cast(mock);
    }

    public void replay() {
        delegate.replay();
    }

    public void reset() {
        delegate.reset();
        mocks.clear();
    }

    public void verify() {
        delegate.verify();
    }

    @SuppressWarnings({"unchecked"})
    public IExpectationSetters andAnswer(IAnswer answer) {
        return delegate.andAnswer(answer);
    }

    @SuppressWarnings({"unchecked"})
    public IExpectationSetters andReturn(Object value) {
        return delegate.andReturn(value);
    }

    @SuppressWarnings({"unchecked"})
    public void andStubAnswer(IAnswer answer) {
        delegate.andStubAnswer(answer);
    }

    public void andStubReturn(Object value) {
        delegate.andStubReturn(value);
    }

    public void andStubThrow(Throwable throwable) {
        delegate.andStubThrow(throwable);
    }

    public IExpectationSetters andThrow(Throwable throwable) {
        return delegate.andThrow(throwable);
    }

    public IExpectationSetters anyTimes() {
        return delegate.anyTimes();
    }

    public void asStub() {
        delegate.asStub();
    }

    public IExpectationSetters atLeastOnce() {
        return delegate.atLeastOnce();
    }

    public IExpectationSetters once() {
        return delegate.once();
    }

    public IExpectationSetters times(int count) {
        return delegate.times(count);
    }

    public IExpectationSetters times(int min, int max) {
        return delegate.times(min, max);
    }

}
