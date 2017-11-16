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
@call all_env.bat
@call argenv.bat %*

if "%1" == "-?" (
   echo Usage:
   echo    start_site [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-?]
   echo.
   echo   Starts servers in the specified site.
   echo.
   echo       -grid      Starts servers in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Starts servers in the specified site. Default: %SITE_DEFAULT%
   echo       -locators  Restarts locators in addition to servers.
   echo.
   echo    Default: start_site -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)
@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

::
:: start locators
::
if "%LOCATORS%" == "true" (
   @set /a NUM=1
   for %%i in (%LOCATOR_HOSTS%) do (
      @call start_locator -num !NUM! -grid %GRID% -site %SITE%
      @set /a "NUM=!NUM!+1"
      echo.
   )
)

@set SERVER_HOSTS=localhost
@set /a NUM_HOSTS=0
for %%i in (%SERVER_HOSTS%) do (
   @set /a "NUM_HOSTS=!NUM_HOSTS!+1"
)

@set /a NUM_SERVERS_PER_HOST="!NUM_SERVERS!"

:: 
:: Start gateway servers
::
@set /a SERVER_NUM=1
for %%i in (%GATEWAY_SERVERS%) do (
   @call start_server -num !SERVER_NUM! -grid %GRID% -site %SITE% -gateway %SITE%_gateway_%%i.xml
   @set /a SERVER_NUM="!SERVER_NUM!+1"
   echo.
)

::
:: Start the remaining servers
::
@set /a NUM="!SERVER_NUM!"
for %%i in (%SERVER_HOSTS%) do (
   @set /a NUM_NON_GATEWAY_SERVERS="%NUM_SERVERS_PER_HOST%"
   for %%j in (%GATEWAY_SERVERS%) do (
      if "%%i" == "%%j" (
         @set /a NUM_NON_GATEWAY_SERVERS="!NUM_NON_GATEWAY_SERVERS!-1"
      )
   )
   for /l %%k in (1, 1, !NUM_NON_GATEWAY_SERVERS!) do (
      @call start_server -num !NUM! -grid %GRID% -site %SITE%
      @set /a NUM="!NUM!+1"
   )
   echo.
)

:: diplay start status
@set /a BEGIN_NUM=1
for %%i in (%SERVER_HOSTS%) do (
   @set /a END_NUM="!BEGIN_NUM!+!NUM_SERVERS_PER_HOST!-1"
   echo started %%i !BEGIN_NUM! - !END_NUM! in grid: %GRID%, site: %SITE%
   @set /a BEGIN_NUM="!END_NUM!+1"
)
echo.

:stop
