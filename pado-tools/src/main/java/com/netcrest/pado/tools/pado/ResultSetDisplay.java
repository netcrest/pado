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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.tools.pado.util.PrintUtil;

public class ResultSetDisplay
{
	@SuppressWarnings({ "rawtypes" })
	public final static void display(IScrollableResultSet rs) throws Exception
	{
		int startIndex = 0;
		int startRowNum = 1;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		char input = ' ';
		// Set nextIndex > 0 initially to prevent the loop from printing 'TOP'.
		int nextIndex = 1;
		boolean topEndPrinted = false;
		do {
			int rowsPrinted = PrintUtil.printScrollableResultSet(rs, startRowNum, true);
			PadoShell.print(":");
			if (nextIndex <= 0) {
				if (topEndPrinted == false) {
					PadoShell.print("TOP");
					topEndPrinted = true;
				}
			} else if (nextIndex + rowsPrinted >= rs.getTotalSize() - 1) {
				if (topEndPrinted == false) {
					PadoShell.print("END");
					topEndPrinted = true;
				}
			} else {
				topEndPrinted = false;
			}
			boolean getInput = true;
			do {
				input = (char) reader.read();
				switch (input) {
				case 'd':
				case ' ':
					nextIndex = startIndex + rowsPrinted;
					if (nextIndex >= rs.getTotalSize() - 1) {
						if (topEndPrinted == false) {
							PadoShell.print("END");
							topEndPrinted = true;
						}
					} else {
						startIndex = nextIndex;
						startRowNum = startIndex + 1;
						getInput = false;
						rs.goToSet(startIndex);
					}
					break;
				case 'u':
					nextIndex = startIndex - rowsPrinted;
					if (nextIndex < 0) {
						if (topEndPrinted == false) {
							PadoShell.print("TOP");
							topEndPrinted = true;
						}
					} else {
						startIndex = nextIndex;
						startRowNum = startIndex + 1;
						getInput = false;
						rs.goToSet(startIndex);
					}
					break;
				case 'h':
					printHelpSummary();
					break;
				case 'q':
					getInput = false;
					PadoShell.println();
					break;
				}
			} while (getInput);
		} while (input != 'q');
	}

	private static void printHelpSummary()
	{
		PadoShell.println("Scroll Commands");
		PadoShell.println("---------------");
		PadoShell.println("   d or <space>   Forward one page.");
		PadoShell.println("   u              Backward one page.");
		PadoShell.println("   h              Display this help.");
		PadoShell.println("   q              Stop");
	}

}
