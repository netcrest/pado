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

pushd ..
@call setenv.bat
@call all_env.bat
@call argenv.bat %*
popd

::
:: Append all jar files found in the $BASE_DIR/plugins directory and
:: its subdirectories in the class path. 
::
@set PLUGINS_JARS=
for /r %BASE_DIR%\plugins %%i in (*.jar) do (
   @if not defined PLUGINS_JARS (
      @set PLUGINS_JARS=%%i
   ) else (
      @set PLUGINS_JARS=!PLUGINS_JARS!;%%i
   )
)
if not "%PLUGINS_JARS%" == "" (
  @set CLASSPATH=%PLUGINS_JARS%;%CLASSPATH%
)
