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

if "%1" == "-?" (
   echo Usage:
   echo    missing_disk_store [-site %SITES_OPT%] [-?]
   echo    Default: missing_disk_store -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i:!LOCATOR_PORT!,%LOCATORS%
)

:: the parent directory of all servers, locators, and gfsh
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
@set GFSH_LOG_DIR=%RUN_DIR%\gfsh
@if not exist "%GFSH_LOG_DIR%" (
   @mkdir %GFSH_LOG_DIR%
)

:: if "%DISK_STORE_DIR%" == "" (
::    @set DISK_STORE_DIR=%DIR%\store
:: )
:: @if not exist "%DISK_STORE_DIR%" (
::   mkdir %DISK_STORE__DIR%
:: )

@pushd %GFSH_LOG_DIR%
echo gfsh -e "connect --locator=%LOCATORS%" -e "show missing-disk-stores"
gfsh -e "connect --locator=%LOCATORS%" -e "show missing-disk-stores"
@popd

:stop
