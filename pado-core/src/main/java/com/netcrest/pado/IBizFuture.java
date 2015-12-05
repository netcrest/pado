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

/**
 * <b>Important:</b> <i>IBizFuture is fully functional; however, its usage
 * pattern may change in the future which may affect the application code.</i>
 * <p>
 * IBizFuture is an optional interface for asynchronously executing remote
 * methods that may take a long time to complete. IBizFuture relies on
 * annotations defined in IBiz and therefore it complements IBiz and does not
 * replace IBiz. Interfaces extending IBizFuture must contain the exact method
 * signatures as its counterpart IBiz and return Future objects for all methods
 * except for the void return type.
 * <p>
 * IFooBiz and its counterpart IFooBizFuture shown below illustrates the use of
 * the required annotations. Note that IFooBizFuture does not define @
 * {@link OnPath}. The IBiz mechanism picks up all annotations defined by the
 * class specified via &#64;{@link BizFuture}.
 * <p>
 * 
 * <pre>
 * &#064;BizClass(name = &quot;FooBiz&quot;)
 * public interface IFooBiz extends IBiz
 * {
 * 	&#064;BizMethod
 * 	&#064;OnPath(path = &quot;foo&quot;)
 * 	Map&lt;String, List&lt;String&gt;&gt; getSomeMap();
 * }
 * </pre>
 * 
 * <pre>
 * import java.util.concurrent.Future;
 * 
 * &#064;BizFuture(bizInterface = &quot;com.foo.IFooBiz&quot;)
 * public interface IFooBizFuture extends IBizFuture
 * {
 * 	&#064;BizMethod
 * 	Future&lt;Map&lt;String, List&lt;String&gt;&gt;&gt; getSomeMap();
 * }
 * </pre>
 * <p>
 * IBizFuture interface classes may use the following annotations:
 * <ul>
 * <li>&#64;{@link BizFuture} - IBizFuture classes are required to specify the
 * corresponding IBiz class names using this annotation.</li>
 * <li>&#64;{@link BizMethod} - IBizFuture methods are required this annotation.
 * All non-annotated methods are silently ignored.</li>
 * 
 * @author dpark
 * 
 */
public interface IBizFuture extends IBiz
{
}
