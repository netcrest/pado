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

@call setenv.bat

if "%1" == "" (
   echo.
   echo You must specify the client name. Use the -? option to see Usage.
   echo.
   goto stop
)

if "%1" == "-?" (
   echo.
   echo Usage:
   echo    create-keystore client-name [-?]
   echo.
   echo       Creates a keystore file: %SECURITY_DIR%\client\client-name.keystore
   echo       to enable Pado RSA security encription.
   echo.
   echo    client-name  the name of the client application used
   echo                 to create keystore and certificate files.
   echo.                It is also the password.
   echo.
   goto stop
) 

:: Create %SECURITY_DIR% directory if not defined
if  not exist "%SECURITY_DIR%\client" (
  mkdir %SECURITY_DIR%\client
)

echo Use password: %1
keytool -genkey -alias %1 -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore %SECURITY_DIR%\client\%1.keystore

echo Keystore created: %SECURITY_DIR%\client\%1.keystore

:stop
