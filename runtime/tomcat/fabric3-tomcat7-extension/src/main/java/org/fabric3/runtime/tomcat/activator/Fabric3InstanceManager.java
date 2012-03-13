package org.fabric3.runtime.tomcat.activator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;

public class Fabric3InstanceManager implements InstanceManager {

	private Map<String, List<Injector<?>>> injectorMappings;
	private ClassLoader contextClassLoader;

	public Fabric3InstanceManager(Map<String, List<Injector<?>>> injectors, ClassLoader classLoader) {
		 this.injectorMappings = injectors;
		 this.contextClassLoader = classLoader;
	}

	public void destroyInstance(Object o) throws IllegalAccessException,
			InvocationTargetException {	}

	public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException {
		return inject(newInstance(className,contextClassLoader));
	}

	public void newInstance(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
		    inject(instance);
	}

	public Object newInstance(String className, ClassLoader cl)
			throws IllegalAccessException, InvocationTargetException,
			NamingException, InstantiationException, ClassNotFoundException {
		if (className.startsWith("org.apache.catalina")) {
			return Class.forName(className,true,InstanceManager.class.getClassLoader()).newInstance();
		}
		return Class.forName(className,true,cl).newInstance();
	}
	
	private Object inject(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
		if (instance==null){
			return null;
		}
		List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                try {
                    injector.inject(instance);
                } catch (ObjectCreationException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }
        return instance;
	}
	

}
