package com.gemstone.gemfire.addon.function;

import java.util.Properties;

import com.gemstone.gemfire.addon.command.CommandResults;
import com.gemstone.gemfire.addon.command.CommandTask;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

/**
 * CommandTaskFunction is a delegator function that executes incoming
 * CommandTask objects.
 * 
 * @author dpark
 *
 */
public class CommandTaskFunction implements Function, Declarable
{
	private static final long serialVersionUID = 1L;

	@Override
	public void init(Properties props)
	{
	}

	@Override
	public void execute(FunctionContext context)
	{
		CommandResults results = new CommandResults();
		Object args = context.getArguments();
		if (args == null) {
			results.setCode(CommandResults.CODE_ERROR);
			results.setCodeMessage("CommandTask not passed in as an argument. Request aborted.");
		} else if (args instanceof CommandTask == false) {
			results.setCode(CommandResults.CODE_ERROR);
			results.setCodeMessage("Invalid argument type " + args.getClass().getName() + ". Must be CommandTask.");
		} else {
			CommandTask task = (CommandTask) args;
			results = task.runTask(null);
		}
		context.getResultSender().lastResult(results);
	}

	@Override
	public String getId()
	{
		return CommandTaskFunction.class.getSimpleName();
	}

	@Override
	public boolean hasResult()
	{
		return true;
	}

	@Override
	public boolean isHA()
	{
		return false;
	}

	@Override
	public boolean optimizeForWrite()
	{
		return true;
	}

}
