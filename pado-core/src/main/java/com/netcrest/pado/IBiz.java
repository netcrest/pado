/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado;

import com.netcrest.pado.annotation.BizFuture;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.annotation.OnPath;
import com.netcrest.pado.annotation.OnServer;
import com.netcrest.pado.annotation.WithGridCollector;

/**
 * IBiz defines a business object class hosted by Pado. By default, server-side
 * IBiz objects are stateful such that their life spans are same as the grid
 * life unless it is re-deployed. This means there is only one instance per IBiz
 * in the server. Except for the IBizConetxtServer resource described below, any
 * members defined in a server-side implementation class are global to all
 * method invocations made by all clients. The IBizContextServer resource, on
 * the other hand, provides access to its content that is unique per method
 * invocation.
 * <p>
 * The following annotations are supported:
 * <ul>
 * <li>&#64;{@link BizFuture}</li>
 * <li>&#64;{@link BizMethod}</li>
 * <li>&#64;{@link OnPath}</li>
 * <li>&#64;{@link OnServer}</li>
 * <li>&#64;{@link WithGridCollector}</li>
 * </ul>
 * <p>
 * <b>Server:</b> A server-side implementation class has an option to include
 * the following line to capture the biz context information pertaining to an
 * individual specific method call. Pado automatically injects
 * {@link IBizContextServer} as a member.
 * 
 * <pre>
 * &#064;Resource
 * IBizContextServer bizContext;
 * </pre>
 * 
 * Server-side implementation classes are not required to implement the IBiz
 * interface as long as the implemented methods have the same arguments as the
 * matching methods defined in IBiz. Note that the return types can however be
 * different from the IBiz methods. This allows the server-side to return a
 * natural type that the client application may consume as a collection type,
 * for example. Furthermore, a server-side implementation class can choose to
 * implement only select methods of IBiz. This may occur if business logic is
 * mostly kept in the local implementation class of {@link IBizLocal}.
 * <p>
 * <b>Client:</b> A client-side implementation class, i.e., {@link IBizLocal},
 * has an option to include the following line to capture the IBiz proxy object
 * used for remote method invocation where <i>IBiz-class</i> is the proxy IBiz
 * class name. Pado automatically injects the IBiz object as a member. Note that
 * the implementation class can also receives the IBiz object via
 * {@link IBizLocal#init(IBiz, IPado, Object...)}.
 * <p>
 * 
 * <pre>
 * &#64;Resource <i>IBiz-class</i> biz;
 * </pre>
 * 
 * A client-side implementation class can also augment with {@link IBizFuture}
 * to receive time-consuming methods to redirect their results to future
 * objects. This allows the client application to asynchronously receive results
 * at its own pace without resorting to a more complex listener mechanism.
 * <p>
 * 
 * @author dpark
 * 
 */
public interface IBiz
{
	/**
	 * Returns the {@link IBizContextClient} object that contains IBiz context
	 * information. This method is only for clients invoking IBiz methods. The
	 * client implementation class is expected to declare the following resource
	 * where <i>IBiz-class</i> is the proxy IBiz class name
	 * <p>
	 * 
	 * <pre>
	 * &#64;Resource <i>IBiz-class</i> biz;
	 * </pre>
	 * <p>
	 * This method is expected to return the IBizContextClient object as
	 * follows:
	 * <p>
	 * 
	 * <pre>
	 * public IBizContextClient getBizContext()
	 * {
	 * 	return biz.getBizContext();
	 * }
	 * </pre>
	 * <p>
	 * The server-side IBiz implementation classes have no use for this method
	 * and should always return null if they are implementing IBiz. Note that
	 * the server-side IBiz implementation classes are not required to implement
	 * IBiz. They typically implement only IBiz methods that must be remotely
	 * invoked.
	 * 
	 * @return Returns biz context containing the client-side information.
	 */
	IBizContextClient getBizContext();
}
