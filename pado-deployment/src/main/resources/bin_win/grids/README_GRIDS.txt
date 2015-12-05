The "bin_sh/grids" directory contains grid parameter definitions retrieved by the Pado management scripts in the "bin_sh" directory. 

The Pado distribution includes the following example grid definitions which can be used to configure your grids:

Example 1
---------

In this example, a single, child-less grid is configured with 3 sites and 2 servers per site.

mygrid - This is an orphan grid that has neither parent nor child grids. It is a stand-alone data grid that can join another grid as a child grid if that grid provides access rights to this grid.

Example 2
---------

In this example, the automous grid called "mock" is configured with 3 sites and 2 servers per site. 

mock - An automous grid for inserting mocked temporal data in the form of JsonLite. Run bin_sh/client/temporal to bulk-load temporal data into the account, account_detail, bank, position, and portfolio paths as follows:

   > cd bin_sh/client/temporal
   > ./temporal -locators localhost:21000
   > ./temporal -locators localhost:21000 -buildlucene

Example 3
---------

In this example, you will run up to 6 grids. Each grid is configured with 3 sites and 2 servers per site. The parent grid, grid0, is configured to use its child grids, grid1 and grid2, to partition shared/portfolio and shared/position grid paths. This means grid1 and grid2 must exist before data can be written to those grid paths. The remaining grids are completely autonomous and they can be run independently.

Parent grid:

grid0 - This is a parent grid that is configured to parent five child grids: grid1, grid2, grid3, grid4, and grid5. Child grids are typically autonomous such that they can join or depart a parent grid without disrupting the parent or each other. A grid may, however, require others if data is partitioned across grids. In that case, the all partitioned grids must exist before clients can access data from the partitioned grid path.

Child grids:

grid1 - A child grid with partitioned grid paths. Requires grid2.
grid2 - A child grid with partitioned grid paths. Requires grid1.
grid3 - A child grid with no other grid dependencies.
grid4 - A child grid with no other grid dependencies.
grid5 - A child grid with no other grid dependencies.

Please see etc/grid?/pado.xml for configuration details. Note that the parent grid defines all child grids in its configuratio nfile (etc/grid0/pado.xml).

