package com.netcrest.pado.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.execute.Function;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizManager;
import com.netcrest.pado.biz.gemfire.proxy.GemfireBizUtil;
import com.netcrest.pado.biz.gemfire.proxy.functions.BizArguments;
import com.netcrest.pado.biz.gemfire.proxy.functions.ServerProxyFunction;
import com.netcrest.pado.internal.server.BizManager;
import com.netcrest.pado.server.PadoServerManager;

/**
 * BizProxy provides proxy services for dynamically invoking methods of IRpc
 * implementation classes.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BizProxy implements InvocationHandler
{
	private ServerProxyFunction serverProxyFunction;

	/**
	 * Returns a new instance of the proxy handler of the specified {@link IBiz}
	 * class. The returned IBiz object is essentially a local (server-side)
	 * version of an IBiz instance that directly invokes IBiz methods in the
	 * server. This means a method call typically works with local resources
	 * only unless the method itself makes remote calls.
	 * 
	 * @param ibizClass
	 *            {@link IBiz} class
	 * @return null if the specified IBiz class has not been registered. IBiz
	 *         classes must be properly registered during startup or
	 *         hot-deployment for a given app ID.
	 * @see BizProxy
	 */
	public static <T> T newInstance(Class<T> ibizClass)
	{
		BizManager bizManager = PadoServerManager.getPadoServerManager().getAppBizManager(ibizClass);
		if (bizManager == null) {
			return null;
		}
		if (bizManager instanceof GemfireBizManager) {
			GemfireBizManager gbm = (GemfireBizManager) bizManager;
			Function function = gbm.getFunction();
			if (function instanceof ServerProxyFunction) {
				BizProxy handler = new BizProxy((ServerProxyFunction) function);
				return (T) Proxy.newProxyInstance(ibizClass.getClassLoader(), new Class[] { ibizClass }, handler);
			}
		}
		return null;
	}

	/**
	 * Constructs a new BizProxy object with the specified server proxy function
	 * which is directly tied to an instance of the IBiz implementation class.
	 * 
	 * @param spf
	 *            Server proxy function that has an instance of the IBiz
	 *            implementation class.
	 */
	private BizProxy(ServerProxyFunction spf)
	{
		this.serverProxyFunction = spf;
	}

	/**
	 * Invokes the specified IBiz proxy object's method. <i>Do not directly
	 * invoke this method.</i>
	 */
	@Override
	public Object invoke(Object ibizProxy, Method method, Object[] args) throws Throwable
	{
		String gmn = GemfireBizUtil.getMethodName(method);
		BizArguments bizArgs = new BizArguments(gmn, null, args, null, null);
		Object obj = serverProxyFunction.execute(bizArgs);
		Class<?> retType = method.getReturnType();
		if (obj == null || retType.isAssignableFrom(obj.getClass())) {
			return obj;
		} else {
			if (Collection.class.isAssignableFrom(retType)) {
				if (List.class.isAssignableFrom(retType)) {
					return Collections.singletonList(obj);
				} else if (Set.class.isAssignableFrom(retType)) {
					return Collections.singleton(obj);
				}
			}
		}

		// TODO: Unable to cover all cases... need a better way to handle
		// non-conforming implementation classes
		return obj;
	}
}