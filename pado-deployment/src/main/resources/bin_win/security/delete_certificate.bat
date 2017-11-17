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

if  "%1" == "" (
   echo.
   echo You must specify the client name or alias. Use the -? option to see Usage.
   echo.
   goto stop
)

if  "%1" == "-?" (
   echo.
   echo Usage:
   echo    delete-certificates client-name [-?]
   echo.
   echo       Deletes a certifcate from
   echo          %SECURITY_DIR%\publicKey.keystore.
   echo.
   echo    Note that the password is the password used to create
   echo    the publicKey.keystore file.
   echo.
   goto stop
) 

echo password is pado123
keytool -delete -keystore %SECURITY_DIR%\publicKey.keystore -alias %1

:stop
