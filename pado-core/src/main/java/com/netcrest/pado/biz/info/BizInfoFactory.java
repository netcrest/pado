package com.netcrest.pado.biz.info;

import com.netcrest.pado.IBizInfo;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.factory.InfoFactory;
import com.netcrest.pado.server.PadoServerManager;

/**
 * BizInfoFactory provides methods to create IBizInfo objects.
 * 
 * @author dpark
 *
 */
public class BizInfoFactory
{
	/**
	 * Creates an instance of IBizInfo for the specified IBiz class.
	 * 
	 * @param ibizClass
	 *            IBiz interface class
	 * @return null if ibizClass is null or it encounters a class loader error
	 */
	public static IBizInfo createBizInfo(Class<?> ibizClass)
	{
		if (ibizClass == null) {
			return null;
		}
		IBizInfo bizInfo = null;
		String infoClassName = ibizClass.getPackage().getName() + ".info." + ibizClass.getSimpleName().substring(1)
				+ "Info";
		try {
			Class<?> infoClass = ibizClass.getClassLoader().loadClass(infoClassName);
			bizInfo = (IBizInfo) infoClass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// BizInfo class undefined. Default is to retain just the class
			// name.
			bizInfo = InfoFactory.getInfoFactory().createSimpleBizInfo(ibizClass.getName());
		}
		return bizInfo;
	}

	/**
	 * Creates a JsonLite instance representing IBizInfo for the specified IBiz
	 * class.
	 * 
	 * @param ibizClass
	 *            IBiz interface class
	 * @return null if ibizClass is null or it encounters a class loader error
	 */
	public static JsonLite<?> createJsonBizInfo(Class<?> ibizClass)
	{
		IBizInfo bizInfo = createBizInfo(ibizClass);
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.toJson();
	}

	/**
	 * Creates an instance of IBizInfo for the specified IBiz class name.
	 * 
	 * @param ibizClassName
	 *            Fully qualified IBiz class name
	 * @return null if ibizClassName is null or it encounters a class loader
	 *         error
	 */
	public static IBizInfo createBizInfo(String ibizClassName)
	{
		if (ibizClassName == null) {
			return null;
		}
		IBizInfo bizInfo = null;
		int index = ibizClassName.lastIndexOf('.');
		String infoClassName;
		if (index == -1) {
			infoClassName = ibizClassName;
		} else {
			String packageName = ibizClassName.substring(0, index);
			infoClassName = packageName + ".info." + ibizClassName.substring(index + 2) + "Info";
		}
		try {
			ClassLoader classLoader = PadoServerManager.getPadoServerManager().getAppBizClassLoader();
			if (classLoader == null) {
				classLoader = ClassLoader.getSystemClassLoader();
			}
			Class<?> infoClass = classLoader.loadClass(infoClassName);
			bizInfo = (IBizInfo) infoClass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// BizInfo class undefined. Default is to retain the class and
			// method names.
			bizInfo = InfoFactory.getInfoFactory().createSimpleBizInfo(ibizClassName);
		}
		return bizInfo;
	}

	/**
	 * Creates a JsonLite instance representing IBizInfo for the specified IBiz
	 * class name. class.
	 * 
	 * @param ibizClassName
	 *            Fully qualified IBiz class name
	 * @return null if ibizClassName is null or it encounters a class loader
	 *         error
	 */
	public static JsonLite<?> createJsonBizInfo(String ibizClassName)
	{
		IBizInfo bizInfo = createBizInfo(ibizClassName);
		if (bizInfo == null) {
			return null;
		}
		return bizInfo.toJson();
	}
}
