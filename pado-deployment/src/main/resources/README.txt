Netcrest Pado Open Source Edition
=================================
https://github.com/netcrest/pado

�2013-2015 Netcrest Technologies, LLC. All rights reserved.

Introduction
------------

Pado is a comprehensive software infrastructure platform for managing and servicing true cloud stores at memory speeds while ensuring linear scalability, data ubiquity, and ease of use. This distribution includes Pado binaries for launching and federating multipe grids. For a quick start, it is equipped with example grids that can be launched, as with any Pado grids, by running a single command script.

Quick Start Guide
-----------------

The best way to quickly learn Pado is to follow the instructions in "doc/Quick Start Guide.pdf". This guide provides step-by-step instructions for installing and launching Pado and Pado Desktop in less than five minutes.

Finding Pado version
--------------------

Pado version can be obtained by executing the following:

   cd $PADO_HOME/bin_sh; ./pado -v

where $PADO_HOME is the root directory of Pado installation.

Compiling Pado
--------------

This distribution has been created by following the instructions in

   https://github.com/netcrest/pado/README.txt

Installation
------------

1. Unzip the pado_<version>.zip or pado_<version>.tar.gz file anywhere in the file system. It will create the "pado_<version>" root directory.
2. Change directory to pado_<version>/bin_(sh/win) and edit setenv.(sh/bat) to include the correct paths for JAVA_HOME and GEMFIRE, where "sh" is for Unix and "win" is for Windows.

Directories
-----------

bin_sh/ - Contains scripts to run Pado grids on Unix. The Unix scripts allow you to
          remotely manage grids, sites, servers, and locators.

bin_win/ - Contains scripts to run Pado grids on Windows. 
           Unlike the Unix scripts, the Windows scripts can only launch Pado grids
           on localhost.

lib/pado-common-<version>.jar  Contains the common classes shared by all modules.
lib/pado-core-<version>.jar    Contains the core classes. 
lib/pado-gemfire.jar  Contains GemFire plug-in code that is not visible to
                      the public.
lib/pado-gemfire-v7-<version>.jar  Contains GemFire 7.x port classes.
lib/pado-tools-<version>.jar   Contains Pado tools such as PadoShell.

plugins/pado-biz-<version>.jar Contains Pado's built-in IBiz classes.
plugins/pado-biz-gemfire-<version>.jar  Contains Gemfire specific IBiz classes.
plugins/pado-test-biz-<version>.jar      Contains IBiz test drivers.

src/ - Source code directory if provided.

build.xml - ant build file if provided. Please see BUILD.txt for instructions. 
            Run 'ant help' to see ant help.

BUILD.txt - build instructions.

Configuring scripts
-------------------

Pado is managed by the scripts found in the bin_(sh/bat) directory. The following scripts may be modified to set your environment.

setenv.(sh/bat)    Edit this file to set the correct JAVA_HOME and 
                   GEMFIRE paths. If source code is provided then 
                   set ANT_HOME also. Include application specific
                   properties and JVM paramenters using APP_JARS
all_env.(sh/bat)   Edit this file to set the grids that you wish
                   to run.

All other files should not be modified. 

Script confiugration
--------------------

On Windows, you can create a shortcut folder by running bin_win\create_shortcuts.bat. This will create the "Pado" shortcut on your desktop. You can start/stop grids and servers from this folder.

On Windows, the scripts will launch servers only on the local machine. On Unix, you can remotely launch servers provided that you have setup ssh auto-login. Please see "Configuring SSH Auto Login" below.

bin(_sh/_bat)/grids This directory must contain sub-directories that 
                    represent the grids that you listed in all_env.(sh/bat). 

Grid sub-directory example: 
all_env.sh  As an example, if GRIDS is set as GRIDS="grid0 grid1" then "grid0"
            and "grid1" effectively become default grid IDs that the Pado grid
            management facility recognizes. Note that for each grid ID, the
            corresponding directory with the same name must exist in the "grids"
            directory, i.e., "grids/grid0" and "grids/grid1". Pado includes
            following example grid settings:

   grids/grid0/grid_env.sh 
   grids/grid0/bind_us.sh 
   grids/grid0/bind_uk.sh 
   grids/grid0/site_us.sh 
   grids/grid0/site_uk.sh 
   grids/grid1/grid_env.sh 
   grids/grid1/bind_us.sh 
   grids/grid1/bind_uk.sh 
   grids/grid1/site_us.sh 
   grids/grid1/site_uk.sh 

Each grid directory must contain the following files:

grid_env.sh    This file contains grid definitions that apply to all of the 
               sites that make up the grid. You must enter the list of 
               sites in this file. As with the grid IDs, the sites listed
               in this file effectively become the sited IDs managed under
               this grid.

bind_<site>.sh This file contains IP addresses to bind servers, clients
               and gateways. It must be named with the "bind_" prefix
               followed by one of the site IDs defined in grid_env.sh.

site_<site>.sh This file inherits all of the settings in grid_env.sh.
               It contains site specific settings and may overwrite
               the settings made in grid_env.sh. It must be named with
               the "site_" prefix followed by one of the site IDs defined
               in grid_env.sh.

Running scripts (bin_(sh/bat)
-----------------------------

The scripts in the bin_(sh/bat) directories allow you to remotely manage all of Pado grids. For example, a single script can start many grids that are comprised of 10's of sites and 1000's of servers.

Pado scripts are grouped by prefixes as follows:

   restart_ Restarts all grids, a single grid, a single site, or a single server.
   start_   Starts all grids, a single grid, a single site, or a single server.
   stop_    Stops all grids, a single grid, a single site, or a single server.
   kill_    Kills all grids, a single grid, a single site, or a single server.
   check_   Displays status of all grids, a single grid, a single site, or a single server.

Pado scripts are also grouped by postfixes as follows:

   _all     All grids, sites, servers, locators.
   _grid    A single grid, all of its sites, all servers/locators/agents per site.
   _site    A single site, all of its servers/locators/agents.
   _locator A single locator.
   _ agent  A single agent.
   _server  A single server.

Examples: 
   restart_all Retarts all grids, sites, and servers.
   check_grid  Displays status of a single grid and all of its sites.
   kill_site   Kills a single site and all of its servers.

- Each script's usage can be viewed by running them with the '-?' option. For example,

  ./start_all -?

- To restart all grids ('-locators' is optional. It must be specified if locators are not running.)

  ./restart_all [-locators]

- To stop all grids (Specifying '-locators' will stop the locators also. If not specified, it will stop the servers only.)

  ./stop_all [-locators]

- To start a single site (By default, it starts the default grid's default site. If no options are provided then the example below starts the grid 'grid0' and the site 'us')

  ./start_site [-grid grid0|grid1] [-site us|uk] [-locators] 

- To stop a single site

  ./stop_site [-grid grid0|grid1] [-site us|uk] [-locators]

- To start a single server
 
  ./start_server [-num <server number>] [-site us|uk] [-gateway] [-rebalance]

- To clean up log and stats files of all grids, sites, and servers

  ./clean_all 

- To clean up log and stats files of a single site

  ./clean_site [-grid grid0|grid1] [-site us|uk] [-locators] [-persist]

- To start PadoShell

  ./pado

- To start GemFire gfsh

  ./start_gfsh


Running Pado Demo
-----------------

The Pado distribution includes a pre-configured demo that launches and manages multiple grids on localhost. To run it on a Unix platform, you must first set ssh auto-login. Please see the "Configuring SSH Auto Login" section at the bottom of this file.

Please see bin_sh/grids/README_GRIDS.txt for more examples and details.

Pado includes Microsoft's sample Northwind data. The default "mygrid" grid has been configured to ingest the "nw" data located in ${PADO_HOME}/data/nw. Follow the steps below to ingest the "nw" data.

1. Start the "mygrid" grid.
2. Copy ${PADO_HOME}/data/nw/import/* to ${PADO_HOME}/import/
3. Copy ${PADO_HOME}/data/nw/schema/* to ${PADO_HOME}/schema/
4. cd ${PADO_HOME}/bin_sh/tools
5. ./import_csv

Unix:

- cd bin_sh/

- Edit setenv.sh to set the correct JAVA_HOME and GEMFIRE paths.

- Edit all_env.sh to enter the desired number of grids. It has been preconfigured to run three grids "grid0", "grid1" and "grid2". The available grids are
  grid0 - The parent grid. It is configured to accept child grids grid1, grid2, grid3, grid4, and grid5. 
  grid1 - A grid2-dependent child grid that contains grid-partitioned data.
  grid2 - A grid1-dependent child grid that contains grid-partitioned data.
  grid3 - An autonomous child grid that contains xxx data.
  grid4 - An autonomous child grid that contains yyy data.
  grid5 - An autonomous child grid that contains zzz data.

- Each grid is independent of each other except for grid1 and grid2 which are configured to partition data across grids. Partitioning data across grids requires all participating grids to be reachable at all times. If any of the configured grids does not exist then Pado automatically prevents clients from accessing partitioned data. Note that clients can still continue to access non-partitioned data.

- Add data in the shared/position and shared/portfolio paths.

bin_sh/client/> ./temporal -gridpath shared/position -position
bin_sh/client/> ./temporal -gridpath shared/portfolio -portfolio

- Run PadoShell to build Lucene indexes

bin_sh/> ./pado -l localhost:20000 -a sys -u foo -e "temporal -lucene -all -grid grid1,grid2"

- Run desktop to see data in shared/position and shared/portfolio:

desktop/bin_win> desktop.bat


Running PadoShell
-----------------

This distribution includes PadoShell (bin_sh/pado and bin_win/pado) that provides Unix-like commands for managing Pado. To run PadoShell, you can run it from the bin_sh or bin_win directory as follows:

bin_sh> ./pado
bin_win> ./pado

You can also set PATH to include the bin_sh or bin_win directory so that pado and pado.bat can be executed from any where in the file system as follows:

Windows:
bin_win> setenv.bat
> pado

Unix:
bin_sh> . ./setenv.sh
> pado

PadoShell requires you to login first before most of the commands can be executed. You can login by providing the app ID, user name, and optionally password from the shell as follows:

> pado -a sys -u foo -p foo

Or you can use the "login"command from PadoShell as follows:

> pado
/mygrid> login -a sys -u foo -p foo

PadoShell supports scripts that contain Pado commands. You can execute a script by specifying the script file path as follows:

> pado -f pado_script_example.pado

Note that by convention, the pado script file extension is ".pado". You can also use the "script" command from PadoShell as follows:

> pado -l localhost:20000 -a sys -u foo -p foo
/pado> script pado_script_example.pado

Security - Pado
---------------

To run security tests, you must set the security properties in the server and client properties files as shown below.

etc/pado.properties (and/or etc/<grid-id>/pado.properties):
-------------------------------------------------------
security.publickey.filepath=../../security/publicKey.keystore
security.publickey.pass=<encrypted password>
security.keystore.path=../../security/<grid-id>/<grid-id>-user.keystore
security.keystore.alias=<grid-id>-user
security.keystore.pass=<encrypted password>

etc/client/pado.properties:
---------------------------
security.keystore.path=security/client/client-user.keystore
security.keystore.alias=client-user
security.keystore.pass=<encrypted password>

In addition to above, you must also enable Pado security in bin_sh/all_env.sh as follows (Note that if it is set to false then the security properties are ignored):
SECURITY_ENABLED=true

JUnit tests:
com.netcrest.pado.test.junit.security.SecurityTest

1. Create the private keystore to manage public certifcates. This must be created for all clients including child grids. The keystore file created contains a private/public key pair. This file must be deployed to the client application. Additionally, the property "security-keystorepath" in client.properties (or server.properties if child grid) must be assigned to the keystore file path. The following command creates a keystore file in security/client/<client-name>.keystore.

   bin_sh/security> create-keystore <client-name>

   Note: The Pado demo uses grid#-user for <client-name>, i.e., grid5-user.

2. Export a self-signed certificate to <client-name>.cert. This must be created for all clients including child grids. The following command extracts the client certificate file from security/client/<client-name>.keystore to security/export/<client-name>.cer.

   bin_sh/security> export-certificate <client-name>

3. Import the self-sigend certifcate to publicKey.store to be used by the servers. The following command imports the certificate generated in #2 above into the security/publicKey.keystore file. The example keystore included in the Pado distribution has the password, "pado123".

   bin_sh/security> import-certificate <client-name>

   Note: Once the certifate is imported, you can remove the security/export/<client-name>.cer file. This file is no longer needed as it is now imported in the security/publicKey.keystore file.

4. List the imported certificates. The example password is "pado123".

   bin_sh/security> list-certificates

5. Deploy the private keystore file to the client app environment. Place it in the security directory as follows:

   security/client> cp <client-name>.keystore <pado-installation-dir>/security/client

6. In order to use the newly created keystore, it must be defined in the client's properties file as shown above. This file path is typically etc/client/client.properties relative to the client's root directory, where <env-name> is the application's environment name such as "client" or a grid id, etc.

The client.properties is typically located in etc/client must contain the following security properties. ("desktop" is used as <client-name> in this example):

security-client-dhalgo=AES:128
security-client-auth-init=com.netcrest.pado.security.gemfire.PadoAuthInit.create
security-keystorepath=security/desktop.keystore
security-alias=desktop
security-keystorepass=desktop

The client must start with the following system properties:

System.setProperty("pado.security-keystorepath", "security/desktop.keystore");
System.setProperty("pado.security-alias", "desktop");
System.setProperty("pado.security-keystorepass", "dekstop");

OR

-Dpado.security-keystorepath=security/desktop.keystore -Dpado.security-alias=desktop -Dpado.security-keystorepass=desktop

Security - GemFire SSL
----------------------

To enable GemFire SSL, you must configure security properties in both server and client gfsecurity.properties files. The example below uses the LDAP example key store to establish SSL between GemFire peers and between GemFire servers and clients. See the "LDAP Configuration" section for details in creating the example key stores.

etc/<grid-id>/gfsecurity.properties:
------------------------------------
cluster-ssl-enabled=true
cluster-ssl-keystore-type=jks
cluster-ssl-keystore=../../ldap/example/ssl/server.keystore
cluster-ssl-keystore-password=secret
cluster-ssl-truststore=../../ldap/example/ssl/trusted.keystore
cluster-ssl-truststore-password=secret

etc/client/gfsecurity.properties:
---------------------------------
cluster-ssl-enabled=true
cluster-ssl-keystore-type=jks
cluster-ssl-truststore=../ldap/example/ssl/trusted.keystore
cluster-ssl-truststore-password=secret
# The following private key store shouldn't be required but GemFire
# requires it.
cluster-ssl-keystore=../ldap/example/ssl/server.keystore

Security - Creating Trust Store via Internet
--------------------------------------------

As a client to an authentication/authorization (Auth2) system, Pado adapters that use web services may require establishing trust relationship before it can access Auth2. This process involves obtaining the public key from Auth2 and creating a trust store file that contains the public key. To create a trust store file, run the following program:

   > cd bin_sh/tools
   > install_certificate

install_certificate requires the Auth2 host name and optional port number. If the port number is not sepcified then it assigns the default SSL port number 443. You may also need to specify the passphrase if Auth2 requires it. For example, the following command downloads the certificates from Symantec into security/pado.truststore.

   ./install_certificate https://www.verisign.com:443

LDAP Configuration
------------------

Pado includes an example LDAP plug-in that can be used as an example for creating a plug-in that is more suited for your application. 

To enable the example LDAP plug-in, follow the steps below:

1. Add the following properties in etc/pado.properties.
class.userAuthentication=com.netcrest.pado.security.server.LdapUserAuthentication
security.ldap.url=ldaps://localhost:10636
security.ldap.base=dc=newco,dc=com
security.ldap.user.filter=(&(objectClass=inetOrgPerson)(uid={0}))
security.ldap.memberof.base=ou=groups,ou=Pado,dc=newco,dc=com
security.ldap.memberof.filter=(&(objectClass=groupOfNames)(member={0}))

2. To enable SSL connection to your LDAP server, follow the steps below.

   - If GemFire SSL is disabled, then add the following line in bin_sh/setenv.sh:

     APP_PROPERTIES="-J-Djavax.net.ssl.trustStore=../../security/pado.keystore"

     where pado.keystore is the Pado trusted keystore file updated from the "Generating LDAP SSL Certificates" section.

   - If GemFire SSL is enabled, then javax.net.ssl.trustStore property optional. Pado automatically picks up pado.keystore if SSL is enabled.

3. Add ldap/example/newco.ldif into your LDAP server:
   3.1. For Apache Directory Server:
        - Create the "dc=newco,dc=com" partition using Apache Directory Studio.
        - Restart Apache Directory Server after the partion has been created.
        - From Apache Directory Studio, import ldap/example/newco.ldif.

There are numerous users defined in the example. Once LDAP has properly been configured, you must use the passwords shown below to login to Pado. 
   
   User   Password  Role
   ----   --------  ----
   admin  admin123  Administrator
   mygrid grid123   Grid-to-grid login, i.e, login to the parent grid
   grid0  grid123   Grid-to-grid login, i.e, login to the parent grid
   grid1  grid123   Grid-to-grid login, i.e, login to the parent grid
   grid2  grid123   Grid-to-grid login, i.e, login to the parent grid
   grid3  grid123   Grid-to-grid login, i.e, login to the parent grid
   grid4  grid123   Grid-to-grid login, i.e, login to the parent grid
   grid5  grid123   Grid-to-grid login, i.e, login to the parent grid
   test1  test123   Client
   test2  test123   Client
   test3  test123   Client
   test4  test123   Client

There are three (3) groups defined as follows:

   Group       Users
   -----       -----
   Admin       admin
   Developers  test1, test2, test3
   Accounting  test4
   Grids       mygrid, grid0, grid1, grid2, grid3, grid4, grid5

Generating LDAP SSL Certificates
--------------------------------

To enable SSL for the LDAP example included in the Pado distribution, follow the steps below:

Apache Directory Server:

1. Create and import private and public keys into the key store files: 
      ldap/example/ssl/server.keystore - holds the server private key 
      ldap/example/ssl/trusted.keystore - holds the trusted key for clients
      security/pado.keystore - holds the same trusted key for clients such as PadoShell

   cd ldap/example/ssl/
   ./create_key        -- create_key by default uses the host name for alias.
                       -- See ldap/example/ssl/README_EXAMPLE_SSL.txt for details.

2. Using Apache Directory Studio, enable LDAPS Server with SSL/Start TLS Keystore loaded with the ldap/example/ssl/server.keystore file. The password is 'secret'.

3. Restart Apache Directory Server.

4. From Apache Directory Studio, connect to the Apache Diretory Server. Allow the self-signed certificate when the self-signed certificate security warning dialog appears.

5. If GemFire SSL is disabled, then add the following line in bin_sh/setenv.sh to enable SSL connection to the LDAP server (See also the "LDAP Configuration" section):

   APP_PROPERTIES="-J-Djavax.net.ssl.trustStore=../../security/pado.keystore"

   Note that if GemFire SSL is enabled, then Pado automatically reads pado.keystore and therefore javax.net.ssl.trustStore is not required.

Running mygrid_ssl
------------------

This Pado distribution includes the mygrid_ssl grid that has been preconfigure with SSL. The only difference between mygrid and mygrid_ssl is in gfsecurity.properties found in etc/mygrid and etc/mygrid_ssl, respectively. This etc/mygrid_ssl/gfproperties contains GemFire SSL properties for a single site. For multiple sites with WAN SSL eanbled, run wan_ssl instead. wan_ssl enables SSL for all three (3) sites for bidirectional WAN replications. Follow the steps below to run the mygrid_ssl grid, PadoShell, and Pado Desktop.

Starting mygrid_ssl:

   - Edit bin_sh/all_env.sh to add the following line:
     GRIDS="mygrid_ssl"
     SECURITY_ENABLED=true

   - Edit bin_sh/setenv.sh to remove javax.net.ssl.trustStore property if it is set. This is optional.
     (See the "LDAP Configuration section for details.)

   - Start the grid:
     bin_sh> ./restart_site -locators

Starting PadoShell:

   - Edit etc/client/gfsecurity.properties to add the following lines:

     # Peers
     cluster-ssl-enabled=true
     cluster-ssl-keystore-type=jks
     cluster-ssl-truststore=security/pado.keystore
     cluster-ssl-truststore-password=changeit
     cluster-ssl-keystore=ldap/example/ssl/server.keystore
     cluster-ssl-keystore-password=secret

   - Start PadoShell

     bin_sh> ./pado -dir ..
     ...
     /> login -l localhost:20000 -a sys -u test1 -p test123     

Starting Pado Desktop:

   - From the Pado Desktop's root directory edit etc/gfsecurity.properties
     to add the following lines:
    
     cluster-ssl-enabled=true
     cluster-ssl-keystore-type=jks
     cluster-ssl-truststore=ldap/example/ssl/trusted.keystore
     cluster-ssl-truststore-password=secret
     cluster-ssl-keystore=ldap/example/ssl/server.keystore
     cluster-ssl-keystore-password=secret

   - Run Pado Desktop:
    
     bin_sh> ./desktop

Starting GemFire Pulse:

   - From bin_sh/ run show_config to get the URL info and then copy/paste it to your browser's URL prompt prefixed with https://.

     bin_sh> ./show_config
     browser prompt: https://<host-name>:20051/pulse

Staring GemFire gfsh:

   - From bin_sh, run start_gfsh as follows (Leave cihpers and protocols promts blank):

     bin_sh> ./start_gfsh
     gfsh> connect --use-ssl --locator=<host-name>:20000
     keystore: <pado-home>/ldap/example/ssl/server.keystore
     key-store-password: secret
     trust-store: <pado-home>/ldap/example/ssl/trusted.keystore
     trust-store-password: secret
     ciphers: 
     protocols:

Running wan_ssl
---------------

wan_ssl is identical to mygrid_ssl except that it is configured to replicate over the WAN. 

Starting wan_ssl:

   - Edit bin_sh/all_env.sh to add the following line:
     GRIDS="wan_ssl"
     SECURITY_ENABLED=true

   - Run ldap/example/ssl/create_key to create server.keystore and trusted.keystore
     ldap/example/ssl> ./create_key
       - Hit 'Return' to use the same key store password
       - Enter yes to "Trust this certifcate"

   - Edit bin_sh/setenv.sh to remove javax.net.ssl.trustStore property if it is set. This is optional.
     (See the "LDAP Configuration section for details.)

   - Start the "us" grid:
     bin_sh> ./restart_site -locators

   - Start the "uk" grid:
     bin_sh> ./restart_site -locators -site uk

   - Start the "jp" grid:
     bin_sh> ./restart_site -locators -site jp

Starting PadoShell, Pado Desktop, GemFire Pulse/gfsh:

   Follow the steps decribed in the "Running mygrid_ssl" section.


Running test script
-------------------

Under the script/ directory you will find the PadoShell script file, "run_test.pado". You can run this script to quickly check whether the installed grid is operational. It performs the following operations:

   1. Login to the grid
   2. Create tmp/temporal  -- temporal path
   3. Import a temporal data file found in data/test into tmp/temporal
   4. Query data from tmp/temporal and display the results
   5. Remove tmp/temporal

To run "run_test.pado", make sure you have entered a valid login information in etc/client/pado.properties. If you have not setup user authentication of your own, then simply run the script. It will use the default SSL and LDAP configuration described above. Once you are satisfied with the client authentication configuration, run the test script in the bin_sh directory as follows:

   bin_sh> ./pado -dir .. -f script/run_test.pado

Note that the above command specifies the '-dir' option. This option is required since the default client configuration expection clients to run from the pado home directory ($PADO_HOME). Since PadoShell is running in $PADO_HOME, the script file must be relative to $PADO_HOME as reflected by the '-f' option.

Recommended Gemfire partitioned region configuration
----------------------------------------------------

The following is the recommended partitioned region configuration for Pado grids. If a server crashes, it automatically recovers redundant data. If a new server starts or restarts, the new server merely joins the grid empty. To distribute data, a manual rebalancing is required. With this configuration, Pado properly synchronizes temporal and Lucene data. 

Caution: Because of auto-recovery of redundant data, if several servers crash, then the running servers may run out of memory, which may lead to a complete grid shutdown. This is unavoidable. If auto-recovery is disabled then it also presents a problem of its own: a possible loss of data. In some situations, disabling auto-recovery (recovery-delay="-1") may be more suited, however. 

IMPORTANT: For Pado, as shown below, always set startup-recovery-delay="-1" and recovery-delay="0" for all partitioned regions storing temporal data. These are required configuration. Without them, the Pado failover mechanism could potentially fail to properly rebuild temporal and Lucene indexes during redundancy recovery.

- In server.xml
  <partition-attributes redundant-copies="1" startup-recovery-delay="-1" recovery-delay="0" />

- In pado.xml
  <path-list build-lucene-index="true">
     <path name="foo" temporal-enabled="true" lucene-enabled="true">

- If a server crashes
    1. Gemfire recovers redundant data (recovery-delay="0").
    2. Pado automatically rebuilds its indexes (temporal & Lucene) for all temporal regions.

- If a new server starts or a crashed server restarts
    1. GemFire does not recover redundant data since the other running servers already have redundant copies.
    2. All of the newly started regions are empty.
    3. Pado attempts to rebuild its indexes but all of the regions are empty so there is no effect (build-lucene-index="true").
    4. You would need to manually run the 'rebalance' command in gfsh to rebalance data into the new server.
       It is important to rebalance all regions. If you wish to rebalance only select regions then make sure to include __pado/server which is required to reset the routing bucket IDs. 
       gfsh example: rebalance --include-region=/go/__pado/server,/go/master_ucn
    5. Pado automatically rebuilds Pado indexes during the rebalancing period.

- If the entire site is restarted
    1. All of persistent regions are reinstated with the persisted data.
    2. Because of build-lucene-index="true", Pado automatically rebuilds Pado indexes.

Rebalancing Gemfire partitioned regions
---------------------------------------

Always use PadoShell's 'rebalance' command to rebalance partitioned regions if possible.

If gfsh's 'rebalance' command is preferred then make sure to always rebalance the /<grid>/__pado/server region in addition to the regions specified by the --includeRegions option. Pado relies on this region's bucket IDs to target servers.  Note that for GemFire 8.1, the rebalance command does not recognize partitioned regions without the --includeRegions option. To rebalance all partitioned regions, your only choice is to use the --includeRegions option to list all partitioned regions. GemFire 8.2 fixes this bug.

Other configration (optional)
-----------------------------

The "start_server" script launches the cache server with the assigne port number. The default begins with 20001 where 200 is the locator port prefix. The prefix can be changed in grids/<grid-id>/site_<site-id>.(sh/bat)

You may need to insert select settings found in etc/template/server_template.xml into etc/<grid-id>/server.xml. Please see server_tempalte.xml for descriptions.

Troubleshotting Scripts
-----------------------

On Windows, if the Java executable has the path name that contains parenthesis, i.e., C:\Program Fies (x86)\Java\JDK\jdk1.6.0_38, then the sripts may not run. In that case, edit %GEMFIRE%\bin\gfsh.bat and encloss environment varable assigments with quotes as follows:
@set "GF_JAVA=%JAVA_HOME%\bin\java.exe"
@set "TOOLS_JAR=%JAVA_HOME%\lib\tools.jar"
@set "CLASSSPATH=%CLASSPATH%;%TOOLS_JARS%"
@set "JAVA_ARGS=%JAVA_ARGS%"

Configuring SSH Auto Login
--------------------------

To remotely run on multiple machines, you must enable ssh auto-login (password-less login). The easiest way to configure password-less login is to run the following command which automatially configures all servers listed in bin_sh/grids/<grid-id>/server_<site>.sh:

   cd $PADO_HOME/bin_sh/tools; ./setup_ssh

If for some reason, the setup_ssh command fails, then follow the manual steps below for each server.

1. Create a public ssh key, if you haven't done so already. This file is ~/.ssh/id_rsa.pub. If it doesn't exist, create one by running

   > ssh-keygen -t rsa 

   Please note that there are other types of keys, e.g. DSA instead of RSA. You might need to use another type but RSA is recommended.

2. Make sure your .ssh dir is 700:

   > chmod 700 ~/.ssh

3. Get your public ssh key on the server you want to login automatically. For example,
   
  > scp ~/.ssh/id_rsa.pub remoteuser@remoteserver.com: 

4. Append the contents of your public key to the ~/.ssh/authorized_keys and remove it. Important: This must be done on the server you just copied your public key to. Otherwise you wouldn't have had to copy it on your server. For example,
  
  > cat id_rsa.pub >> .ssh/authorized_keys 

5. Instead of steps 3 and 4, you can issue something like this:

  > cat ~/.ssh/id_rsa.pub | ssh -l remoteuser remoteserver.com 'cat >> ~/.ssh/authorized_keys'

6. Remove your public key from the home directory on the server. For example,

  > rm ~/id_rsa.pub 

You can now login without getting asked for a password. For example,

   ssh -l remoteuser remoteserver.com or ssh remoteuser@remoteserver.com

�2013-2015 Netcrest Technologies, LLC. All rights reserved.
