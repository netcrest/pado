package com.gemstone.gemfire.addon.command.task;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.addon.command.CommandResults;
import com.gemstone.gemfire.addon.command.CommandTask;
import com.gemstone.gemfire.addon.command.task.data.MemberInfo;
import com.gemstone.gemfire.addon.util.DataSerializerEx;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedMember;

/**
 * RegionDestroyTask destroys a remote region. Invoke this class using
 * the function service as follows:
 * <p>
 * <pre>
 * String regionPath = "/foo";
 * ResultCollector collector = FunctionService.onServers(pool)
 *      .withArgs(new RegionDestroyTask(regionPath)).execute("CommandTaskFunction");
 *</pre>
 * Note that this task destroys the specified region in the connected server 
 * and distributes it to other servers only if the region scope is not LOCAL.
 * <p>
 * Check CommandResults.getCode() to see the task execution status as follows:
 * <pre>
 * List<CommandResults> resultList = (List<CommandResults>)collector.getResult();
 * for (CommandResults commandResults : resultList) {
 *     System.out.println("code=" + commandResults.getCode() + ", message=" + commandResults.getCodeMessage());
 * }
 * </pre>
 * <p>
 * RegionDestroyTask.SUCCESS_DESTROYED for region successfully destroyed,
 * RegionDestroyTask.ERROR_REGION_DESTROY for an error creating region.
 * CommandResults.getDataObject() returns MemberInfo.
 * 
 * @author dpark
 */
public class RegionDestroyTask implements CommandTask, DataSerializable
{
	private static final long serialVersionUID = 1L;

	public static final byte ERROR_REGION_DESTROY = 1;

	private String regionFullPath;

	public RegionDestroyTask()
	{
	}

	/**
	 * Constructs a RegionDestroyTask object.
	 * 
	 * @param regionFullPath
	 *            The path of the region to destroy.
	 */
	public RegionDestroyTask(String regionFullPath)
	{
		this.regionFullPath = regionFullPath;
	}

	public CommandResults runTask(Object userData)
	{
		CommandResults results = new CommandResults();

		MemberInfo memberInfo = new MemberInfo();

		try {
			Cache cache = CacheFactory.getAnyInstance();
			Region region = cache.getRegion(regionFullPath);
			DistributedMember member = cache.getDistributedSystem().getDistributedMember();
			memberInfo.setHost(member.getHost());
			memberInfo.setMemberId(member.getId());
			memberInfo.setMemberName(cache.getName());
			memberInfo.setPid(member.getProcessId());

			results.setDataObject(memberInfo);

			if (region == null) {
				results.setCode(ERROR_REGION_DESTROY);
				results.setCodeMessage("Region undefined: " + regionFullPath);
			} else {
				synchronized (region) {
					region.destroyRegion();
					results.setCodeMessage("Region destroyed: " + regionFullPath);
				}
			}
		} catch (Exception ex) {
			results.setCode(ERROR_REGION_DESTROY);
			results.setCodeMessage(ex.getMessage());
			results.setException(ex);
		}

		return results;
	}

	public void fromData(DataInput input) throws IOException, ClassNotFoundException
	{
		regionFullPath = DataSerializerEx.readUTF(input);
	}

	public void toData(DataOutput output) throws IOException
	{
		DataSerializerEx.writeUTF(regionFullPath, output);
	}

}
