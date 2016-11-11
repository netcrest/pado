package com.netcrest.pado.internal.pql.antlr4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.internal.pql.antlr4.PathItem.JoinType;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class NodeFactory
{
	private PqlEvalListener pqlEvalListener;
	private boolean isAggregation;
	private ITemporalBizLink temporalBiz;
	private long validAtTime;
	private long asOfTime;
	private boolean isForceRebuildIndex = false;
	private int batchSize = 1000;

	public AliasNode rootNode;

	public NodeFactory(PqlEvalListener pqlEvalListener, boolean isAggregation, ITemporalBizLink temporalBiz,
			long validAtTime, long asOfTime)
	{
		this.pqlEvalListener = pqlEvalListener;
		this.isAggregation = isAggregation;
		this.temporalBiz = temporalBiz;
		this.validAtTime = validAtTime;
		this.asOfTime = asOfTime;
	}

	public boolean isAggregation()
	{
		return isAggregation;
	}

	public static class AliasNode
	{
		String alias;

		List<ValueNode> valueNodeList;

		public AliasNode(String alias)
		{
			this.alias = alias;
		}

		public List<ValueNode> getValueNodeList()
		{
			return valueNodeList;
		}

		public boolean exists(String pathAlias)
		{
			return exists(pathAlias, this);
		}

		private boolean exists(String pathAlias, AliasNode aliasNode)
		{
			if (aliasNode.alias.equals(pathAlias)) {
				return true;
			} else {
				if (aliasNode.valueNodeList == null || aliasNode.valueNodeList.size() == 0) {
					return false;
				}
				ValueNode valueNode = aliasNode.valueNodeList.get(0);
				if (valueNode.aliasNodeList == null || valueNode.aliasNodeList.size() == 0) {
					return false;
				} else {
					return exists(pathAlias, valueNode.aliasNodeList.get(0));
				}
			}
		}

		private AliasNode findFirstAliasNode(String alias)
		{
			AliasNode aliasNode = null;
			if (this.alias.equals(alias)) {
				aliasNode = this;
			} else {
				if (valueNodeList != null) {
					for (ValueNode valueNode : valueNodeList) {
						if (valueNode.aliasNodeList != null && valueNode.aliasNodeList.isEmpty() == false) {
							AliasNode an = valueNode.aliasNodeList.get(0);
							aliasNode = an.findFirstAliasNode(alias);
							break;
						}
					}
				}
			}

			return aliasNode;
		}

		private boolean leftExistsAfter(String leftAlias, String rightAlias)
		{
			AliasNode leftAliasNode = findFirstAliasNode(leftAlias);
			if (leftAliasNode == null) {
				return false;
			}
			AliasNode rightAliasNode = leftAliasNode.findFirstAliasNode(rightAlias);
			return rightAliasNode != null;
		}

		public List getResults()
		{
			return null;
		}

		public String toString()
		{
			return "alias=" + alias + ", valueNodeList=" + valueNodeList;
		}
	}

	public static class ValueNode
	{
		Object value;
		List<AliasNode> aliasNodeList;

		public ValueNode(Object value)
		{
			this.value = value;

		}

		public ValueNode(Object value, List<AliasNode> aliasNodeList)
		{
			this.value = value;
			this.aliasNodeList = aliasNodeList;
		}

		public List<AliasNode> getAliasNodeList()
		{
			return aliasNodeList;
		}

		public void setValue(Object value)
		{
			this.value = value;
		}

		public Object getValue()
		{
			return this.value;
		}

		public String toString()
		{
			return "value=" + value + ", aliasNodeList=" + aliasNodeList;
		}
	}

	public boolean isRootExist()
	{
		return rootNode != null && rootNode.valueNodeList != null;
	}

	public void joinRoot(JoinType joinType, String leftPathAlias, String leftColumnName, List<Map> leftList,
			String rightPathAlias, String rightColumnName, List<Map> rightList)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (Map leftMap : leftList) {
			Object leftValue = leftMap.get(leftColumnName);
			if (leftValue == null) {
				continue;
			}

			ValueNode leftValueNode = new ValueNode(leftMap);

			List<AliasNode> rightAliasNodeList = null;
			AliasNode rightAliasNode = null;

			Iterator<Map> rightIterator = rightList.iterator();
			while (rightIterator.hasNext()) {
				Map rightMap = rightIterator.next();
				Object rightValue = rightMap.get(rightColumnName);
				if (rightValue == null) {
					continue;
				}
				if (leftValue.equals(rightValue)) {

					if (rightAliasNode == null) {
						rightAliasNode = new AliasNode(rightPathAlias);
						rightAliasNode.valueNodeList = new ArrayList();
						rightAliasNodeList = new ArrayList<AliasNode>();
						rightAliasNodeList.add(rightAliasNode);
					}

					ValueNode rightValueNode = new ValueNode(rightMap);
					rightAliasNode.valueNodeList.add(rightValueNode);
				}
			}
			switch (joinType) {
			case INNER:
				if (rightAliasNode != null) {
					rootValueNodeList.add(leftValueNode);
					leftValueNode.aliasNodeList = rightAliasNodeList;
				}
				break;
			case LEFT:
				rootValueNodeList.add(leftValueNode);
				if (rightAliasNode != null) {
					leftValueNode.aliasNodeList = rightAliasNodeList;
				}
				break;
			// TODO: Add support for RIGHT, FULL
			}
		}
	}

	public void joinRootEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> leftList, String rightPathAlias, String rightColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> rightList)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (TemporalEntry<ITemporalKey, ITemporalData> leftTe : leftList) {
			Map leftMap = (Map) leftTe.getTemporalData().getValue();
			Object leftValue = leftMap.get(leftColumnName);
			if (leftValue == null) {
				continue;
			}

			ValueNode leftValueNode = new ValueNode(leftTe);

			List<AliasNode> rightAliasNodeList = null;
			AliasNode rightAliasNode = null;

			Iterator<TemporalEntry<ITemporalKey, ITemporalData>> rightIterator = rightList.iterator();
			while (rightIterator.hasNext()) {
				TemporalEntry<ITemporalKey, ITemporalData> rightTe = rightIterator.next();
				Map rightMap = (Map) rightTe.getTemporalData().getValue();
				Object rightValue = rightMap.get(rightColumnName);
				if (rightValue == null) {
					continue;
				}
				if (leftValue.equals(rightValue)) {

					if (rightAliasNode == null) {
						rightAliasNode = new AliasNode(rightPathAlias);
						rightAliasNode.valueNodeList = new ArrayList();
						rightAliasNodeList = new ArrayList<AliasNode>();
						rightAliasNodeList.add(rightAliasNode);
					}

					ValueNode rightValueNode = new ValueNode(rightTe);
					rightAliasNode.valueNodeList.add(rightValueNode);
				}
			}
			if (rightAliasNode != null) {
				rootValueNodeList.add(leftValueNode);
				leftValueNode.aliasNodeList = rightAliasNodeList;
			}
		}
	}

	public void joinRoot(JoinType joinType, String leftPathAlias, String leftColumnName, List<Map> leftList,
			String rightPathAlias, String rightColumnName, IScrollableResultSet<Map> rightScrollableResultSet)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (Map leftMap : leftList) {
			Object leftValue = leftMap.get(leftColumnName);
			if (leftValue == null) {
				continue;
			}

			ValueNode leftValueNode = new ValueNode(leftMap);

			List<AliasNode> rightAliasNodeList = null;
			AliasNode rightAliasNode = null;

			do {
				List<Map> rightList = rightScrollableResultSet.toList();
				Iterator<Map> rightIterator = rightList.iterator();
				while (rightIterator.hasNext()) {
					Map rightMap = rightIterator.next();
					Object rightValue = rightMap.get(rightColumnName);
					if (rightValue == null) {
						continue;
					}
					if (leftValue.equals(rightValue)) {

						if (rightAliasNode == null) {
							rightAliasNode = new AliasNode(rightPathAlias);
							rightAliasNode.valueNodeList = new ArrayList();
							rightAliasNodeList = new ArrayList<AliasNode>();
							rightAliasNodeList.add(rightAliasNode);
						}

						ValueNode rightValueNode = new ValueNode(rightMap);
						rightAliasNode.valueNodeList.add(rightValueNode);
					}
				}
			} while (rightScrollableResultSet.nextSet());
			if (rightAliasNode != null) {
				rootValueNodeList.add(leftValueNode);
				leftValueNode.aliasNodeList = rightAliasNodeList;
			}
		}
	}

	public void joinRoot(JoinType joinType, String leftPathAlias, String leftColumnName,
			IScrollableResultSet<Map> leftScrollableResultSet, String rightPathAlias, String rightColumnName,
			IScrollableResultSet<Map> rightScrollableResultSet)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		do {

			List<Map> leftList = leftScrollableResultSet.toList();
			for (Map leftMap : leftList) {
				Object leftValue = leftMap.get(leftColumnName);
				if (leftValue == null) {
					continue;
				}

				ValueNode leftValueNode = new ValueNode(leftMap);

				List<AliasNode> rightAliasNodeList = null;
				AliasNode rightAliasNode = null;

				do {
					List<Map> rightList = rightScrollableResultSet.toList();
					Iterator<Map> rightIterator = rightList.iterator();
					while (rightIterator.hasNext()) {
						Map rightMap = rightIterator.next();
						Object rightValue = rightMap.get(rightColumnName);
						if (rightValue == null) {
							continue;
						}
						if (leftValue.equals(rightValue)) {

							if (rightAliasNode == null) {
								rightAliasNode = new AliasNode(rightPathAlias);
								rightAliasNode.valueNodeList = new ArrayList();
								rightAliasNodeList = new ArrayList<AliasNode>();
								rightAliasNodeList.add(rightAliasNode);
							}

							ValueNode rightValueNode = new ValueNode(rightMap);
							rightAliasNode.valueNodeList.add(rightValueNode);
						}
					}
				} while (rightScrollableResultSet.nextSet());
				if (rightAliasNode != null) {
					rootValueNodeList.add(leftValueNode);
					leftValueNode.aliasNodeList = rightAliasNodeList;
				}
			}
		} while (leftScrollableResultSet.nextSet());
	}

	public void joinRootEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> leftList, String rightPathAlias, String rightColumnName,
			IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> rightScrollableResultSet)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (TemporalEntry<ITemporalKey, ITemporalData> leftTe : leftList) {
			Map leftMap = (Map) leftTe.getTemporalData().getValue();
			Object leftValue = leftMap.get(leftColumnName);
			if (leftValue == null) {
				continue;
			}

			ValueNode leftValueNode = new ValueNode(leftTe);

			List<AliasNode> rightAliasNodeList = null;
			AliasNode rightAliasNode = null;

			do {
				List rightList = rightScrollableResultSet.toList();
				Iterator<TemporalEntry<ITemporalKey, ITemporalData>> rightIterator = rightList.iterator();
				while (rightIterator.hasNext()) {
					TemporalEntry<ITemporalKey, ITemporalData> rightTe = rightIterator.next();
					Map rightMap = (Map) rightTe.getTemporalData().getValue();
					Object rightValue = rightMap.get(rightColumnName);
					if (rightValue == null) {
						continue;
					}
					if (leftValue.equals(rightValue)) {

						if (rightAliasNode == null) {
							rightAliasNode = new AliasNode(rightPathAlias);
							rightAliasNode.valueNodeList = new ArrayList();
							rightAliasNodeList = new ArrayList<AliasNode>();
							rightAliasNodeList.add(rightAliasNode);
						}

						ValueNode rightValueNode = new ValueNode(rightTe);
						rightAliasNode.valueNodeList.add(rightValueNode);
					}
				}
			} while (rightScrollableResultSet.nextSet());
			if (rightAliasNode != null) {
				rootValueNodeList.add(leftValueNode);
				leftValueNode.aliasNodeList = rightAliasNodeList;
			}
		}
	}

	public void joinRoot(JoinType joinType, String leftPathAlias, List<Map> leftList)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (Map leftMap : leftList) {
			ValueNode leftValueNode = new ValueNode(leftMap);
			rootValueNodeList.add(leftValueNode);
		}
	}

	public void joinRootEntries(String leftPathAlias, List<TemporalEntry<ITemporalKey, ITemporalData>> leftList)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;

		for (TemporalEntry<ITemporalKey, ITemporalData> te : leftList) {
			ValueNode leftValueNode = new ValueNode(te);
			rootValueNodeList.add(leftValueNode);
		}
	}

	public void joinRoot(JoinType joinType, String leftPathAlias, IScrollableResultSet<Map> leftScrollableResultSet)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;
		do {
			List<Map> leftList = leftScrollableResultSet.toList();
			for (Map leftMap : leftList) {
				ValueNode leftValueNode = new ValueNode(leftMap);
				rootValueNodeList.add(leftValueNode);
			}
		} while (leftScrollableResultSet.nextSet());
	}

	public void joinRootEntries(JoinType joinType, String leftPathAlias,
			IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> leftScrollableResultSet)
	{
		if (rootNode == null) {
			rootNode = new NodeFactory.AliasNode(leftPathAlias);
		}

		List<ValueNode> rootValueNodeList = new ArrayList();
		rootNode.valueNodeList = rootValueNodeList;
		do {
			List<TemporalEntry<ITemporalKey, ITemporalData>> leftList = leftScrollableResultSet.toList();
			for (TemporalEntry<ITemporalKey, ITemporalData> te : leftList) {
				ValueNode leftValueNode = new ValueNode(te);
				rootValueNodeList.add(leftValueNode);
			}
		} while (leftScrollableResultSet.nextSet());
	}

	public boolean isAliasExist(String alias)
	{
		if (rootNode == null) {
			return false;
		} else {
			if (rootNode.alias.equals(alias)) {
				return rootNode.valueNodeList != null;
			}
			return rootNode.exists(alias);
		}
	}

	private Set getAliasResults(String alias, AliasNode aliasNode, Set set)
	{
		if (aliasNode.alias.equals(alias)) {
			if (aliasNode.valueNodeList != null) {
				for (ValueNode valueNode : aliasNode.valueNodeList) {
					set.add(valueNode.value);
				}
			}
		} else {
			if (aliasNode.valueNodeList != null) {
				for (ValueNode valueNode : aliasNode.valueNodeList) {
					if (valueNode.aliasNodeList != null) {
						for (AliasNode an : valueNode.aliasNodeList) {
							getAliasResults(alias, an, set);
						}
					}
				}
			}
		}
		return set;
	}

	public List getAliasResults(String alias)
	{
		// TODO: Keep track of alias results to avoid this search
		if (isRootExist() == false) {
			return null;
		}
		Set set = new HashSet();
		set = getAliasResults(alias, rootNode, set);
		if (set.size() == 0) {
			return null;
		}
		return new ArrayList(set);
	}

	public boolean isEmpty()
	{
		return rootNode != null && rootNode.valueNodeList != null && rootNode.valueNodeList.isEmpty();
	}

	public void printRoot()
	{
		System.out.println("Root: " + rootNode.alias);
		List<ValueNode> valueNodeList = rootNode.getValueNodeList();
		for (ValueNode valueNode : valueNodeList) {
			System.out.println("       Value: " + valueNode);
			System.out.println("   AliasList: " + valueNode.aliasNodeList);
		}
	}

	public void joinEqualsConstant(JoinType joinType, String leftPathAlias, String leftColumnName, Object constant)
	{

		joinEqualsConstant(joinType, rootNode, leftPathAlias, leftColumnName, constant);
	}

	public void joinEqualsConstant(JoinType joinType, AliasNode aliasNode, String leftPathAlias, String leftColumnName,
			Object constant)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				Map map = (Map) valueNode.value;
				Object columnValue = map.get(leftColumnName);
				if (columnValue == null || columnValue.equals(constant) == false) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				}
			}
		} else {
			Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
			while (valueNodeIterator.hasNext()) {
				ValueNode valueNode = valueNodeIterator.next();
				if (valueNode.aliasNodeList != null) {
					Iterator<AliasNode> aliasIterator = valueNode.aliasNodeList.iterator();
					while (aliasIterator.hasNext()) {
						AliasNode an2 = aliasIterator.next();
						joinEqualsConstant(joinType, an2, leftPathAlias, leftColumnName, constant);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasIterator.remove();
								break;
							}
						}
					}
				}
				if (aliasNode.valueNodeList.isEmpty()) {
					switch (joinType) {
					case INNER:
						valueNodeIterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							Map map = (Map) valueNode.value;
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				}
			}
		}
	}

	public void joinEqualsConstantEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			Object constant)
	{

		joinEqualsConstantEntries(joinType, rootNode, leftPathAlias, leftColumnName, constant);
	}

	public void joinEqualsConstantEntries(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, Object constant)
	{

		if (aliasNode == null) {
			return;
		}

		if (aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				TemporalEntry<ITemporalKey, ITemporalData> te = (TemporalEntry<ITemporalKey, ITemporalData>) valueNode.value;
				Map map = (Map) te.getTemporalData().getValue();
				Object columnValue = map.get(leftColumnName);
				if (columnValue == null || columnValue.equals(constant) == false) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				}
			}
		} else {
			Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
			while (valueNodeIterator.hasNext()) {
				ValueNode valueNode = valueNodeIterator.next();
				if (valueNode.aliasNodeList != null) {
					Iterator<AliasNode> aliasIterator = valueNode.aliasNodeList.iterator();
					while (aliasIterator.hasNext()) {
						AliasNode an2 = aliasIterator.next();
						joinEqualsConstant(joinType, an2, leftPathAlias, leftColumnName, constant);
						if (an2.getValueNodeList().isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasIterator.remove();
								break;
							}
						}
					}
				}
				if (valueNode.aliasNodeList.isEmpty()) {
					switch (joinType) {
					case INNER:
						valueNodeIterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							Map map = (Map) valueNode.value;
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				}
			}
		}
	}

	private void removeRight(ValueNode leftValueNode, String leftColumnName, String rightPathAlias,
			String rightColumnName)
	{
		Map map = (Map) leftValueNode.value;
		Object leftColumnValue = map.get(leftColumnName);
		if (leftColumnValue == null) {
			return;
		} else {
			if (leftValueNode.aliasNodeList == null) {
				return;
			} else {
				Iterator<AliasNode> aliasNodeIterator = leftValueNode.aliasNodeList.iterator();
				while (aliasNodeIterator.hasNext()) {
					AliasNode aliasNode = aliasNodeIterator.next();
					if (aliasNode.alias.equals(rightPathAlias)) {
						if (aliasNode.valueNodeList == null) {
							aliasNodeIterator.remove();
						} else {
							Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
							while (valueNodeIterator.hasNext()) {
								ValueNode valueNode = valueNodeIterator.next();
								Map rightMap = (Map) valueNode.value;
								Object rightColumnValue = rightMap.get(rightColumnName);
								if (rightColumnValue == null || rightColumnValue.equals(leftColumnValue) == false) {
									valueNodeIterator.remove();
								}
							}
						}
					} else {
						if (aliasNode.valueNodeList != null) {
							Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
							while (valueNodeIterator.hasNext()) {
								ValueNode valueNode = valueNodeIterator.next();
								removeRight(valueNode, leftColumnName, rightPathAlias, rightColumnName);
								if (valueNode.aliasNodeList.isEmpty()) {
									valueNodeIterator.remove();
								}
							}
						}
					}
					if (aliasNode.valueNodeList == null || aliasNode.valueNodeList.isEmpty()) {
						aliasNodeIterator.remove();
					}
				}
			}
		}
	}

	public void joinEqualsExist(JoinType joinType, String leftPathAlias, String leftColumnName, String rightPathAlias,
			String rightColumnName)
	{
		if (rootNode.leftExistsAfter(leftPathAlias, rightPathAlias)) {
			joinEqualsExists(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName);
		} else {
			joinEqualsExists(joinType, rootNode, rightPathAlias, rightColumnName, leftPathAlias, leftColumnName);
		}
	}

	public void joinEqualsExists(JoinType joinType, AliasNode aliasNode, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName)
	{
		switch (joinType) {
		case INNER:
			break;
		default:
			return;
		}
		if (aliasNode != null && aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
			while (valueNodeIterator.hasNext()) {
				ValueNode leftValueNode = valueNodeIterator.next();
				Map map = (Map) leftValueNode.value;
				Object leftColumnValue = map.get(leftColumnName);
				if (leftColumnValue == null) {
					valueNodeIterator.remove();
				} else {
					removeRight(leftValueNode, leftColumnName, rightPathAlias, rightColumnName);
					if (leftValueNode.aliasNodeList.isEmpty()) {
						valueNodeIterator.remove();
					}
				}
			}
		} else {
			if (aliasNode.valueNodeList != null) {
				Iterator<ValueNode> valueNodeIterator = aliasNode.valueNodeList.iterator();
				while (valueNodeIterator.hasNext()) {
					ValueNode leftValueNode = valueNodeIterator.next();
					if (leftValueNode.aliasNodeList != null) {
						Iterator<AliasNode> aliasNodeIterator = leftValueNode.aliasNodeList.iterator();
						while (aliasNodeIterator.hasNext()) {
							AliasNode an = aliasNodeIterator.next();
							joinEqualsExists(joinType, an, leftPathAlias, leftColumnName, rightPathAlias,
									rightColumnName);
							if (an.valueNodeList.isEmpty()) {
								aliasNodeIterator.remove();
							}
						}
						if (leftValueNode.aliasNodeList.isEmpty()) {
							valueNodeIterator.remove();
						}
					} else {

						// Remove the value node if it has no alias nodes.
						// This is fir INNER join that has no right-side values
						// to compare.
						valueNodeIterator.remove();
					}
				}
			}

		}
	}

	public void joinEqualsRightResults(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName, List<Map> rightResults)
	{
		joinEqualsRightResults(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				rightResults);
	}

	public void joinEqualsLeftResults(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName, List<Map> leftResults)
	{
		joinEqualsLeftResults(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				leftResults);
	}

	public void joinEqualsLeftResults(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName, IScrollableResultSet<Map> leftScrollableResultSet)
	{
		joinEqualsLeftResults(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				leftScrollableResultSet);
	}

	public void joinEqualsRightEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> rightResults)
	{
		joinEqualsRightEntries(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				rightResults);
	}

	public void joinEqualsLeftEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName, List<TemporalEntry<ITemporalKey, ITemporalData>> leftResults)
	{
		joinEqualsLeftEntries(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				leftResults);
	}

	public void joinEqualsLeftEntries(JoinType joinType, String leftPathAlias, String leftColumnName,
			String rightPathAlias, String rightColumnName,
			IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> leftScrollableResultSet)
	{
		joinEqualsLeftEntries(joinType, rootNode, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				leftScrollableResultSet);
	}

	/**
	 * Invoked if the rightPathAlias does not exist
	 * 
	 * @param leftValueNode
	 * @param leftColumnValue
	 * @param rightPathAlias
	 * @param rightColumnName
	 * @param rightResults
	 * @return true if added, false if not added.
	 */
	private boolean addToEnd(ValueNode leftValueNode, Object leftColumnValue, String rightPathAlias,
			String rightColumnName, List<Map> rightResults)
	{
		AliasNode rightAliasNode = null;
		for (Map rightMap : rightResults) {
			Object rightColumnValue = rightMap.get(rightColumnName);
			if (leftColumnValue.equals(rightColumnValue)) {
				if (rightAliasNode == null) {
					rightAliasNode = new AliasNode(rightPathAlias);
					rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
					rightAliasNode.valueNodeList.add(new ValueNode(rightMap));
				} else {
					rightAliasNode.valueNodeList.add(new ValueNode(rightMap));
				}
			}
		}
		if (rightAliasNode == null) {
			return false;
		} else {
			addToEnd(leftValueNode, rightAliasNode);
			return true;
		}

	}

	private boolean addToEnd(ValueNode leftValueNode, Object leftColumnValue, String rightPathAlias,
			String rightColumnName, IScrollableResultSet<Map> rightScrollableResultSet)
	{
		if (rightScrollableResultSet == null) {
			return false;
		}
		AliasNode rightAliasNode = null;
		do {
			List<Map> rightResults = rightScrollableResultSet.toList();
			for (Map rightMap : rightResults) {
				Object rightColumnValue = rightMap.get(rightColumnName);
				if (leftColumnValue.equals(rightColumnValue)) {
					if (rightAliasNode == null) {
						rightAliasNode = new AliasNode(rightPathAlias);
						rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
						rightAliasNode.valueNodeList.add(new ValueNode(rightMap));
					} else {
						rightAliasNode.valueNodeList.add(new ValueNode(rightMap));
					}
				}
			}
		} while (rightScrollableResultSet.nextSet());
		if (rightAliasNode == null) {
			return false;
		} else {
			addToEnd(leftValueNode, rightAliasNode);
			return true;
		}

	}

	private void addToEnd(ValueNode valueNode, AliasNode rightAliasNode)
	{
		if (valueNode.aliasNodeList != null) {
			for (AliasNode aliasNode : valueNode.aliasNodeList) {
				if (aliasNode.valueNodeList != null) {
					for (ValueNode vn : aliasNode.valueNodeList) {
						addToEnd(vn, rightAliasNode);
					}
				}
			}
		} else {
			valueNode.aliasNodeList = new ArrayList<AliasNode>();
			valueNode.aliasNodeList.add(rightAliasNode);
		}
	}

	private boolean addToEndEntries(ValueNode leftValueNode, Object leftColumnValue, String rightPathAlias,
			String rightColumnName, List<TemporalEntry<ITemporalKey, ITemporalData>> rightResults)
	{
		AliasNode rightAliasNode = null;
		for (TemporalEntry<ITemporalKey, ITemporalData> rightTe : rightResults) {
			Map rightMap = (Map) rightTe.getTemporalData().getValue();
			Object rightColumnValue = rightMap.get(rightColumnName);
			if (leftColumnValue.equals(rightColumnValue)) {
				if (rightAliasNode == null) {
					rightAliasNode = new AliasNode(rightPathAlias);
					rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
					rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
				} else {
					rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
				}
			}
		}
		if (rightAliasNode == null) {
			return false;
		} else {
			addToEnd(leftValueNode, rightAliasNode);
			return true;
		}

	}

	private boolean addToEndEntries(ValueNode leftValueNode, Object leftColumnValue, String rightPathAlias,
			String rightColumnName,
			IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> rightScrollableResultSet)
	{
		AliasNode rightAliasNode = null;
		do {
			List<TemporalEntry<ITemporalKey, ITemporalData>> rightResults = rightScrollableResultSet.toList();
			for (TemporalEntry<ITemporalKey, ITemporalData> rightTe : rightResults) {
				Map rightMap = (Map) rightTe.getTemporalData().getValue();
				Object rightColumnValue = rightMap.get(rightColumnName);
				if (leftColumnValue.equals(rightColumnValue)) {
					if (rightAliasNode == null) {
						rightAliasNode = new AliasNode(rightPathAlias);
						rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
						rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
					} else {
						rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
					}
				}
			}
		} while (rightScrollableResultSet.nextSet());
		if (rightAliasNode == null) {
			return false;
		} else {
			addToEnd(leftValueNode, rightAliasNode);
			return true;
		}

	}

	public void joinEqualsRightResults(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName, List<Map> rightResults)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode leftValueNode = iterator.next();
				Map map = (Map) leftValueNode.value;
				Object leftColumnValue = map.get(leftColumnName);
				if (leftColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				} else {
					if (addToEnd(leftValueNode, leftColumnValue, rightPathAlias, rightColumnName,
							rightResults) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					Map map = (Map) valueNode.value;
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						for (Map map2 : rightResults) {
							Object columnValue2 = map2.get(leftColumnName);
							if (columnValue.equals(columnValue2)) {
								if (rightAliasNode == null) {
									rightAliasNode = new AliasNode(rightPathAlias);
									rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								} else {
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								}
							}
						}
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
								iterator.remove();
								break;
							case LEFT:
								if (isAggregation) {
									map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
								}
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Map map = (Map) valueNode.value;
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsRightResults(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, rightResults);
					}
					if (valueNode.aliasNodeList.isEmpty()) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}
			}
		}

	}

	public void joinEqualsRightEntries(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> rightResults)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode leftValueNode = iterator.next();

				TemporalEntry<ITemporalKey, ITemporalData> leftTe = (TemporalEntry<ITemporalKey, ITemporalData>) leftValueNode.value;
				Map map = (Map) leftTe.getTemporalData().getValue();
				Object leftColumnValue = map.get(leftColumnName);
				if (leftColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					}
				} else {

					if (addToEndEntries(leftValueNode, leftColumnValue, rightPathAlias, rightColumnName,
							rightResults) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					TemporalEntry<ITemporalKey, ITemporalData> leftTe = (TemporalEntry<ITemporalKey, ITemporalData>) valueNode.value;
					Map map = (Map) leftTe.getTemporalData().getValue();
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						for (TemporalEntry<ITemporalKey, ITemporalData> rightTe : rightResults) {
							Map map2 = (Map) rightTe.getTemporalData().getValue();
							Object columnValue2 = map2.get(leftColumnName);
							if (columnValue.equals(columnValue2)) {
								if (rightAliasNode == null) {
									rightAliasNode = new AliasNode(rightPathAlias);
									rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
									rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
								} else {
									rightAliasNode.valueNodeList.add(new ValueNode(rightTe));
								}
							}
						}
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
							case RIGHT:
								iterator.remove();
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsRightEntries(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, rightResults);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasNodeIterator.remove();
								break;
							}
						}
					}
					if (valueNode.aliasNodeList.isEmpty()) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					}
				}

			}
		}
	}

	public void joinEqualsLeftResults(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName, List<Map> leftResults)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode.alias.equals(rightPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode rightValueNode = iterator.next();
				Map map = (Map) rightValueNode.value;
				Object rightColumnValue = map.get(rightColumnName);
				if (rightColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				} else {
					if (addToEnd(rightValueNode, rightColumnValue, leftPathAlias, leftColumnName,
							leftResults) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					Map map = (Map) valueNode.value;
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						for (Map map2 : leftResults) {
							Object columnValue2 = map2.get(leftColumnName);
							if (columnValue.equals(columnValue2)) {
								if (rightAliasNode == null) {
									rightAliasNode = new AliasNode(rightPathAlias);
									rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								} else {
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								}
							}
						}
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
								iterator.remove();
								break;
							case LEFT:
								if (isAggregation) {
									map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
								}
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Map map = (Map) valueNode.value;
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsLeftResults(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, leftResults);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasNodeIterator.remove();
								break;
							}
						}
					}
					if (valueNode.aliasNodeList.isEmpty()) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}

			}
		}
	}

	public boolean joinEqualsLeftEntries(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName,
			List<TemporalEntry<ITemporalKey, ITemporalData>> leftResults)
	{
		if (aliasNode == null) {
			return false;
		}
		if (aliasNode.alias.equals(rightPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode rightValueNode = iterator.next();
				TemporalEntry<ITemporalKey, ITemporalData> rightTe = (TemporalEntry<ITemporalKey, ITemporalData>) rightValueNode.value;
				Map map = (Map) rightTe.getTemporalData().getValue();
				Object rightColumnValue = map.get(rightColumnName);
				if (rightColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					}
				} else {

					if (addToEndEntries(rightValueNode, rightColumnValue, leftPathAlias, leftColumnName,
							leftResults) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					}
				}
			}
			if (aliasNode.valueNodeList.isEmpty()) {
				return true;
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					TemporalEntry<ITemporalKey, ITemporalData> te = (TemporalEntry<ITemporalKey, ITemporalData>) valueNode.value;
					Map map = (Map) te.getTemporalData().getValue();
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						iterator.remove();
					} else {
						AliasNode rightAliasNode = null;
						for (TemporalEntry<ITemporalKey, ITemporalData> leftTe : leftResults) {
							Map map2 = (Map) leftTe.getTemporalData().getValue();
							Object columnValue2 = map2.get(leftColumnName);
							if (columnValue.equals(columnValue2)) {
								if (rightAliasNode == null) {
									rightAliasNode = new AliasNode(rightPathAlias);
									rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
									rightAliasNode.valueNodeList.add(new ValueNode(leftTe));
								} else {
									rightAliasNode.valueNodeList.add(new ValueNode(leftTe));
								}
							}
						}
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
							case LEFT:
								iterator.remove();
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					for (AliasNode an2 : valueNode.aliasNodeList) {
						return joinEqualsLeftEntries(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, leftResults);
					}
				}

			}
		}
		return false;

	}

	/**
	 * @param aliasNode
	 * @param leftPathAlias
	 * @param leftColumnName
	 * @param rightPathAlias
	 * @param rightColumnName
	 * @param leftScrollableResultSet
	 */
	public void joinEqualsLeftResults(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName,
			IScrollableResultSet<Map> leftScrollableResultSet)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode.alias.equals(rightPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode rightValueNode = iterator.next();
				Map map = (Map) rightValueNode.value;
				Object rightColumnValue = map.get(rightColumnName);
				if (rightColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				} else {

					if (addToEnd(rightValueNode, rightColumnValue, leftPathAlias, leftColumnName,
							leftScrollableResultSet) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					Map map = (Map) valueNode.value;
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						do {
							List<Map> leftResults = leftScrollableResultSet.toList();
							for (Map map2 : leftResults) {
								Object columnValue2 = map2.get(leftColumnName);
								if (columnValue.equals(columnValue2)) {
									if (rightAliasNode == null) {
										rightAliasNode = new AliasNode(rightPathAlias);
										rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
										rightAliasNode.valueNodeList.add(new ValueNode(map2));
									} else {
										rightAliasNode.valueNodeList.add(new ValueNode(map2));
									}
								}
							}
						} while (leftScrollableResultSet.nextSet());
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
								iterator.remove();
								break;
							case LEFT:
								if (isAggregation) {
									map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
								}
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Map map = (Map) valueNode.value;
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsLeftResults(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, leftScrollableResultSet);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasNodeIterator.remove();
								break;
							}
						}
					}
					if (valueNode.aliasNodeList.isEmpty()) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					}
				}
			}
		}
	}

	public void joinEqualsLeftEntries(JoinType joinType, AliasNode aliasNode, String leftPathAlias,
			String leftColumnName, String rightPathAlias, String rightColumnName,
			IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> leftScrollableResultSet)
	{
		if (aliasNode == null) {
			return;
		}
		if (aliasNode != null && aliasNode.alias.equals(rightPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode rightValueNode = iterator.next();
				TemporalEntry<ITemporalKey, ITemporalData> rightTe = (TemporalEntry<ITemporalKey, ITemporalData>) rightValueNode.value;
				Map map = (Map) rightTe.getTemporalData().getValue();
				Object rightColumnValue = map.get(rightColumnName);
				if (rightColumnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					}
				} else {

					if (addToEndEntries(rightValueNode, rightColumnValue, leftPathAlias, leftColumnName,
							leftScrollableResultSet) == false) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					TemporalEntry<ITemporalKey, ITemporalData> te = (TemporalEntry<ITemporalKey, ITemporalData>) valueNode.value;
					Map map = (Map) te.getTemporalData().getValue();
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						do {
							List<TemporalEntry<ITemporalKey, ITemporalData>> leftEntries = leftScrollableResultSet
									.toList();
							for (TemporalEntry<ITemporalKey, ITemporalData> leftTe : leftEntries) {
								Map map2 = (Map) leftTe.getTemporalData().getValue();
								Object columnValue2 = map2.get(leftColumnName);
								if (columnValue.equals(columnValue2)) {
									if (rightAliasNode == null) {
										rightAliasNode = new AliasNode(rightPathAlias);
										rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
										rightAliasNode.valueNodeList.add(new ValueNode(leftTe));
									} else {
										rightAliasNode.valueNodeList.add(new ValueNode(leftTe));
									}
								}
							}
						} while (leftScrollableResultSet.nextSet());
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
							case LEFT:
								iterator.remove();
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsLeftEntries(joinType, an2, leftPathAlias, leftColumnName, rightPathAlias,
								rightColumnName, leftScrollableResultSet);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasNodeIterator.remove();
								break;
							}
						}
					}
				}

				if (valueNode.aliasNodeList.isEmpty()) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					}
				}
			}

		}
	}

	/**
	 * If left alias does not exist
	 * 
	 * @param aliasNode
	 * @param leftPathAlias
	 * @param leftColumnName
	 * @param compareList
	 * @param rightPathAlias
	 * @return
	 */
	public void joinEqualsRight(JoinType joinType, AliasNode aliasNode, String leftPathAlias, String leftColumnName,
			List<Map> compareList, String rightPathAlias)
	{

		if (aliasNode != null && aliasNode.alias.equals(leftPathAlias)) {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				Map map = (Map) valueNode.value;
				Object columnValue = map.get(leftColumnName);
				if (columnValue == null) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				} else {
					AliasNode rightAliasNode = null;
					for (Map map2 : compareList) {
						Object columnValue2 = map2.get(leftColumnName);
						if (columnValue.equals(columnValue2)) {
							if (rightAliasNode == null) {
								rightAliasNode = new AliasNode(rightPathAlias);
								rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
								rightAliasNode.valueNodeList.add(new ValueNode(map2));
							} else {
								rightAliasNode.valueNodeList.add(new ValueNode(map2));
							}
						}
					}
					if (rightAliasNode == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					} else {
						if (valueNode.aliasNodeList == null) {
							valueNode.aliasNodeList = new ArrayList<AliasNode>();
						}
						valueNode.aliasNodeList.add(rightAliasNode);
					}
				}
			}
		} else {
			Iterator<ValueNode> iterator = aliasNode.valueNodeList.iterator();
			while (iterator.hasNext()) {
				ValueNode valueNode = iterator.next();
				if (valueNode.aliasNodeList == null) {
					// compare and add
					Map map = (Map) valueNode.value;
					Object columnValue = map.get(leftColumnName);
					if (columnValue == null) {
						switch (joinType) {
						case INNER:
							iterator.remove();
							break;
						case LEFT:
							if (isAggregation) {
								map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
							}
							break;
						}
					} else {
						AliasNode rightAliasNode = null;
						for (Map map2 : compareList) {
							Object columnValue2 = map2.get(leftColumnName);
							if (columnValue.equals(columnValue2)) {
								if (rightAliasNode == null) {
									rightAliasNode = new AliasNode(rightPathAlias);
									rightAliasNode.valueNodeList = new ArrayList<ValueNode>();
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								} else {
									rightAliasNode.valueNodeList.add(new ValueNode(map2));
								}
							}
						}
						if (rightAliasNode == null) {
							switch (joinType) {
							case INNER:
								iterator.remove();
								break;
							case LEFT:
								if (isAggregation) {
									map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
								}
								break;
							}
						} else {
							if (valueNode.aliasNodeList == null) {
								valueNode.aliasNodeList = new ArrayList<AliasNode>();
							}
							valueNode.aliasNodeList.add(rightAliasNode);
						}
					}
				} else {
					Map map = (Map) valueNode.value;
					Iterator<AliasNode> aliasNodeIterator = valueNode.aliasNodeList.iterator();
					while (aliasNodeIterator.hasNext()) {
						AliasNode an2 = aliasNodeIterator.next();
						joinEqualsRight(joinType, an2, leftPathAlias, leftColumnName, compareList, rightPathAlias);
						if (an2.valueNodeList.isEmpty()) {
							switch (joinType) {
							case INNER:
								aliasNodeIterator.remove();
								break;
							}
						}
					}
				}
				if (aliasNode.valueNodeList.isEmpty()) {
					switch (joinType) {
					case INNER:
						iterator.remove();
						break;
					case LEFT:
						if (isAggregation) {
							Map map = (Map) valueNode.value;
							map.put(pqlEvalListener.getToColumnName(leftPathAlias), null);
						}
						break;
					}
				}
			}
		}
	}

	public List<Map> queryPredicateValues(String pathAlias, String predicate)
	{
		return temporalBiz.getQueryValues(predicate, validAtTime, asOfTime);
	}

	public IScrollableResultSet queryPredicateValuesScrolled(String pathAlias, String predicate)
	{
		return temporalBiz.getValueResultSet(predicate, validAtTime, asOfTime, null, true, batchSize, isForceRebuildIndex);
	}

	public List<TemporalEntry<ITemporalKey, ITemporalData>> queryPredicateEntries(String pathAlias, String predicate)
	{
		return temporalBiz.getQueryEntries(predicate, validAtTime, asOfTime);
	}

	public IScrollableResultSet<TemporalEntry<ITemporalKey, ITemporalData>> queryPredicateEntriesScrolled(
			String pathAlias, String predicate)
	{
		return temporalBiz.getEntryResultSet(validAtTime, asOfTime, null, true, batchSize, isForceRebuildIndex);
	}

	public static List<Map> queryPredicate_test(String pathAlias, String predicate)
	{
		List<Map> list = new ArrayList();
		if (pathAlias.equals("m")) {
			list = queryMovement(pathAlias, predicate);
		} else if (pathAlias.equals("ad")) {
			if (predicate.equals("asset/asset_detail?assetTagID=1")) {
				list = queryAssetDetail_constant(pathAlias, predicate);
			} else {
				list = queryAssetDetail(pathAlias, predicate);
			}
		} else {
			list = queryTest(pathAlias, predicate);
		}
		return list;
	}

	private static List<Map> queryMovement(String pathAlias, String predicate)
	{
		List<Map> list = new ArrayList();
		for (int i = 1; i <= 4; i++) {
			Map map = new HashMap();
			map.put("USFEASID", i + "");
			map.put("PRICE", (double) i * .5d);
			map.put("TestID", "T" + i);
			list.add(map);
		}
		for (int i = 1; i <= 2; i++) {
			Map map = new HashMap();
			map.put("USFEASID", i + "");
			map.put("PRICE", (double) i * 1.5d);
			map.put("TestID", "T" + i);
			list.add(map);
		}
		return list;
	}

	// asset/asset_detail?assetTagID=1
	private static List<Map> queryAssetDetail_constant(String pathAlias, String predicate)
	{
		List<Map> list = new ArrayList();
		for (int i = 1; i <= 1; i++) {
			Map map = new HashMap();
			map.put("assetTagID", i + "");
			map.put("Count", i + 10);
			map.put("name", "parkie");
			list.add(map);
		}
		return list;
	}

	// asset/asset_detail ad
	private static List<Map> queryAssetDetail(String pathAlias, String predicate)
	{
		List<Map> list = new ArrayList();
		for (int i = 1; i <= 2; i++) {
			Map map = new HashMap();
			map.put("assetTagID", i + "");
			map.put("Count", i + 10);
			map.put("name", "no name");
			list.add(map);
		}
		for (int i = 2; i <= 5; i++) {
			Map map = new HashMap();
			map.put("assetTagID", i + "");
			map.put("Count", i + 10);
			map.put("name", "parkie");
			list.add(map);
		}
		return list;
	}

	// asset/test t
	private static List<Map> queryTest(String pathAlias, String predicate)
	{
		List<Map> list = new ArrayList();
		for (int i = 1; i <= 2; i++) {
			Map map = new HashMap();
			map.put("TestID", "T" + i);
			map.put("TestVal", i + 100 + "");
			map.put("name", "parkie");
			list.add(map);
		}
		for (int i = 3; i <= 4; i++) {
			Map map = new HashMap();
			map.put("TestID", "T" + i);
			map.put("TestVal", "101");
			map.put("name", "parkie");
			list.add(map);
		}
		for (int i = 5; i <= 6; i++) {
			Map map = new HashMap();
			map.put("TestID", "T" + i);
			map.put("TestVal", "10" + i);
			map.put("name", "parkie");
			list.add(map);
		}
		return list;
	}

	static void testNodes()
	{

		NodeFactory factory = new NodeFactory(null, true, null, -1, -1);

		// 1. Q1: Evaluate the right side first
		List<Map> rightResults = factory.queryPredicateValues("m", "asset/movement?");

		String leftPathAlias = "ad";
		String leftColumnName = "assetTagID";
		String rightPathAlias = "m";
		String rightColumnName = "USFEASID";

		System.out.println("Right(m) - asset/movement?:");
		for (Map map : rightResults) {
			System.out.println("   " + map);
		}

		// 2. Q1: Evaluate the left side by applying the right side results
		String pathAlias = "ad";
		factory.rootNode = new AliasNode(pathAlias);
		List<Map> leftResults = factory.queryPredicateValues("ad", "asset/asset_detail?assetTagID:(4 1 2 3)");
		System.out.println("Left(ad) - asset/asset_detail?assetTagID:(4 1 2 3):");
		for (Map map : leftResults) {
			System.out.println("   " + map);
		}
		factory.joinRoot(JoinType.INNER, leftPathAlias, leftColumnName, leftResults, rightPathAlias, rightColumnName,
				rightResults);
		factory.printRoot();

		// 3. Q2: Evaluate ad.name:parkie
		leftPathAlias = "ad";
		leftColumnName = "name";
		Object constantValue = "parkie";
		System.out.println("asset/asset_detail?name:parkie");
		factory.joinEqualsConstant(JoinType.INNER, leftPathAlias, leftColumnName, constantValue);
		factory.printRoot();

		// 4. Q3: Evaluate JOIN asset/test t ON t.name:ad.name
		leftPathAlias = "t";
		leftColumnName = "name";
		rightPathAlias = "ad";
		rightColumnName = "name";
		leftResults = factory.queryPredicateValues(leftPathAlias, "asset/test?name:(parkie)");
		System.out.println("asset/test?name:(parkie)");
		for (Map map : rightResults) {
			System.out.println("   " + map);
		}
		factory.joinEqualsLeftResults(JoinType.INNER, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName,
				leftResults);
		factory.printRoot();

		// 5. Q4: Evaluate t.TestVal:101
		leftPathAlias = "t";
		leftColumnName = "TestVal";
		rightPathAlias = "t";
		constantValue = "101";
		System.out.println("asset/test?TestVal:101");
		System.out.println("   compareValue=" + constantValue);
		factory.joinEqualsConstant(JoinType.INNER, leftPathAlias, leftColumnName, constantValue);
		factory.printRoot();

		// 6: Q5: Evaluate t.TestID:m.TestID
		leftPathAlias = "t";
		leftColumnName = "TestID";
		rightPathAlias = "m";
		rightColumnName = "TestID";
		System.out.println("t.TestID:m.TestID");
		factory.joinEqualsExist(JoinType.INNER, leftPathAlias, leftColumnName, rightPathAlias, rightColumnName);
		factory.printRoot();
	}

	public static void main(String[] args)
	{
		testNodes();

	}
}
