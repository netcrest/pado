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

pushd ..
@call setenv.bat
@call all_env.bat
@call argenv.bat %*
popd

::
:: Append all jar files found in the $BASE_DIR/plugins directory and
:: its subdirectories in the class path. 
::
@set PLUGINS_JARS=
for /r %BASE_DIR%\plugins %%i in (*.jar) do (
   @if not defined PLUGINS_JARS (
      @set PLUGINS_JARS=%%i
   ) else (
      @set PLUGINS_JARS=!PLUGINS_JARS!;%%i
   )
)
if not "%PLUGINS_JARS%" == "" (
  @set CLASSPATH=%PLUGINS_JARS%;%CLASSPATH%
)

if "%GEMFIRE_SECURITY_PROPERTY_FILE%" == "" (
   @set GEMFIRE_SECURITY_PROPERTY_FILE=%ETC_DIR%\client\gfsecurity.properties
)

::
:: Check if security is enabled
::
@set SECURITY_ERROR=false
@set GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if exist "%GEMFIRE_SECURITY_PROPERTY_FILE%" (
   if "%SECURITY_ENABLED%" == "true" (
      @set GEMFIRE_SECURITY_PROPERTY_SYSTEM=-DgemfireSecurityPropertyFile=%GEMFIRE_SECURITY_PROPERTY_FILE%
   )
) else (
   if "%SECURITY_ENABLED%" == "true" (
      echo.
      echo Security is enabled but the following security file does not exist:
      echo    %GEMFIRE_SECURITY_PROPERTY_FILE%
      echo start_server Aborted.
      echo.
      @set SECURITY_ERROR=true
      goto stop
   )
)

if "%SECURITY_ENABLED%" == "true" (
   @set SECURITY_PROPERTIES=-Dpado.security.enabled=true
) else (
   @set SECURITY_PROPERTIES=-Dpado.security.enabled=false
)
@set SECURITY_PROPERTIES=%SECURITY_PROPERTIES% %GEMFIRE_SECURITY_PROPERTY_SYSTEM%

@set PADO_PROPERTIES=-Dpado.home.dir=%PADO_HOME% -Dpado.server=false -Dpado.properties=%ETC_DIR%\client\pado.properties -Dpado.command.jar.path=%BASE_DIR%\lib\pado-tools.jar -Dpado.security.aes.userCertificate=%SECURITY_DIR%\user.cer -Dpado.security.keystore.path=%SECURITY_DIR%\client\client-user.keystore

:stop
