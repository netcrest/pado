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

echo 2. SECURITY_DIR=%SECURITY_DIR%

if "%1" == "-?" (
   echo.
   echo Usage:
   echo    list-certificates [-?]
   echo.
   echo       Lists all certifcates in
   echo          %SECURITY_DIR%\publicKey.keystore.
   echo.
   echo    Note that the password is the password used to create 
   echo    the publicKey.keystore file.
   echo.
   goto stop
) 

echo password is pado123
keytool -list -keystore %SECURITY_DIR%\publicKey.keystore
echo.
echo Keystore: %SECURITY_DIR%\publicKey.keystore
echo.

:stop
