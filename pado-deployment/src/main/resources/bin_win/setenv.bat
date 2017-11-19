:: ========================================================================
:: Copyright (c) 2013-2017 Netcrest Technologies, LLC. All rights reserved.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::     http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
:: ========================================================================

@echo off

::
:: You may change this file to assign environment variables. At a minimum,
:: you must set the "REQUIRED" environment variables. The "OPTIONAL" 
:: environment variables are mostly for building Pado and running GemFire
:: tools. All third-party dependencies are specifed in this file. 
::

::
:: IMPORTANT: The following environment variables are set by this file.
:: They are used by Pado internals and scripts. Do not set them manually.
::
::  %PADO_HOME% - The absolute path of the Pado directory. Same as %BASE_DIR%.
::  %BASE_DIR% - The absolute path of the Pado directory. Same as %PADO_HOME%.
::

::
:: REQUIRED: The following environment varirables are required to run Pado.
::
::  %JAVA_HOME% - Java home directory path.
::  %GEMFIRE% - GemFire home path.
::

::
:: Application specifics:
::    %APP_PROPERTIES% - Application specific system properties and JVM settings
::             should be set using APP_PROPERTIES. See the APP_PROPERTIES section
::             below for setting system properties. Note that APP_PROPERTIES
::             overrides Pado settings set by start_server.bat.
::    %APP_JARS% - This environment variable is automatically set to include all
::             jar files in the %BASE_DIR%/lib directory and its 
::             subdirectories. If you want to include other jar files or 
::             class directories then include them in APP_JARS. You might need 
::             to reorder the files listed in APP_JARS if you have multiple versions
::             of classes. If so, prepend the proper jar files. APP_JARS is 
::             found below.
::   %USER_CLASSPATH% - Instead of using APP_JARS which also include Pado jars, 
::            you can set your library class paths using this environment
::            variable. Note that USER_CLASSPATH is placed in front of CLASSPATH.
::            You can change the order where USER_CLASSPATH is included in this
::            file.
::
:: Note that the class path constructed by this script is used by all executables,
:: i.e., cacheserver, gfmon, databrower, vsd, and gfsh.
::

::
:: -----------------------------------------------------------------
:: Set environment variables below this line.
:: -----------------------------------------------------------------
::

:: 
:: Set JAVA_HOME to the Java home (root) directory
::
@set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_144

::
:: Gemfire root direcoty path
::
:: @set GEMFIRE_V7=C:\apps\products\Pivotal_GemFire_70211_b48040
@set GEMFIRE_V8=C:\apps\products\Pivotal_GemFire_827_b18_Windows
@set GEMFIRE=%GEMFIRE_V8%

::
:: Application specifics
::
:: List all application specific properties here. Make sure to
:: use the prefix "--J=-D", e.g., APP_PROPERTIES=--J=-Dfoo.test=true
::
@set APP_PROPERTIES=--J=-Djavax.net.ssl.trustStore=../../ldap/example/ssl/trusted.keystore

::
:: GF_JAVA executable
::
@set GF_JAVA=%JAVA_HOME%\bin\java
@set "PATH=%JAVA_HOME%\bin;%GEMFIRE%\bin;%GEMFIRE%\lib;%PATH%"

::
:: -----------------------------------------------------------------
:: Do not modify below this line except for the order of CLASSPATH.
:: -----------------------------------------------------------------
::

::
:: Working directory. Do not edit this line.
::
@pushd ..
@set BASE_DIR=%CD%
@popd

::
:: PADO_HOME
::
@set PADO_HOME=%BASE_DIR:\=/%


@set "PATH=%BASE_DIR%\bin_win;%BASE_DIR%\lib;%PATH%"

::
:: Application library path 
::
:: Append all jar files found in the %BASE_DIR/lib directory and
:: its subdirectories in the class path. 
::
@set APP_JARS=
for /r %BASE_DIR%\lib %%i in (*.jar) do (
   @if not defined APP_JARS (
      @set APP_JARS=%%i
   ) else (
      @set APP_JARS=!APP_JARS!;%%i
   )
)

:: 
:: plugins jars
::
@set PLUGIN_JARS=
@set PREV_FILE_HEAD=
pushd %BASE_DIR%\plugins
@setlocal enabledelayedexpansion
REM for  %%i in (*.jar) do (
for /f  "delims=" %%i in ('dir /b /o:-n *.jar') do (
   call:parseFileName %%i 2
   if "!FILE_HEAD!" neq "!PREV_FILE_HEAD!" ( 
      @if not defined PLUGIN_JARS (
         @set PLUGIN_JARS=%BASE_DIR%\plugins\%%i
      ) else (
         @set PLUGIN_JARS=!PLUGIN_JARS!;%BASE_DIR%\plugins\%%i
      )
   )
   @set PREV_FILE_HEAD=!FILE_HEAD!
)
endlocal & set PLUGIN_JARS=%PLUGIN_JARS%
popd

::
:: If jar files need to be ordered, then list them in front of APP_JARS here.
::
@set APP_JARS=%APP_JARS%

::
:: class path
::
if not "%APP_JARS%" == "" (
   @set CLASSPATH=%BASE_DIR%\classes;%APP_JARS%;%PLUGIN_JARS%;%GEMFIRE%\lib\gemfire.jar;%GEMFIRE%\lib\antlr.jar
) else (
   @set CLASSPATH=%BASE_DIR%\classes;%PLUGIN_JARS%;%GEMFIRE%\lib\gemfire.jar;%GEMFIRE%\lib\antlr.jar
)

::
:: Change the order of class path as necessary here
::
if not "%USER_CLASSPATH%" == "" (
   @set CLASSPATH=%USER_CLASSPATH%;%CLASSPATH%
)

goto stop

REM parseFileName parses file names found in the lib directory
REM to drop the version postfix from the select files names.
REM Input:
REM    arg1 fileName - file name
REM    arg2 delimiterCount - delimiter count of postfix for determining the index number
REM Output:
REM    FILE_HEAD - File header without the postfix.

:parseFileName

   @set FILE_NAME=%~1
   @set DELIMITER_COUNT=%~2
   set _myvar=%FILE_NAME%
   set n=0
:FORLOOP
for /F "tokens=1* delims=." %%A IN ("%_myvar%") DO (
    set vector[!n!]=%%A
    set _myvar=%%B
    @set /a n="!n!+1"
    if NOT "%_myvar%"=="" goto FORLOOP
)

@set /a LAST_INDEX="!n!-1"
@set /a FILE_HEAD_LAST_INDEX="!LAST_INDEX!-!DELIMITER_COUNT!"
@set FILE_HEAD=
for /l %%i in (0,1,%FILE_HEAD_LAST_INDEX%) do (
   if %%i == 0 (
      @set FILE_HEAD=!vector[%%i]!
   ) else (
      @set FILE_HEAD=!FILE_HEAD!-!vector[%%i]!
   )
)

REM @set /a FILE_TAIL_START_INDEX="!FILE_HEAD_LAST_INDEX!+1"
REM @set FILE_TAIL=
REM for /l %%i in (%FILE_TAIL_START_INDEX%,1,%LAST_INDEX%) do (
REM   @set FILE_TAIL=!FILE_TAIL!.!vector[%%i]!
REM )

goto:eof

:stop
