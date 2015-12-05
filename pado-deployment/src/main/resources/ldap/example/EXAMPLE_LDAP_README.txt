LDAP SSL
--------

This directory contains an example for creating SSL certifcates for establishing secure LDAP connections. Note that LDAP SSL is independent of GemFire SSL which requires its own settings. For our example, both environments are configured with the same example key stores created in this example, however.

1. Create and load the private key store to the LDAP server:

- cd ssl
- Run create_server_keystore to create server.keystore
- Import server.keystore from Apache Directory Studio
    - Right click on Connections/<server>
    - Select Open Configuration
    - Select the "LDA/LDAPS Servers" tab
    - Load the server.keystore file from the SSL/Start TLS Keystore option
    - Restart Apache Directory Service

2. Create the public (trusted) key store for LDAP clients (Note that LDAP clients are essentially grids as they perform authentiation and authorization):

- cd ssl
- Run create_trusted_keystore to create trusted.keystore

3. Configure LDAP in grids:

- See the "LDAP Configuration" section in README.txt for details on how to configure LDAP SSL for grids.
- See the "Security - GemFire SSL" section in README.txt for details on how to configure GemFire SSL with the same key stores.
