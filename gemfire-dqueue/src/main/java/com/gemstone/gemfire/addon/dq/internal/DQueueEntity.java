package com.gemstone.gemfire.addon.dq.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.gemstone.gemfire.addon.dq.DQueueAttributes;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

/**
 * Class <code>DQueueEntity</code> provides methods for use by all
 * <code>DQueue</code> entities.
 */
public abstract class DQueueEntity {

  /**
   * The GemFire <code>Cache</code>
   */
  protected final Cache cache;

  /**
   * The GemFire <code>DistributedSystem</code>
   */
  protected final InternalDistributedSystem distributedSystem;

  /**
   * The identifier of this <code>DQueue</code>
   */
  protected final String id;

  /**
   * The <code>DQueueAttributes</code> of this <code>DQueue</code>
   */
  protected final DQueueAttributes dqAttributes;

  /**
   * The <code>DistributedMember</code> id
   */
  protected final String memberId;

  /**
   * The <code>PartitionedRegion</code> containing the actual data
   */
  protected Region dqRegion;

  //////////////////////// Constructors //////////////////////

  /**
   * Constructor. Creates a new <code>DQueueEntity</code>.
   *
   * @param cache The <code>Cache</code>
   * @param id The id
   */
  public DQueueEntity(Cache cache, String id, DQueueAttributes dqAttributes) {
    this.cache = cache;
    this.id = id;
    this.dqAttributes = dqAttributes;
    this.distributedSystem = (InternalDistributedSystem) this.cache.getDistributedSystem();
    this.memberId = this.distributedSystem.getDistributedMember().getId();
    loadInstantiators();
  }

  ////////////////////// Instance Methods ///////////////////

  public String getId() {
    return this.id;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer
      .append(getClass().getSimpleName())
      .append("[")
      .append("id=")
      .append(this.id)
      .append("]");
    return buffer.toString();
  }

  protected Cache getCache() {
    return this.cache;
  }

  protected DQueueAttributes getAttributes() {
    return this.dqAttributes;
  }

  private void loadInstantiators() {
    Properties properties = new Properties();

    // Get the class loader
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
      if (loader == null) {
        throw new IllegalStateException("No classloader can be found while loading instantiators");
      }
    }

    // Load and parse the properties file
    String name = getClass().getPackage().getName().replace('.', '/') + "/DataSerializables.properties";
    InputStream is = loader.getResourceAsStream(name);
    if (is == null) {
      throw new IllegalStateException("Properties file named " + name + " cannot be found");
    }
    try {
      properties.load(is);
    } catch (IOException e) {
      throw new IllegalStateException("Properties file named " + name + " cannot be loaded: " + e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {}
    }

    // Iterate the entries in the properties file and register their instantiators
    Class[] parameterTypes = new Class[] {int.class};
    for (Iterator i = properties.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String className = (String) entry.getKey();
      int classId = Integer.parseInt((String) entry.getValue());
      try {
        Class cl = Class.forName(className);
        Method method = cl.getMethod("registerInstantiator", parameterTypes);
        method.invoke(null, classId);
      } catch (Exception e) {
        throw new IllegalStateException("The instantiator for class named " + className + " could not be invoked: " + e);
      }
    }
  }

  protected void logInfo(String message) {
    //System.out.println(message);
    this.cache.getLogger().info(message);
  }
}
