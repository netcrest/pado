## What is Pado?

Pado is a comprehensive software platform for building, managing and servicing true cloud stores on top of data grid products while ensuring performance, linear scalability, data ubiquity, and ease of use.

## Pado and Big Data - Grids within Grid

Pado is designed from the ground up to manage and service big data that can amount to 10’s and 100’s of terabytes.

Pado achieves this by federating one or more grids into one logical grid, which can also further nested by another logical grid. The top-level grid becomes the single entry point to which applications connect and access data. Upon a successful login to the top-level grid, Pado automatically connects the application directly to all relevant grids based on application and user credentials. Applications can also optionally and independently connect directly to any of the grids.

![Pado Login](/pado-javadoc/pado-login.png)

## Simple API

```
Pado.connect("locahost:20000"); // Connect
IPado pado = Pado.login("app-id", "domain-name", "user-name", "password".toCharArray()); // Login
ITemporalBiz temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class, "grid/path"); // Create IBiz
```

## Pado Features

Pado provides the data ubiquity service by versioning data structures and dynamically loading business classes called IBiz. Any code changes made can be deployed to all of the grids that make up Pado by simply dropping them into the Pado cloud.

Pado organizes data via its hierarchical namespace that works like the file system namespace. Each grid path represents a dataset, which can hold data in any form and size. A grid path can target objects, temporal data, real-time events/messages, files, images, transactions, spreadsheets, documents, web contents, etc.

Pado is equipped with numerous data services: Apache Lucene, GemFire OQL, temporal data, business rules engines, compliance foundation, context-based state machine, workflow state machine, batch processing, GUI framework, etc.
 
Pado retrieves millions of data records in sub-second and delivers them in streamed result sets with pagination support.

## Supported JDK Versions

Pado requires JDK 1.7 to compile and runs on JDK 1.7 and 1.8.

## Building Pado

1. Download or clone Pado from GitHub: http://github.com/netcrest/pado
2. Run "mvn install". 

The above command compiles all of Pado Maven modules and creates zip and tar distribution files in the pado-deployment/assembly directory. It also inflates the tar distribution file in the deploy/ directory for running and testing the Pado build. 

IMPORTANT: Note that it does not overwrite the existing files in the deploy/ directory, preserving the changes that you may have made.

## Installing Pivotal GemFire

In order to run Pado, you must install Pivotal GemFire, which is downloadable from the following Pivotal web site:

   https://network.pivotal.io/products/pivotal-gemfire

Pado supports GemFire 7.x and 8.x. Once installed, set the GEMFIRE environment variable to the GemFire installation root directory in bin_sh/setenv.sh as described below.


## Running Pado in a Build Environment

Upon successful build, run the default grid called "mygrid" as follows:

```
cd deploy/pado_<version>/bin_sh
<Edit setenv.sh and set JAVA_HOME, GEMFIRE>
./start_site -locators
```

Load mock data into mygrid as follows:

```
cd client
./temporal -all
```

Run PadoShell to view data in mygrid:

```
cd ..
./pado -dir ..
/mygrid> login
/mygrid> grid -s
/mygrid> ls -lR
/mygrid> less account
/mygrid> quit
```
