/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.internal;

/**
 * Constants contains all of Pado constant values used internally.
 * 
 * @author dpark
 * 
 */
public interface Constants
{
	public final static String PROP_SYSTEM_PADO = "pado.";
	public final static String PROP_JNLP_SYSTEM_PADO = "jnlp.pado.";

	/**
	 * Pado implementation data grid. This property determines the default
	 * package name of data grid implementation of IBiz classes.
	 */
	public final static String PROP_DATA_GRID = "data.grid";

	/**
	 * The default data grid name. This is used to determine the default package
	 * name for each IBiz implementation class.
	 */
	public final static String DEFAULT_DAGA_GRID = "gemfire";

	/**
	 * Default user certificate for embedding the encrypted password for
	 * security-password in the security properties file for client programs.
	 */
	public final static String DEFAULT_SECURITY_USER_CERTIFICATE_FILE_PATH = "security/user.cer";
	
	/**
	 * The underlying IMDG product name.
	 */
	public final static String PROP_PRODUCT_IMDG_NAME = "vendor.imdg.name";
	
	/**
	 * Trust store file path.
	 */
	public final static String PROP_SECURITY_TRUSTSTORE_FILE_PATH = "security.truststore.file.path";

	public final static String PROP_CLASS_LOGGER = "class.logger";
	public final static String PROP_CLASS_PADO_CLIENT_MANAGER = "class.padoClientManager";
	public final static String PROP_CLASS_PADO = "class.pado";
	public final static String PROP_CLASS_PADO_SERVER_MANAGER = "class.padoServerManager";
	public final static String PROP_CLASS_BIZ_MANAGER_FACTORY = "class.bizManagerFactory";
	public final static String PROP_CLASS_INFO_FACTORY = "class.infoFactory";
	public final static String PROP_CLASS_INTERNAL_FACTORY = "class.internalFactory";
	public final static String PROP_CLASS_LDAP_FACTORY = "class.ldapFactory";
	public final static String PROP_CLASS_PADO_BIZ_IMPL = "class.padoBizImpl";
	public final static String PROP_CLASS_TEMPORAL_FACTORY = "class.temporalFactory";
	public final static String PROP_CLASS_TEMPORAL_ATTACHMENT_SET_FACTORY = "class.temporal.attachmentSetFactory";
	public final static String PROP_CLASS_TEMPORAL_INTERNAL_FACTORY = "class.temporal.internalFactory";
	public final static String PROP_CLASS_TEMPORAL_MANAGER = "class.temporal.manager";
	public final static String PROP_CLASS_TEMPORAL_CLIENT_FACTORY = "class.temporal.clientFactory";
	public final static String PROP_CLASS_BULK_LOADER = "class.bulkLoader";
	public final static String PROP_CLASS_FILE_LOADER = "class.fileLoader";
	public final static String PROP_CLASS_VIRTUAL_PATH_ENGINE = "class.virtualPathEngine";

	public final static String PROP_CLASS_USER_AUTHENTICATION = "class.userAuthentication";
	public final static String PROP_CLASS_USER_CONTEXT = "class.userContext";
	public final static String PROP_CLASS_DATA_CONTEXT = "class.dataContext";
	public final static String PROP_CLASS_USER_INFO = "class.userInfo";
	public final static String PROP_CLASS_DATA_INFO = "class.dataInfo";

	public final static String PROP_CLASS_ID_START = "class.id.start";

	public final static String DEFAULT_PRODUCT_IMDG_NAME = "gemfire";
	
	public final static String DEFAULT_CLASS_LOGGER = "com.netcrest.pado.gemfire.util.GemfireLogger";
	public final static String DEFAULT_CLASS_PADO_CLIENT_MANAGER = "com.netcrest.pado.gemfire.GemfirePadoClientManager";
	public final static String DEFAULT_CLASS_PADO = "com.netcrest.pado.gemfire.GemfirePado";
	public final static String DEFAULT_CLASS_PADO_SERVER_MANAGER = "com.netcrest.pado.gemfire.GemfirePadoServerManager";
	public final static String DEFAULT_CLASS_BIZ_MANAGER_FACTORY = "com.netcrest.pado.gemfire.factory.GemfireBizManagerFactory";
	public final static String DEFAULT_CLASS_INFO_FACTORY = "com.netcrest.pado.gemfire.factory.GemfireInfoFactory";
	public final static String DEFAULT_CLASS_INTERNAL_FACTORY = "com.netcrest.pado.gemfire.factory.GemfireInternalFactory";
	public final static String DEFAULT_CLASS_TEMPORAL_FACTORY = "com.netcrest.pado.gemfire.factory.GemfireTemporalFactory";
	public final static String DEFAULT_CLASS_LDAP_FACTORY = "com.netcrest.pado.gemfire.factory.GemfireLdapFactory";
	public final static String DEFAULT_CLASS_PADO_BIZ_IMPL = "com.netcrest.pado.gemfire.biz.server.impl.gemfire.GemfirePadoBizImpl";
	public final static String DEFAULT_CLASS_TEMPORAL_ATTACHMENT_SET_FACTORY = "com.netcrest.pado.temporal.gemfire.GemfireAttachmentSetFactory";
	public final static String DEFAULT_CLASS_TEMPORAL_INTERNAL_FACTORY = "com.netcrest.pado.temporal.gemfire.GemfireTemporalInternalFactory";
	public final static String DEFAULT_CLASS_TEMPORAL_MANAGER = "com.netcrest.pado.temporal.gemfire.GemfireTemporalManager";
	public final static String DEFAULT_CLASS_TEMPORAL_CLIENT_FACTORY = "com.netcrest.pado.temporal.gemfire.GemfireTemporalClientFactory";
	public final static String DEFAULT_CLASS_BULK_LOADER = "com.netcrest.pado.gemfire.util.RegionBulkLoader";
	public final static String DEFAULT_CLASS_FILE_LOADER = "com.netcrest.pado.biz.file.CsvFileLoader";
	public final static String DEFAULT_CLASS_VIRTUAL_PATH_ENGINE = "com.netcrest.pado.gemfire.GemfireVirtualPathEngine";

	public final static String DEFAULT_CLASS_USER_AUTHENTICATION = "com.netcrest.pado.security.server.NoUserAuthentication";
	public final static String DEFAULT_CLASS_USER_CONTEXT = "com.netcrest.pado.gemfire.context.SimpleUserContextImpl";
	public final static String DEFAULT_CLASS_DATA_CONTEXT = "com.netcrest.pado.gemfire.context.SimpleDataContextImpl";
	public final static String DEFAULT_CLASS_USER_INFO = "com.netcrest.pado.gemfire.context.UserInfoImpl";
	public final static String DEFAULT_CLASS_DATA_INFO = "com.netcrest.pado.gemfire.context.DataInfoImpl";

	public final static String DEFAULT_CLASS_ID_START_DEFAULT = "1001";

	public final static String DEFAULT_CLASS_ROUTER = "com.netcrest.pado.internal.impl.DefaultGridRouter";

	public final static String DEFAULT_SITE_ID = "pado";

	// Default etc dir relative to the working directory of server.
	public final static String DEFAULT_ETC_DIR = "../../etc";
	// Default db dir relative to the working directory of server.
	public final static String DEFAULT_DB_DIR = "../../db";
	// Default scheduler dir relative to the working directory of server.
	public final static String DEFAULT_SCHEDULER_DIR = "../../data/scheduler";
	// Default data dump dir relative to the working directory of server.
	public final static String DEFAULT_DUMP_DIR = "dump";
	public final static String DEFAULT_PADO_PLUGINS_PROPERTY_FILE_NAME = "plugins.properties";
	public final static String DEFAULT_PADO_PROPERTY_FILE_NAME = "pado.properties";
	public final static String DEFAULT_SECURITY_AES_PUBLICKEY_FILEPATH = "../../security/publicKey.keystore";

	public final static String PROP_PADO_PLUGINS_PROPERTY_FILE = "plugins.properties";
	public final static String PROP_PADO_PROPERTY_FILE = "properties";

	// Pado statistics enabled. Default: false
	public final static String PROP_STATISTICS_ENABLED = "statistics.enabled";

	public final static String PROP_LICENSE_KEY = "license.key";
	public final static String PROP_DESKTOP_LICENSE_KEY = "desktop.license.key";
	public final static String PROP_PADO_ID = "grid.id";
	public final static String PROP_GRID_ID = "grid.id";
	public final static String PROP_SITE_ID = "site.id";
	public final static String PROP_SERVER_ID = "server.id";
	public final static String PROP_HOME_DIR = "home.dir";
	public final static String PROP_PLUGINS_DIR = "plugins.dir";
	public final static String PROP_ETC_DIR = "etc.dir";
	public final static String PROP_ETC_GRID_DIR = "etc.grid.dir";
	public final static String PROP_DUMP_DIR = "dump.dir";
	public final static String PROP_DB_DIR = "db.dir";
	public final static String PROP_SCHEDULER_DIR = "scheduler.dir";

	// User session idle timeout in msec. Default: 900000 msec (15 min)
	public final static String PROP_USER_IDLE_TIMEOUT = "user.idle.timeout";
	// Security enabled. If true then the client is required to supply
	// a valid user certificate.
	public final static String PROP_SECURITY_ENABLED = "security.enabled";
	// Encryption enabled. Default: false. If enabled then the AES private key
	// is generated during startup and sent to the clients as a login response.
	public final static String PROP_SECURITY_ENCRYPTION_ENABLE = "security.encryptionEnabled";
	// AES user certificate file path
	public final static String PROP_SECURITY_AES_USER_CERTIFICATE = "security.aes.userCertificate";
	// AES key size (default: 128. options: 128, 196, 256)
	public final static String PROP_SECURITY_AES_KEY_SIZE = "security.aes.keySize";
	// SSL (RSA) Public keys store
	public final static String PROP_SECURITY_AES_PUBLICKEY_FILEPATH = "security.publickey.filepath";
	// SSL (RSA) public key encrypted password
	public final static String PROP_SECURITY_AES_PUBLICKEY_PASS = "security.publickey.pass";
	
	public final static String PROP_SECURITY_PARENT_APPID = "security.parent.appid";
	public final static String PROP_SECURITY_PARENT_DOMAIN = "security.parent.domain";
	public final static String PROP_SECURITY_PARENT_USERNAME = "security.parent.username";
	public final static String PROP_SECURITY_PARENT_PASS = "security.parent.pass";
	
	// Client security properties
	public final static String PROP_SECURITY_KEYSTORE_FILE_PATH = "security.keystore.path";
	public final static String PROP_SECURITY_KEYSTORE_ALIAS = "security.keystore.alias";
	public final static String PROP_SECURITY_KEYSTORE_PASS = "security.keystore.pass";
	public final static String PROP_SECURITY_SIGNATURE_DATA = "security.signature.data";
	
	// Client login properties
	public final static String PROP_SECURITY_CLIENT_LOCATORS = "security.client.locators";
	public final static String PROP_SECURITY_CLIENT_APPID = "security.client.appId";
	public final static String PROP_SECURITY_CLIENT_DOMAIN = "security.client.domain";
	public final static String PROP_SECURITY_CLIENT_USER = "security.client.user";
	// Password must always be encrypted. No support for clear password.
	public final static String PROP_SECURITY_CLIENT_PASS = "security.client.pass"; 
	
	// Server security properties
	public final static String PROP_SECURITY_TOKEN = "security.token";
	
	// Lucene specifics
	// Max number of clauses per query. Default is 1024.
	public final static String PROP_LUCENE_MAX_CLAUSE_COUNT = "lucene.maxClauseCount";

	// Property to enable verbose (true/false). It logs data loader commands.
	// Default: false.
	public final static String PROP_LOADER_DATA_VERBOSE = "loader.data.verbose";
	// Properties file that contains data file to schema file mapping info
	public final static String PROP_LOADER_DATA_FILE_NAME_MAP_FILE_PATH = "loader.data.file.properties";
	// Data file loader schema directory path. default $PADO_HOME/data"
	public final static String PROP_LOADER_SCHEMA_FILE_DIR = "loader.schema.file.dir";
	// Whole data file directory path. default: "$PADO_HOME/data"
	public final static String PROP_LOADER_DATA_FILE_DIR = "loader.data.file.dir";
	// Output data file loader split data directory. This is the directory in
	// which the whole file is split.
	// default: "$PADO_HOME/run/<server-name>/data"
	public final static String PROP_LOADER_DATA_SPLIT_OUTPUT_FILE_DIR = "loader.data.split.output.file.dir";
	// Input data file loader split data directory. This is the directory
	// to which the split files are deployed to individual servers.
	// default: "$PADO_HOME/run/<server-name>/data"
	public final static String PROP_LOADER_DATA_SPLIT_INPUT_FILE_DIR = "loader.data.split.input.file.dir";
	// Loaded files are moved to this directory.
	public final static String PROP_LOADER_DATA_SPLIT_INPUT_FILE_DONE_DIR = "loader.data.split.input.file.done.dir";
	// Number of split files assigned to each server to load. default: 1
	public final static String PROP_LOADER_DATA_SPLIT_FILE_COUNT_PER_SERVER = "loader.data.split.file.count.per.server";
	// Default file loader class. This can be overwritten from schema files.
	public final static String PROP_LOADER_DATA_FILE_LOADER_CLASS = "loader.data.file.loader.class";
	// Default bulk loader class. This can be overwritten from schema files.
	public final static String PROP_LOADER_DATA_BULK_LOADER_CLASS = "loader.data.bulk.loader.class";
	// Bulk loader batch size. default: 5000
	public final static String PROP_LOADER_DATA_BULK_LOAD_BATCH_SIZE = "loader.data.bulk.loader.batch.size";
}
