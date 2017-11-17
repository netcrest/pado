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
   echo    kill_server [-num server-number] [-site %SITES_OPT%] [-?]
   echo.
   echo    Kills a server in the specified grid and site." 
   echo    The kill command is faster than the stop command
   echo    but at the expense of possible loss or corruption
   echo    of data.
   echo.
   echo       server-number server number 1-99
   echo       -grid      Kills the server in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Kills the server in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: kill_server -num 1 -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: the parent directory of all servers
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
:: directory in which the server is to be run
@set DIR=%RUN_DIR%\%SERVER_ID%

@set PID=
@set LINE=
for /f "delims=" %%a in ('jps -v ^| findstr "%SERVER_ID%"') do @set LINE=%%a
for %%i in (%LINE%) do (
   @set PID=%%i
   goto exit_for
)
:exit_for

if not "%PID%" == "" (
   @call taskkill /pid %PID% /f > NUL 2>&1
   @call erase /f /q %DIR%\* > NUL 2>&1
   echo Killed server: %SERVER_ID% %PID% 
) else (
   echo Kill canceled. Server is not running: %SERVER_ID%
)

echo.

:stop
