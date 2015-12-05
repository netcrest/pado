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
   echo    kill_agent [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo    Kills the agent in the specified site of the specified grid.
   echo.
   echo       -grid      Kills the agent in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Kills the agent in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: kill_agent -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: the parent directory of all servers
if not exist "%RUN_DIR%" (
   @set RUN_DIR=%BASE_DIR%\run
)

:: directory in which the agent is running
@set DIR=%RUN_DIR%\%AGENT_ID%
@if not exist "%DIR%" (
   @mkdir %DIR%
)

@set PID=
@set LINE=
for /f "delims=" %%a in ('jps -v ^| findstr "%AGENT_ID%"') do @set LINE=%%a
for %%i in (%LINE%) do (
   @set PID=%%i
   goto exit_for
)
:exit_for

if not "%PID%" == "" (
   @call taskkill /pid %PID% /f > NUL 2>&1
   @call erase /f /q %DIR%\* > NUL 2>&1
   echo Killed agent: %AGENT_ID% %PID%
) else (
   echo Kill canceled. Agent is not running: %AGENT_ID%
)
echo.

:stop
