:: ========================================================================
:: Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
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

::
:: OPTIONAL: The following environment varaialbes are optional.
::
::  %ANT_HOME% - The absolute path of the Apache ant root directory. Ant
::              is required to build Pado.
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
@set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_79

::
:: Gemfire root direcoty path
::
@set GEMFIRE_V7=Y:\GemStone\Pivotal_GemFire_70211_b48040
@set GEMFIRE_V8=Y:\GemStone\Pivotal_GemFire_810_b50625_Windows
@set GEMFIRE=%GEMFIRE_V8%

::
:: ANT_HOME
::
@set ANT_HOME=Y:\Java\apache-ant-1.8.1

::
:: Application specifics
::
:: List all application specific properties here. Make sure to
:: use the prefix "-J-D", e.g., APP_PROPERTIES=-J-Dfoo.test=true
::
@set APP_PROPERTIES=

::
:: GF_JAVA executable
::
@set GF_JAVA=%JAVA_HOME%\bin\java
if  "%ANT_HOME%" == "" (
   @set "PATH=%JAVA_HOME%\bin;%GEMFIRE%\bin;%GEMFIRE%\lib;%PATH%"
) else (
   @set "PATH=%JAVA_HOME%\bin;%GEMFIRE%\bin;%GEMFIRE%\lib;%ANT_HOME%\bin;%PATH%"
)

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
if not "%APP_JARS%" == "" (
  @set CLASSPATH=%APP_JARS%;%CLASSPATH%
)

::
:: If jar files need to be ordered, then list them in front of APP_JARS here.
::
@set APP_JARS=%APP_JARS%

::
:: class path
::
if not "%APP_JARS%" == "" (
   @set CLASSPATH=%BASE_DIR%\classes;%APP_JARS%;%GEMFIRE%\lib\gemfire.jar;%GEMFIRE%\lib\antlr.jar
) else (
   @set CLASSPATH=%BASE_DIR%\classes;%GEMFIRE%\lib\gemfire.jar;%GEMFIRE%\lib\antlr.jar
)

::
:: Change the order of class path as necessary here
::
if not "%USER_CLASSPATH%" == "" (
   @set CLASSPATH=%USER_CLASSPATH%;%CLASSPATH%
)
