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
package com.netcrest.pado.internal.l;

import com.netcrest.pado.PadoVersion;

public class PadoDisclaimer
{
	public final static String getDisclaimer()
	{
		StringBuffer b = new StringBuffer(1024);
		b.append("\n\n");
		b.append("==========================================================================\n");
		b.append("           Netcrest PADO: Extreme Scalability of Data Grid\n");
		b.append("   Copyright (c) 2013-2015 Netcrest Technologies, LLC, All rights reserved.\n");
		b.append("\n");
		b.append("       This product is protected by  U.S. and international\n");
		b.append("       copyright  and intellectual property laws. All other\n");
		b.append("       products mentioned herein may be trademarks of their\n");
		b.append("       respective companies.\n");
		b.append("\n");
		b.append("   Pado is licensed under the following terms:\n");
		b.append("\n");
		b.append("               Application: PADO\n");
		b.append("                      Type: OPEN SOURCE\n");
		b.append("                Version(s): " + PadoVersion.getVersion() + "\n");
		b.append("           Expiration Date: NEVER\n");
		b.append("                 Max Grids: UNLIMITED\n");
		b.append("      Max Servers Per Grid: UNLIMITED\n");
		b.append("==========================================================================");
		return b.toString();
	}
}
