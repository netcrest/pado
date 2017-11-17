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
@setlocal ENABLEDELAYEDEXPANSION

@call setenv.bat
@call all_env.bat
@call argenv.bat %*

set DIR=%CD%
set SHORTCUT_DIR=Shortcut
set SHORTCUT_ALL_DIR=Shortcut\All
set SHORTCUT_CLIENT_DIR=Shortcut\Client

REM
REM Create Shortcut directories
REM
pushd ..
@if exist "%SHORTCUT_DIR%" (
   rmdir /s /q %SHORTCUT_DIR%
)
mkdir %SHORTCUT_ALL_DIR%
@for %%X in (%GRIDS%) do (
   @set GRID=%%X
   mkdir %SHORTCUT_DIR%\!GRID!\AllSites
   @for %%X in (%SITES%) do (
      @set SITE=%%X
      mkdir %SHORTCUT_DIR%\!GRID!\!SITE!
   )
)

REM
REM All
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\1.1. Restart All.lnk" %DIR%\restart_all.bat "-locators -kill" %DIR% "Restarts all grids (%GRIDS%) after killing all running grids" 1 %DIR%\images\Start.bmp
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\1.2. Kill All.lnk" %DIR%\kill_all.bat "-locators" %DIR% "Kills all grids (%GRIDS%)" 1 %DIR%\images\Stop.bmp
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\1.3. Clean All.lnk" %DIR%\clean_all.bat "" %DIR% "Cleans all grids (%GRIDS%) - removes log and stats files" 1 %DIR%"\images\Clean.ico
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\1.4. Check All.lnk" %DIR%\check_all.bat "-prompt" %DIR% "Displays all running grids (%GRIDS%)" 1 %DIR%"\images\Check.ico
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\2.1. bin_win.lnk" %windir%\system32\cmd.exe "/K setenv.bat" %DIR% "bin_win" 1
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\3.1. PadoShell.lnk" %DIR%\pado.bat "" %DIR% "Starts PadoShell" 1 %DIR%"\images\pado_40x40.ico"
bin_win\XXMKLINK.EXE /q "%SHORTCUT_ALL_DIR%\3.2. gfsh.lnk" %DIR%\start_gfsh.bat "" %DIR% "Starts gfsh" 1 %DIR%"\images\Fish.ico"

REM
REM Grids
REM
@for %%X in (%GRIDS%) do (
   @set GRID=%%X
   @set ALL_SITES_DIR=%SHORTCUT_DIR%\!GRID!\AllSites
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\1.1. Restart Sites.lnk" %DIR%\restart_grid.bat "-grid !GRID! -locators -kill" %DIR% "Restarts all sites (%SITES%) after killing all running sites" 1 %DIR%\images\Start.bmp
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\1.2. Kill All Sites.lnk" %DIR%\kill_grid.bat "-grid !GRID! -locators" %DIR% "Kills all sites (%SITES%)" 1 %DIR%\images\Stop.bmp
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\1.3. Clean All Sites.lnk" %DIR%\clean_grid.bat "-grid !GRID!" %DIR% "Cleans all sites (%SITES%) - removes log and stats files" 1 %DIR%"\images\Clean.ico
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\1.4. Check Grid.lnk" %DIR%\check_grid.bat "-grid !GRID! -prompt" %DIR% "Displays all running sites (%SITE%) in grid, !GRID!" 1 %DIR%"\images\Check.ico
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\2.1. bin_win.lnk" %windir%\system32\cmd.exe "/K setenv.bat" %DIR% "bin_win" 1
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\3.1. PadoShell.lnk" %DIR%\pado.bat "" %DIR% "Starts PadoShell" 1 %DIR%"\images\pado_40x40.ico"
   bin_win\XXMKLINK.EXE /q "!ALL_SITES_DIR!\3.2. gfsh.lnk" %DIR%\start_gfsh.bat "" %DIR% "Starts gfsh" 1 %DIR%"\images\Fish.ico"

   REM
   REM Shortcuts for each site
   REM
   @for %%X in (%SITES%) do (
      @set SITE=%%X
      @set SITE_DIR=%SHORTCUT_DIR%\!GRID!\!SITE!
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\1.1. Restart !SITE!.lnk" %DIR%\restart_site.bat "-grid !GRID! -site !SITE! -locators -kill" %DIR% "Restarts the entire !SITE! (locator, servers) after killing all running sites" 1 %DIR%\images\Start.bmp
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\1.2. Kill !SITE!.lnk" %DIR%\kill_site.bat "-grid !GRID! -site !SITE! -locators" %DIR% "Kills the entire !SITE! (servers, locator)" 1 %DIR%\images\Stop.bmp
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\1.3. Clean !SITE!.lnk" %DIR%\clean_site.bat "-grid !GRID! -site !SITE!" %DIR% "cleans !SITE! - Removes log and stats files" 1 %DIR%"\images\Clean.ico
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\1.4. Check Site.lnk" %DIR%\check_site.bat "-site !SITE! -prompt" %DIR% "Displays all running servers in site, !SITE!" 1 %DIR%"\images\Check.ico
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\2.1. bin_win.lnk" %windir%\system32\cmd.exe "/K setenv.bat" %DIR% "bin_win" 1
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\3.1 PadoShell.lnk" %DIR%\pado.bat "" %DIR% "Starts PadoShell" 1 %DIR%"\images\pado_40x40.ico"
      bin_win\XXMKLINK.EXE /q "!SITE_DIR!\3.2 gfsh.lnk" %DIR%\start_gfsh.bat "" %DIR% "Starts gfsh" 1 %DIR%"\images\Fish.ico"
   )
)

REM
REM Explorer
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\%SYSTEM_NAME% dir.lnk" %windir%\explorer.exe "%CD%" %windir% "%SYSTEM_NAME% dir" 1

REM
REM Command linet window - bin_win
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\bin_win.lnk" %windir%\system32\cmd.exe "/K setenv.bat" %DIR% "bin_win" 1

REM
REM PadoShell
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\PadoShell.lnk" %DIR%\pado.bat "" %DIR% "Starts PadoShell" 1 %DIR%"\images\pado_40x40.ico"

REM
REM gfsh
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\gfsh.lnk" %DIR%\start_gfsh.bat "" %DIR% "Starts gfsh" 1 %DIR%"\images\Fish.ico"

REM
REM README and RELEASE_NOTES files in main folder
REM
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\README.lnk" %DIR%\..\README.txt "" %DIR% "README.txt" 1
bin_win\XXMKLINK.EXE /q "%SHORTCUT_DIR%\RELEASE_NOTES.lnk" %DIR%\..\RELEASE_NOTES.txt "" %DIR% "RELEASE_NOTES.txt" 1

REM
REM Create a shortcut on desktop
REM

bin_win\XXMKLINK.EXE /q "%USERPROFILE%\Desktop\%SYSTEM_NAME%.lnk" %CD%\%SHORTCUT_DIR% "" %CD%\%SHORTCUT_DIR% "%SYSTEM_NAME%" 1

popd

:stop
