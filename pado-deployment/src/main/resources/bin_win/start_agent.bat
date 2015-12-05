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
   echo    start_agent [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo   Starts an agent in the specified grid and site. This command executes
   echo   on the local host. As such, it starts an agent in the host where it
   echo   is executed.
   echo.
   echo    Default: start_agent -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

@set MY_ADDRESS=localhost
:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i[!LOCATOR_PORT!],%LOCATORS%
)

:: the parent directory of all servers
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
:: directory in which the agent is to be run
@set DIR=%RUN_DIR%\%AGENT_ID%
@if not exist "%DIR%" (
   @mkdir %DIR%
)

::echo start_agent %GRID% %SITE%
echo *****************************************************
echo Starting %AGENT_ID% on host %MY_ADDRESS%
echo *****************************************************
echo %GEMFIRE%\bin\agent start -J-Dpado.vm.id=%AGENT_ID% -J-Djava.awt.headless=true -dir=%DIR% rmi-port=%AGENT_PORT% http-port=%AGENT_HTTP_PORT% mcast-port=0 locators=%LOCATORS% log-file=%DIR%/agent.log
%GEMFIRE%\bin\agent start -J-Dpado.vm.id=%AGENT_ID% -J-Djava.awt.headless=true -dir=%DIR% rmi-port=%AGENT_PORT% http-port=%AGENT_HTTP_PORT% mcast-port=0 locators=%LOCATORS% log-file=%DIR%/agent.log

:stop
