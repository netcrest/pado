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
@call all_env.bat
@call argenv.bat %*

if "%1" == "-?" (
   echo Usage:
   echo    start_gfsh [-?]
   echo.
   echo   Starts gfsh. Once started, use the following command to connect
   echo   to a locator: 
   echo.
   echo      connect --locator=\"host[port]\"
   echo.
   echo    Default: start_gfsh
   echo.
   goto stop
)

::
:: cd to the log directory to write log files in there.
:: gfsh currently does not support redirecting log files.
::

:: the parent directory of all servers, locators, and gfsh
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
@set GFSH_LOG_DIR=%RUN_DIR%\gfsh
@if not exist "%GFSH_LOG_DIR%" (
   @mkdir %GFSH_LOG_DIR%
)

@pushd %GFSH_LOG_DIR%
@call gfsh
@popd

:stop
