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

if "%SECURITY_ERROR%" == "true" (
   goto stop
)

@set JSONLITE=-DkeyMapType=jsonlite

pushd %BASE_DIR%
"%GF_JAVA%" -Xms256m -Xmx256m -Djava.awt.headless=true %PADO_PROPERTIES% -DgemfirePropertyFile=%ETC_DIR%\client\client.properties %SECURITY_PROPERTIES% %JSONLITE% com.netcrest.pado.temporal.test.TemporalClient %*
popd

:stop
