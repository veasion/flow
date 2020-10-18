package cn.veasion.flow.core;

import cn.veasion.flow.FlowManager;
import cn.veasion.flow.model.FlowConfig;
import cn.veasion.flow.model.FlowDefaultConfig;
import cn.veasion.flow.model.FlowNextConfig;
import cn.veasion.flow.model.FlowNextNode;
import cn.veasion.flow.model.FlowNodeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FlowNodeCore
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class FlowNodeCore {

    private IFlowService flowService;
    private Map<String, FlowConfig> flowConfigMap;
    private Map<String, FlowNextNode> flowNextNodeMap;

    public FlowNodeCore(IFlowService flowService) {
        this.flowService = flowService;
    }

    public FlowConfig getFlowConfig(String flow) {
        if (flowConfigMap == null) {
            throw new FlowException("Flow Config Not loaded.");
        }
        return flowConfigMap.get(flow);
    }

    public FlowNextNode getCurrentFlowNextNode(String flow, String currentNode) {
        if (flowNextNodeMap == null) {
            throw new FlowException("Flow Config Not loaded.");
        }
        return flowNextNodeMap.get(getFlowNextKey(flow, currentNode));
    }

    public List<FlowNextNode> getFlowNextNodes(String flow, String currentNode) {
        FlowNextNode flowNextNode = getCurrentFlowNextNode(flow, currentNode);
        return flowNextNode != null ? flowNextNode.getNextNodes() : null;
    }

    public boolean isLoaded() {
        return flowConfigMap != null;
    }

    public synchronized void reload() {
        flowConfigMap = new HashMap<>();
        List<FlowNodeConfig> flowNodeConfigs = flowService.queryFlowNodeConfig();
        List<FlowDefaultConfig> flowDefaultConfigs = flowService.queryFlowDefaultConfig();
        List<FlowNextConfig> flowNextConfigs = flowService.queryFlowNextConfig();
        Map<String, FlowNodeConfig> flowNodeConfigMap = flowNodeConfigs.stream().collect(Collectors.toMap(FlowNodeConfig::getCode, o -> o, (v1, v2) -> v1));
        Map<String, FlowDefaultConfig> flowDefaultConfigMap = flowDefaultConfigs.stream().collect(Collectors.toMap(FlowDefaultConfig::getFlow, o -> o, (v1, v2) -> v1));
        Map<String, List<FlowNextConfig>> flowNextConfigsMap = new HashMap<>();
        for (FlowNextConfig flowNextConfig : flowNextConfigs) {
            String key = getFlowNextKey(flowNextConfig.getFlow(), flowNextConfig.getNode());
            List<FlowNextConfig> list = flowNextConfigsMap.getOrDefault(key, new ArrayList<>());
            list.add(flowNextConfig);
            flowNextConfigsMap.put(key, list);
        }
        for (FlowNextConfig flowNextConfig : flowNextConfigs) {
            String key = getFlowNextKey(flowNextConfig.getNextFlow(), flowNextConfig.getNextNode());
            if (!flowNextConfigsMap.containsKey(key)) {
                FlowNextConfig nextConfig = new FlowNextConfig();
                nextConfig.setFlow(flowNextConfig.getNextFlow());
                nextConfig.setNode(flowNextConfig.getNextNode());
                flowNextConfigsMap.put(key, Collections.singletonList(nextConfig));
            }
        }
        for (String flow : flowDefaultConfigMap.keySet()) {
            FlowDefaultConfig defaultConfig = flowDefaultConfigMap.get(flow);
            String startNode = defaultConfig.getStartNode();
            String errorNode = defaultConfig.getErrorNode();
            FlowConfig flowConfig = new FlowConfig();
            flowConfig.setFlow(flow);
            if (startNode != null && !"".equals(startNode)) {
                List<FlowNextConfig> startNodes = flowNextConfigsMap.get(getFlowNextKey(flow, startNode));
                if (startNodes == null || startNodes.isEmpty()) {
                    throw new FlowConfigException(String.format("flow: %s startNode: %s Not Found.", flow, startNode));
                }
                // TODO next 有问题，应该是 [{node, node_next}, {node, node_next}]
                //  而不是 [{node_next, next_next}, {node_next, next_next}]
                flowConfig.setStartNode(buildFlowNextNode(flowNodeConfigMap, flowNextConfigsMap, startNodes.get(0)));
            }
            if (errorNode != null && !"".equals(errorNode)) {
                List<FlowNextConfig> errorNodes = flowNextConfigsMap.get(getFlowNextKey(flow, errorNode));
                if (!flowNodeConfigMap.containsKey(errorNode)) {
                    throw new FlowConfigException(String.format("flow: %s errorNode: %s Not Found.", flow, errorNode));
                } else if (errorNodes != null && !errorNodes.isEmpty()) {
                    flowConfig.setErrorNode(buildFlowNextNode(flowNodeConfigMap, flowNextConfigsMap, errorNodes.get(0)));
                } else {
                    flowConfig.setErrorNode(buildOneFlowNextNode(flow, flowNodeConfigMap.get(errorNode)));
                }
            }
            flowConfig.setDefaultConfig(defaultConfig);
            flowConfigMap.put(flow, flowConfig);
        }
    }

    private FlowNextNode buildOneFlowNextNode(String flow, FlowNodeConfig node) {
        FlowNextConfig flowNextConfig = new FlowNextConfig();
        flowNextConfig.setNode(node.getCode());
        flowNextConfig.setFlow(flow);
        FlowNextNode nextNode = new FlowNextNode();
        nextNode.setNode(node);
        nextNode.setFlowNextConfig(flowNextConfig);
        if (!FlowManager.YES.equals(node.getIsVirtual())) {
            nextNode.setFlowNode(flowService.getFlowNode(node.getCode()));
        }
        return nextNode;
    }

    private FlowNextNode buildFlowNextNode(Map<String, FlowNodeConfig> flowNodeConfigMap, Map<String, List<FlowNextConfig>> flowNextConfigsMap, FlowNextConfig flowNextConfig) {
        FlowNodeConfig node = flowNodeConfigMap.get(flowNextConfig.getNode());
        if (node == null) {
            throw new FlowConfigException(String.format("node: %s, FlowNodeConfig Not Found.", flowNextConfig.getNode()));
        }
        FlowNextNode flowNextNode = new FlowNextNode();
        flowNextNode.setNode(node);
        flowNextNode.setFlowNextConfig(flowNextConfig);
        if (!FlowManager.YES.equals(node.getIsVirtual())) {
            flowNextNode.setFlowNode(flowService.getFlowNode(node.getCode()));
        }
        String nextFlow = flowNextConfig.getNextFlow();
        String nextNode = flowNextConfig.getNextNode();
        if (nextFlow != null && !"".equals(nextFlow)
                && nextNode != null && !"".equals(nextNode)) {
            String flowNextKey = getFlowNextKey(nextFlow, nextNode);
            List<FlowNextConfig> list = flowNextConfigsMap.get(flowNextKey);
            if (!flowNodeConfigMap.containsKey(nextNode)) {
                throw new FlowConfigException(String.format("nextFlow: %s nextNode: %s Not Found.", nextFlow, nextNode));
            }
            if (list != null && !list.isEmpty()) {
                List<FlowNextNode> _nextList = new ArrayList<>();
                flowNextNode.setNextNodes(new ArrayList<>());
                for (FlowNextConfig obj : list) {
                    FlowNextNode _next = buildFlowNextNode(flowNodeConfigMap, flowNextConfigsMap, obj);
                    _next.setPrev(flowNextNode);
                    _nextList.add(_next);
                }
                flowNextNode.setNextNodes(_nextList);
            }
        }
        return checkLoop(flowNextNode);
    }

    private FlowNextNode checkLoop(final FlowNextNode nextNode) {
        String key;
        FlowNextConfig flowNextConfig;
        FlowNextNode node = nextNode;
        Set<String> flowNodes = new HashSet<>();
        do {
            flowNextConfig = node.getFlowNextConfig();
            key = getFlowNextKey(flowNextConfig.getFlow(), flowNextConfig.getNode());
            if (flowNodes.contains(key)) {
                throw new FlowConfigException(String.format("flow: %s, node: %s is loop", flowNextConfig.getFlow(), flowNextConfig.getNode()));
            }
            flowNodes.add(key);
        } while ((node = node.getPrev()) != null);
        return nextNode;
    }

    private String getFlowNextKey(String flow, String node) {
        return flow + ";" + node;
    }

}
