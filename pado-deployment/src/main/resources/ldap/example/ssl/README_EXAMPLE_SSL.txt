This directory ($PADO_HOME/ldap/example/ssl) contains a set of scripts that manage RSA certificates using host names as their aliases. There are three files that are affected by the scripts:

   server.keystore - Server private keys
   trusted.keystore - Client trusted keys exported from server.keystore
   ../../../security/pado.keystore - Pado trusted key store

The keystore files and passwords are defined in setenv.sh.

Specify the '-?' option to see the usage of each script. By default, each script uses the host name as the alias if not specified with the '-alias' option.
