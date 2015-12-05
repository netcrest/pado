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

import javax.annotation.Resource;

import com.netcrest.pado.annotation.BizClass;

/**
 * Local biz classes are required to implement IBizLocal. IBizLocal methods are
 * executed locally prior to executing their remote counterpart methods. This
 * allows applications to perform local tasks such as input argument validation
 * and filtration tasks before committing remote method invocations.
 * <p>
 * Local implementation classes can use &#64;{@link Resource} to capture the
 * corresponding IBiz proxy object injected by
 * {@link ICatalog#newInstance(Class, Object...)} as shown in the example below.
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * public class FooBizLocalImpl implements IFooBiz, IBizLocal
 * {
 *    // Use &#64;Resource to capture the IFooBiz proxy object.
 *    // The proxy object is automatically injected by the underlying
 *    // Pado's catalog service.
 *    &#64;Resource IFooBiz biz;
 *    
 *    // Initializes this object. The passed in arguments are the initialization
 *    // values provided by the caller. See {@link ICatalog#newInstance(Class, Object...)}.
 *    &#64;Override
 *    public void init(IBiz biz, IPado pado, Object...args)
 *    {
 *       this.biz = (IFooBiz)biz;
 *    }
 *    
 *    // getBizContext() is required by IBiz.
 *    &#64;Override
 *    public IBizContextClient getBizContext()
 *    {
 *        return biz.getBizContext();
 *    }
 *    
 *    &#64;Override
 *    public String getValue(String key)
 *    {
 *       // if key is null return immediately without 
 *       // invoking the corresponding remote method
 *       if (key == null) {
 *          return null;
 *       }
 *       
 *       // invoke remote method
 *       return biz.getValue(key);
 *    }
 *    
 *    &#64;Override
 *    public int add(int x, int y)
 *    {
 *       // Alter the value of x
 *       x += 10;
 *       
 *       // invoke remote method
 *       return biz.add(x, y);
 *    }
 *    
 *    ...
 * }
 * </pre>
 * <ul>
 * <li>
 * All local biz classes must implement IBizLocal.
 * </li>
 * <li>
 * Local biz classes implement the same IBiz interface as the
 * remote implementation class. For example, IFooBiz in the example above
 * implements IBiz.
 * </li>
 * </ul>
 * 
 * @see BizClass#localImpl()
 * @see IBiz
 * @author dpark
 * 
 */
public interface IBizLocal
{
	/**
	 * Initializes the IBizLocal instance.
	 * 
	 * @param biz
	 *            IBiz proxy instance
	 * @param pado
	 *            Pado instance
	 * @param args
	 *            Arguments to intialize the IBizLocal instance
	 */
	void init(IBiz biz, IPado pado, Object... args);
}
