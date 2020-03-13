Pado Import-Scheduler
=====================

The import-scheduler schedules the data importer based on the time information in the configuration files in the etc/ directory.

Getting started
---------------

By default, Pado has been configured with the $PADO_HOME/data/scheduler directory as the scheduler’s base directory. Copy the entire $PADO_HOME/data/template/scheduler directory to $PADO_HOME/data/. If you prefer to use another base directory then you can change it in $PADO_HOME/etc/client/scheduler.properties.

Once you have the $PADO_HOME/data/scheduler directory, follow the instructions shown below to create the scheduler and schema files.

Configuration (etc/)
--------------------

The import-scheduler config file must be in JSON string representation. The following attributes are supported (see template/etc for examples):

Driver: JDBC driver
Url: JDBC URL
User: Database user name
Password: Encrypted password. Use bin_sh/tools/encryptor to encrypt the password.
FetchSize: Integer value of result set fetch size. Default: 0 (no hint)
Delimiter: Column delimiter. Default: "	" (tab)
Null: Null value. Default: "'\\N'",
GridId: Grid ID
Paths: Array of grid paths
   Path: Grid path
   Columns: Ordered list of column names of the query result set
   Query: SQL statement
   Day: Comma separated list of days. Valid values: 
        Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
   Time: Comma separated time in 24 hour format, e.g., 05:00:00


Schema (schema/)
----------------

All schema files must be placed in the schema/ directory. The schema file name must be of the following naming conventions:

   <grid ID>.<hyphen separated grid path>.schema

where <grid ID> is the grid ID defined in the configuration file.
      <hypyen separated grid path> is the grid path name. Nested
      grid path must be separated by hypen. In other words, the
      path separator must be replaced with hyphen.

Example: 
   Given the grid ID, "mygrid" and the grid path "product/inventory", the
   file name must be:
      mygrid.product-inventory.schema

Note that in Pado, hyphens are now allowed in grid paths.


Running scheduler
-----------------

cd $PADO_HOME/bin_sh/tools
./import_scheduler -?

The above command show the usage of import_scheduler. Follow the instructions in the usage.
