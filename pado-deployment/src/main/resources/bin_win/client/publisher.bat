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

if "%1" == "-?" (
   echo Usage:"
   echo    publisher [-price|-order]"
   echo.
   echo   Publishes stock price objects or order objects."
   echo.
   echo       -price     Publishes S&P 500 stock prices"
   echo       -order     Publishes random order objects"
   echo.
   echo    Default: publisher -price"
   echo.
   goto stop
)

if "%SECURITY_ERROR%" == "true" (
   goto stop
)

@set CLASS_NAME=Publisher
echo.
if "%1" == "-order" (
   @set CLASS_NAME=OrderPublisher
   echo Publishing Order objects...
) else (
   echo Publishing Stock prices...
)

pushd %PADO_HOME%
"%GF_JAVA%" -Xms128m -Xmx128m -Dpado.properties=%ETC_DIR%\client\pado.properties -DgemfirePropertyFile=%ETC_DIR%\client\client.properties %SECURITY_PROPERTIES% com.netcrest.pado.demo.bank.market.%CLASS_NAME% %*
popd

:stop
