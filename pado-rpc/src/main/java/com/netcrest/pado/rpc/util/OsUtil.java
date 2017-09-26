package com.netcrest.pado.rpc.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.data.jsonlite.JsonLite;

/**
 * OsUtil provides methods for executing OS commands.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OsUtil
{
	/**
	 * Executes the command sequentially constructed from the specified command
	 * array. Each word in the command must be sequentially placed in the array.
	 * For example, <code>"ls -l /etc"</code> must be in passed in as
	 * <code>new String[] { "ls", "-l",
	 * "/etc" }</code>.
	 * 
	 * @param commandArray
	 *            Command array
	 * @return Output of the command execution
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws InterruptedException
	 *             Thrown if the command is interrupted by anot her thread
	 */
	public static CommandOutput executeCommand(String... commandArray) throws IOException, InterruptedException
	{
		return executeCommand(commandArray, null, (Map<String, String>) null, true);
	}

	/**
	 * Executes the command sequentially constructed from the specified command
	 * array. Each word in the command must be sequentially placed in the array.
	 * For example, <code>"ls -l /etc"</code> must be in passed in as
	 * <code>new String[] { "ls", "-l",
	 * "/etc" }</code>.
	 * 
	 * @param commandArray
	 *            Command array
	 * @param workingDir
	 *            Directory from which the command to be executed
	 * @return Output of the command execution
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws InterruptedException
	 *             Thrown if the command is interrupted by another thread
	 */
	public static CommandOutput executeCommand(String[] commandArray, String workingDir)
			throws IOException, InterruptedException
	{
		return executeCommand(commandArray, workingDir, (Map<String, String>) null, true);
	}

	/**
	 * Executes the command sequentially constructed from the specified command
	 * array. Each word in the command must be sequentially placed in the array.
	 * For example, <code>"ls -l /etc"</code> must be in passed in as
	 * <code>new String[] { "ls", "-l",
	 * "/etc" }</code>.
	 * 
	 * @param commandArray
	 *            Command array
	 * @param workingDir
	 *            Directory from which the command to be executed
	 * @param envMap
	 *            Environment variables
	 * @param isOutput
	 *            true to wait till the process ends and return its output,
	 *            false to immediately return upon process start. false always
	 *            returns null.
	 * 
	 * @return Output of the command execution. null if isOutput is false.
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws InterruptedException
	 *             Thrown if the command is interrupted by another thread
	 */
	public static CommandOutput executeCommand(String[] commandArray, String workingDir, Map<String, String> envMap,
			boolean isOutput) throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder(commandArray);
		if (envMap != null) {
			Set<String> keySet = envMap.keySet();
			for (String key : keySet) {
				pb.environment().put(key, (String) envMap.get(key));
			}
		}
		if (workingDir != null) {
			pb.directory(new File(workingDir));
		}
		Process proc = pb.start();
		if (isOutput) {
			// Wait till the process completes and collect the output
			String output = ProcessUtil.getProcessOutput(proc);
			int exitStatus = proc.waitFor();
			return new CommandOutput(output, exitStatus);
		} else {
			// Return immediately to avoid long-running processes
			return null;
		}
	}

	/**
	 * Executes the command sequentially constructed from the specified command
	 * array. Each word in the command must be sequentially placed in the array.
	 * For example, <code>"ls -l /etc"</code> must be in passed in as
	 * <code>new String[] { "ls", "-l",
	 * "/etc" }</code>.
	 * 
	 * @param commandArray
	 *            Command array
	 * @param workingDir
	 *            Directory from which the command to be executed
	 * @param envMap
	 *            Environment variables
	 * @param isOutput
	 *            true to wait till the process ends and return its output,
	 *            false to immediately return upon process start. false always
	 *            returns null.
	 * 
	 * @return Output of the command execution. null if isOutput is false.
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws InterruptedException
	 *             Thrown if the command is interrupted by another thread
	 */
	public static CommandOutput executeCommandJsonEnv(String[] commandArray, String workingDir, JsonLite envMap,
			boolean isOutput) throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder(commandArray);
		if (envMap != null) {
			Set<String> keySet = envMap.keySet();
			for (String key : keySet) {
				pb.environment().put(key, envMap.getString(key, null));
			}
		}
		if (workingDir != null) {
			pb.directory(new File(workingDir));
		}
		Process proc = pb.start();
		if (isOutput) {
			// Wait till the process completes and collect the output
			String output = ProcessUtil.getProcessOutput(proc);
			int exitStatus = proc.waitFor();
			return new CommandOutput(output, exitStatus);
		} else {
			// Return immediately to avoid long-running processes
			return null;
		}
	}

	/**
	 * CommandOutput contains the output of a command execution.
	 * 
	 * @author dpark
	 *
	 */
	public static class CommandOutput
	{
		private int status;
		private String output;

		public CommandOutput(String output, int status)
		{
			this.output = output;
			this.status = status;
		}

		/**
		 * Returns the command execution status. If 0, success; otherwise,
		 * failure.
		 */
		public int getStatus()
		{
			return this.status;
		}

		/**
		 * Returns the oupt of the command.
		 */
		public String getOutput()
		{
			return this.output;
		}
	}

	/**
	 * Returns Linux information extracted from the files in "/proc" directory.
	 * 
	 * @param props
	 *            Object to which Linux information to be stored. If null, then
	 *            it creates a new JSONObject and returns that object.
	 * @return The specified props object or a new JSONObject containing Linux
	 *         information.
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws InterruptedException
	 *             Thrown if the command is interrupted by another thread
	 */
	public static JsonLite getLinuxInfo(JsonLite props) throws IOException, InterruptedException
	{
		if (props == null) {
			props = new JsonLite();
		}
		CommandOutput co = executeCommand("grep", "-E",
				"(MemTotal:|MemFree:|MemAvailable:|SwapTotal:|SwapFree:|Cached:|SwapCached:)", "/proc/meminfo");
		String output = co.getOutput();
		if (output != null) {
			String[] lines = output.split("\n");
			String unit = null;
			String[] arrayOfString1;
			int j = (arrayOfString1 = lines).length;
			for (int i = 0; i < j; i++) {
				String line = arrayOfString1[i];
				String[] split = line.split(" ");
				if (split.length > 2) {
					String name = split[0].substring(0, split[0].length() - 1);
					String mem = split[(split.length - 2)];
					int memTotalInKb = Integer.parseInt(mem);
					unit = split[(split.length - 1)];
					props.put("Os." + name, memTotalInKb);
				}
			}
			props.put("Os.MemUnit", unit);
		}
		return props;
	}
}
