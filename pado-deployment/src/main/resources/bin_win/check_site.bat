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
   echo    check_site [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-prompt] [-?]
   echo.
   echo        Displays running status of all locators, agents and servers
   echo        in the specified grid.
   echo.
   echo       -grid      Displays servers in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Displays servers in the specified site. Default: %SITE_DEFAULT%
   echo       -prompt    Displays user prompt to exit.
   echo.
   echo    Default: check_site -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

@set /a LOCATOR_INDEX=0
@set /a AGENT_INDEX=0
@set /a SERVER_INDEX=0
for /f "delims=" %%i in ('jps -v ^| findstr "pado.vm.id"') do (
   @set pid=
   @set id=
   for /f "tokens=1,2,3 delims=/ " %%a in ("%%i") do (
      @set pid=%%a
      @set name=%%b
      @set prop=%%c
   )
   for /f "tokens=1,2 delims==" %%a in ("!prop!") do (
      @set id=%%b
   )

   @set GRID_MATCHED=
   for /f "delims=" %%a in ('echo !id! ^| findstr "%GRID%"') do @set GRID_MATCHED=%%a
   if "!GRID_MATCHED!" neq "" (
      @set SITE_MATCHED=
      for /f "delims=" %%a in ('echo !id! ^| findstr "%SITE%"') do @set SITE_MATCHED=%%a
      if "!SITE_MATCHED!" neq "" (
         @set LOCATOR_ID=
         @set AGENT_ID=
         @set SERVER_ID=
         for /f "delims=" %%a in ('echo !id! ^| findstr "locator"') do @set LOCATOR_ID=%%a
         if "!LOCATOR_ID!" neq "" (
            @set ARRAY_LOCATOR_ID[!LOCATOR_INDEX!]=!id!      
            @set ARRAY_LOCATOR_PID[!LOCATOR_INDEX!]=!pid!      
            @set /a LOCATOR_INDEX="!LOCATOR_INDEX!+1"
         )
         for /f "delims=" %%a in ('echo !id! ^| findstr "agent"') do @set AGENT_ID=%%a
         if "!AGENT_ID!" neq "" (
            @set ARRAY_AGENT_ID[!AGENT_INDEX!]=!id!      
            @set ARRAY_AGENT_PID[!AGENT_INDEX!]=!pid!      
            @set /a AGENT_INDEX="!AGENT_INDEX!+1"
         )
         for /f "delims=" %%a in ('echo !id! ^| findstr "server"') do @set SERVER_ID=%%a
         if "!SERVER_ID!" neq "" (
            @set ARRAY_SERVER_ID[!SERVER_INDEX!]=!id!      
            @set ARRAY_SERVER_PID[!SERVER_INDEX!]=!pid!      
            @set /a SERVER_INDEX="!SERVER_INDEX!+1"
         )
      )
   )
)

echo -------------------------------------------
echo Site: %SITE%
echo -------------------------------------------

@set /a ALL_INDEXES="!LOCATOR_INDEX!+!AGENT_INDEX!+!SERVER_INDEX!"

if !ALL_INDEXES! leq 0 (
   echo Site down. No locators, agents and servers running.
) else (
   @set /a END_INDEX="!LOCATOR_INDEX!-1"
   for /l %%i in (0,1,!END_INDEX!) do (
      echo.  Locator: !ARRAY_LOCATOR_ID[%%i]! !ARRAY_LOCATOR_PID[%%i]!
   )
   @set /a END_INDEX="!AGENT_INDEX!-1"
   for /l %%i in (0,1,!END_INDEX!) do (
      echo.    Agent: !ARRAY_AGENT_ID[%%i]! !ARRAY_AGENT_PID[%%i]!
   )
   @set /a END_INDEX="!SERVER_INDEX!-1"
   for /l %%i in (0,1,!END_INDEX!) do (
      echo.   Server: !ARRAY_SERVER_ID[%%i]! !ARRAY_SERVER_PID[%%i]!
   )
)

if "%PROMPT%" == "true" (
   @set /p USER_INPUT=Type Enter to exit . . .
)

:stop
