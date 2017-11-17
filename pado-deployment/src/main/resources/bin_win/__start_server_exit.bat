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

:: __start_server_exit is executed by start_site.bat in order to run
:: the servers in back ground. Without this script, running scripts in
:: background causes the command prompt to behave strangely. It seems the
:: exit call at the end of the start call resolves the command prompt issue.

@echo off
@call start_server %*
exit
