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
   echo    locate_server [-num <server number>] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo   Locates the specified server in the specified grid and site.
   echo.
   echo       <server number> server number 1-99
   echo       -grid      Locates the specified server number in
   echo.                 the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Locates the specified server number in
   echo                  the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: locate_server -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

if %SERVER_NUM% lss 1 (
   goto out_of_range
)
if %SERVER_NUM% gtr %NUM_SERVERS% (
   goto out_of_range
)

@set LINE=
@set PID=
REM find the process running on the host
for /f "delims=" %%a in ('jps -v ^| findstr "%SERVER_ID%"') do @set LINE=%%a
for %%i in (%LINE%) do (
   @set PID=%%i
   goto exit_for
)
:exit_for
echo.
echo     Server ID: %SERVER_ID%
echo          Grid: %GRID%
echo          Site: %SITE%
echo Server Number: %SERVER_NUM%
echo          Host: %HOST%
if "%PID%" == "" (
   echo    Process ID: Not found
) else (
   echo    Process ID: %PID%
)      
echo.
goto stop

:out_of_range
   echo.
   echo Server number out of range. Valid range [1, %NUM_SERVERS%]
   echo.
   goto stop

:stop
