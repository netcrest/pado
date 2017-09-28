package com.netcrest.pado.rpc.client;

import com.netcrest.pado.rpc.mqtt.client.MqttJsonRpcClient;

/**
 * RpcAgent starts the agent daemon service that runs indefinitely to receive and
 * execute RPC requests.
 * 
 * @author dpark
 *
 */
public class RpcAgent
{
	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   RpcAgent <server-id> [-?]");
		writeLine();
		writeLine("   RpcAgent runs indefinitely to receive and execute JSON RPC requests. Note that");
		writeLine("   only once instance of RpcAgent is allowed per data node identified by the specified");
		writeLine("   server ID.");
		writeLine("      <server-id> Unique Pado server ID representing the data node.");
		writeLine();
		writeLine("   Default: RpcMain <Pado extended JSON-RPC 2.0 request>");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args)
	{
		if (args == null || args.length == 0) {
			return;
		}

		String arg;
		String serverId = null;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else {
				serverId = arg;
			}
		}
		
		if (serverId == null) {
			System.err.println("Server ID required");
		}

		// RpcUtil.readRpcProperties();

		// Initialize the Mqtt mechanics
		MqttJsonRpcClient.initialize(serverId);

		// Go to sleep until the RPC has been stopped.
		try {
			while (MqttJsonRpcClient.getRpcClient().isStopped() == false) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			// ignore
		}

		System.exit(0);
	}
}
