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
@setlocal enabledelayedexpansion

::
:: Source in the env files from the pado directory
::
@set PADO_SHELL_DIR=%~dp0
@pushd %PADO_SHELL_DIR%
@call setenv.bat
@call all_env.bat
@call argenv.bat %*
@popd

::
:: Set properties
::
@set GEMFIRE_PROPERTIES=
for /f "delims=" %%a in ('dir /b %BASE_DIR%\lib\ ^| findstr "pado-tools"') do @set PADO_TOOLS_JAR=%%a
@set PADO_PROPERTIES=-Dpado.server=false -Dpado.properties=%ETC_DIR%/client/pado.properties -Dpado.command.jar.path=%BASE_DIR%/lib/%PADO_TOOLS_JAR% %PADO_DEBUG_PROPERTIES% -Dpado.security.aes.userCertificate=%SECURITY_DIR%\user.cer -Dpado.security.keystore.path=%SECURITY_DIR%\client\client-user.keystore

::
:: Plugins directory
::
:: Append all jar files found in the %BASE_DIR%\plugins directory and
:: its subdirectories in the class path.
::
@set PLUGINS_JARS=
for /r %BASE_DIR%\plugins %%i in (*.jar) do (
   @if not defined PLUGINS_JARS (
      @set PLUGINS_JARS=%%i
   ) else (
      @set PLUGINS_JARS=!PLUGINS_JARS!;%%i)
)
if not "%PLUGINS_JARS%" == "" (
  @set CLASSPATH=%CLASSPATH%;%PLUGINS_JARS%
)

::
:: -jar directory
::
:: Append all jar files found in the -jar directory and
:: its subdirectories in the class path.
::
@if defined JAR_OPT (
   @if not exist "%JAR_OPT%" (
      echo pado: %JAR_OPT%: Specified directory does not exist.
      goto stop
   )
   @set JAR_OPT_JARS=
      for /r %JAR_OPT% %%i in (*.jar) do (
      @if not defined JAR_OPT_JARS (
         @set JAR_OPT_JARS=%%i
      ) else (
         @set JAR_OPT_JARS=!JAR_OPT_JARS!;%%i)
   )
   if not "%JAR_OPT_JARS%" == "" (
     @set CLASSPATH=%CLASSPATH%;%JAR_OPT_JARS%
   )
)

"%GF_JAVA%" -Xms512m -Xmx512m -Djava.awt.headless=true %GEMFIRE_PROPERTIES% %SECURITY_PROPERTIES% %PADO_PROPERTIES% com.netcrest.pado.tools.pado.PadoShell %*

:stop
