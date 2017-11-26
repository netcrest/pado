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
package com.netcrest.pado.tools.pado;

import com.netcrest.pado.PadoVersion;

public class PadoShellLogo
{
	// http://www.kammerl.de/ascii/AsciiSignature.php
	// (Font: 's-relief')
		private final static String padoLogo_s_relief = ""
			+ "__/\\\\\\\\\\\\\\\\\\\\\\\\\\________/\\\\\\\\\\\\\\\\\\______/\\\\\\\\\\\\\\\\\\\\\\\\___________/\\\\\\\\\\______\n"
			+ " _\\/\\\\\\/////////\\\\\\____/\\\\\\\\\\\\\\\\\\\\\\\\\\___\\/\\\\\\////////\\\\\\_______/\\\\\\///\\\\\\____\n"
			+ "  _\\/\\\\\\_______\\/\\\\\\___/\\\\\\/////////\\\\\\__\\/\\\\\\______\\//\\\\\\____/\\\\\\/__\\///\\\\\\__\n"
			+ "   _\\/\\\\\\\\\\\\\\\\\\\\\\\\\\/___\\/\\\\\\_______\\/\\\\\\__\\/\\\\\\_______\\/\\\\\\___/\\\\\\______\\//\\\\\\_\n"
			+ "    _\\/\\\\\\/////////_____\\/\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\__\\/\\\\\\_______\\/\\\\\\__\\/\\\\\\_______\\/\\\\\\_\n"
			+ "     _\\/\\\\\\______________\\/\\\\\\/////////\\\\\\__\\/\\\\\\_______\\/\\\\\\__\\//\\\\\\______/\\\\\\__\n"
			+ "      _\\/\\\\\\______________\\/\\\\\\_______\\/\\\\\\__\\/\\\\\\_______/\\\\\\____\\///\\\\\\__/\\\\\\____\n"
			+ "       _\\/\\\\\\______________\\/\\\\\\_______\\/\\\\\\__\\/\\\\\\\\\\\\\\\\\\\\\\\\/_______\\///\\\\\\\\\\/_____\n"
			+ "        _\\///_______________\\///________\\///___\\////////////___________\\/////_______\n";

	// (Font: 'starwars')
	private final static String padoLogo_starwars = "" 
			+ ".______      ___       _______    ______  \n"
			+ "|   _  \\    /   \\     |       \\  /  __  \\ \n" 
			+ "|  |_)  |  /  ^  \\    |  .--.  ||  |  |  |\n"
			+ "|   ___/  /  /_\\  \\   |  |  |  ||  |  |  |\n" 
			+ "|  |     /  _____  \\  |  '--'  ||  `--'  |\n"
			+ "| _|    /__/     \\__\\ |_______/  \\______/ ";

	public final static String getPadoLogo()
	{
//		PadoVersion padoVersion = new PadoVersion();
//		return padoLogo_starwars + " v" + padoVersion.getVersion();
		return padoLogo_starwars;
	}

	public final static String getCopyrights()
	{
		return "Copyright 2013-2017 Netcrest Technologies, LLC. All rights reserved.";
	}

	public static void main(String args[])
	{
		System.out.println(getPadoLogo());
	}
}
