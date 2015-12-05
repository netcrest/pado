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
@setlocal enabledelayedexpansion

@call setenv.bat

if "%1" == "" (
   echo secure-site not specified. Aborted. '-?' to see usage.
   echo.
   goto stop
)

if "%1" == "-?" (
   echo Usage:
   echo    install_certificate secure-site[:port] [-?]
   echo.
   echo    Installs the certificates downloaded from the specified web site
   echo    into %SECURITY_DIR%\pado.keystore
   echo    if the file exists. If the keystore file does not exist then it
   echo    stores the certificates in the default Java keystore file found
   echo    in the Java installation directory:
   echo.
   echo       %JAVA_HOME%\lib\security\cacerts
   echo.
   echo    Note that the cacerts file may differ from OS to OS. If pado.keystore
   echo    does not exist, then it is recommended that copy cacerts to:
   echo.
   echo       %SECURITY_DIR%\pado.keystore
   echo.
   echo    See the 'Security' section in README.txt for details on how to read
   echo    the keystore file from your Pado adapters.
   echo.
   echo       secure-site   Web site address
   echo       port          Securte site port number. Default: 443
   echo.
   echo    Default: install_certificate ...:443
   echo.
   goto stop
)

pushd %PADO_HOME%
"%GF_JAVA%" -Xms32m -Xmx32m com.netcrest.pado.tools.com.aw.ad.util.InstallCert %*
popd

:stop
