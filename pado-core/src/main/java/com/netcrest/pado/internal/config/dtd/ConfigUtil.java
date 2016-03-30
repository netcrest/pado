package com.netcrest.pado.internal.config.dtd;

import java.util.List;
import java.util.Properties;

import com.netcrest.pado.IBeanInitializable;

/**
 * ConfigUtil provides utility methods for DTD-specific operations.
 * 
 * @author dpark
 *
 */
public class ConfigUtil
{
	/**
	 * Creates and returns a new bean object with properties initialized if
	 * defined.
	 * 
	 * @return null bean is null or the class name in bean is not defined
	 * @param classLoader
	 *            Class loader to create a new object. If null, then the system
	 *            class loader is used.
	 * @param bean
	 *            Pado bean info
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Object createBean(ClassLoader classLoader, com.netcrest.pado.internal.config.dtd.generated.Bean bean)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		if (bean == null) {
			return null;
		}
		String className = bean.getClassName();
		if (className == null) {
			return null;
		}
		Class<?> clazz;
		if (classLoader == null) {
			clazz = Class.forName(className);
		} else {
			clazz = classLoader.loadClass(className);
		}
		Object obj = clazz.newInstance();
		if (obj instanceof IBeanInitializable) {
			List<com.netcrest.pado.internal.config.dtd.generated.Property> list = bean.getProperty();
			if (list != null) {
				Properties properties = new Properties();
				for (com.netcrest.pado.internal.config.dtd.generated.Property property : list) {
					properties.setProperty(property.getKey(), property.getValue());
				}
				((IBeanInitializable) obj).init(properties);
			}
		}
		return obj;
	}
}
