package cn.veasion.flow.chart;

import cn.veasion.flow.core.FlowException;
import cn.veasion.flow.core.IFlowService;
import cn.veasion.flow.model.FlowDefaultConfig;
import cn.veasion.flow.model.FlowNextConfig;
import cn.veasion.flow.model.FlowNodeConfig;
import cn.veasion.flow.model.FlowRun;
import cn.veasion.flow.model.FlowRunTrack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * MermaidHelper
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class MermaidHelper {

    public static void main(String[] args) {
        List<FlowNextConfig> nextConfigs = new LinkedList<>();
        FlowNextConfig a = new FlowNextConfig();
        a.setFlow("test");
        a.setNode("a");
        a.setNextNode("b");
        FlowNextConfig b1 = new FlowNextConfig();
        b1.setFlow("test");
        b1.setNode("b");
        b1.setCond("yes");
        b1.setNextNode("c");
        FlowNextConfig b2 = new FlowNextConfig();
        b2.setFlow("test");
        b2.setNode("b");
        b2.setCond("no");
        b2.setNextNode("h");
        FlowNextConfig c1 = new FlowNextConfig();
        c1.setFlow("test");
        c1.setNode("c");
        c1.setCond("yes");
        c1.setNextNode("d");
        FlowNextConfig c2 = new FlowNextConfig();
        c2.setFlow("test");
        c2.setNode("c");
        c2.setCond("no");
        c2.setNextNode("f");
        FlowNextConfig d = new FlowNextConfig();
        d.setFlow("test");
        d.setNode("d");
        d.setNextNode("e");
        FlowNextConfig e = new FlowNextConfig();
        e.setFlow("test");
        e.setNode("e");
        e.setNextNode("g");
        FlowNextConfig f = new FlowNextConfig();
        f.setFlow("test");
        f.setNode("f");
        f.setNextNode("g");
        FlowNextConfig g = new FlowNextConfig();
        g.setFlow("test");
        g.setNode("g");
        g.setNextNode("h");
        FlowNextConfig h1 = new FlowNextConfig();
        h1.setFlow("test");
        h1.setNode("h");
        h1.setCond("yes");
        h1.setNextNode("i");
        FlowNextConfig h2 = new FlowNextConfig();
        h2.setFlow("test");
        h2.setNode("h");
        h2.setCond("no");
        h2.setNextNode("j");
        FlowNextConfig i = new FlowNextConfig();
        i.setFlow("test");
        i.setNode("i");
        i.setNextNode("k");
        FlowNextConfig j = new FlowNextConfig();
        j.setFlow("test");
        j.setNode("j");
        j.setNextNode("k");
        nextConfigs.add(a);
        nextConfigs.add(b1);
        nextConfigs.add(b2);
        nextConfigs.add(c1);
        nextConfigs.add(c2);
        nextConfigs.add(d);
        nextConfigs.add(e);
        nextConfigs.add(f);
        nextConfigs.add(g);
        nextConfigs.add(h1);
        nextConfigs.add(h2);
        nextConfigs.add(i);
        nextConfigs.add(j);
        StringBuilder styles = new StringBuilder();
        System.out.println(doGetFlowChartCode(nextConfigs, Collections.emptyMap(), Collections.emptyMap(), "test", "a", "g", Arrays.asList("a", "b", "c", "d", "e", "g"), styles, new AtomicInteger(0)));
        System.out.println(styles.toString());
    }

    public static String getChartSQL(String flow) {
        StringBuilder sb = new StringBuilder();
        sb.append("select concat(if(c.cond is null, concat(c.node, '-->', c.next_node), concat(c.node, '-->|\"', c.cond, '\"|', c.next_node)), if(cn.cond is null, '', concat('(', c.next_node, ')'))) ");
        sb.append("from flow_next_config c ");
        sb.append("left join flow_next_config cn on cn.flow = c.flow and cn.node = c.next_node ");
        sb.append("where c.flow = '").append(flow).append("'");
        sb.append("and c.is_deleted = 0 ");
        sb.append("group by c.flow, c.node, c.next_node ");
        sb.append("order by c.id");
        return sb.toString();
    }

    public static String getChartHtml(IFlowService flowService, String flow, String flowCode) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>Flow Chart: ").append(flow).append(flowCode != null ? "[" + flowCode + "]" : "").append("</title>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"mermaid\">\n");
        List<FlowDefaultConfig> defaultConfigs = flowService.queryFlowDefaultConfig();
        Optional<FlowDefaultConfig> find = defaultConfigs.stream().filter((cfg) -> cfg.getFlow().equalsIgnoreCase(flow)).findAny();
        if (!find.isPresent()) {
            throw new FlowException("no such flow: " + flow);
        }
        List<FlowNodeConfig> nodeConfigs = flowService.queryFlowNodeConfig();
        List<FlowNextConfig> nextConfigs = flowService.queryFlowNextConfig();
        Map<String, String> nodeNameMap = nodeConfigs.stream().collect(Collectors.toMap(FlowNodeConfig::getCode, FlowNodeConfig::getName));
        html.append(MermaidHelper.getFlowChartCode(defaultConfigs, nextConfigs, nodeNameMap, flow, flowCode, flowService));
        html.append("\n</div>");
        html.append("\n<script type=\"text/javascript\" src=\"https://cdn.bootcdn.net/ajax/libs/mermaid/8.8.2/mermaid.min.js\"></script>");
        html.append("\n<script>");
        html.append("mermaid.initialize({\n      startOnLoad: true, \n      theme: 'default', // default || forest || dark || neutral\n      logLevel:'fatal',\n      securityLevel:'strict',\n      arrowMarkerAbsolute: false,\n      fontFamily: 'arial',\n      flowchart:{\n        useMaxWidth: false,\n        htmlLabels: true,\n        curve: 'linear', // basis || linear || cardinal\n        nodeSpacing: 40\n      }\n    });");
        html.append("\n</script>");
        html.append("</body>");
        html.append("</html>");
        return html.toString();
    }

    public static String getFlowChartCode(List<FlowDefaultConfig> defaultConfigs, List<FlowNextConfig> nextConfigs, Map<String, String> nodeNameMap, String flow, String flowCode, IFlowService flowService) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD");
        StringBuilder styles = new StringBuilder();
        sb.append(doGetFlowChartCode(defaultConfigs, nextConfigs, getNodeIdMap(nextConfigs, flow), nodeNameMap, flow, flowCode, flowService, styles, new AtomicInteger(0)));
        sb.append(styles.toString());
        return sb.toString();
    }

    private static String doGetFlowChartCode(List<FlowDefaultConfig> defaultConfigs, List<FlowNextConfig> nextConfigs, Map<String, String> nodeIdMap, Map<String, String> nodeNameMap, String flow, String flowCode, IFlowService flowService, StringBuilder styles, AtomicInteger lineCounter) {
        String currentNode = null;
        List<String> pastNodes = Collections.emptyList();
        if (flowCode != null) {
            FlowRun run = flowService.queryFlowRun(flow, flowCode);
            if (run != null) {
                currentNode = run.getNode();
            }
            List<FlowRunTrack> tracks = flowService.queryFlowRunTrack(flow, flowCode);
            pastNodes = tracks.stream().map(FlowRunTrack::getNode).collect(Collectors.toList());
        }

        String startNode = getStartNode(defaultConfigs, flow);
        Map<String, String> subFlows = new HashMap<>();

        for (FlowNextConfig cfg : nextConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow())) {
                if ("BRANCH".equalsIgnoreCase(cfg.getNextNode())) {
                    // subFlows.put("BRANCH", cfg.getNodeData());
                }
                if ("SUB".equalsIgnoreCase(cfg.getNextNode())) {
                    // subFlows.put("SUB", cfg.getNodeData());
                }
            }
        }

        StringBuilder buff = new StringBuilder();
        buff.append(doGetFlowChartCode(nextConfigs, nodeIdMap, nodeNameMap, flow, startNode, currentNode, pastNodes, styles, lineCounter));

        for (Entry<String, String> entry : subFlows.entrySet()) {
            Map<String, String> subNodeIdMap = getNodeIdMap(nextConfigs, entry.getValue());
            String branch = entry.getKey();
            String subFlow = entry.getValue();
            String subStartNode = getStartNode(defaultConfigs, subFlow);
            buff.append("\n").append(nodeIdMap.getOrDefault(flow + branch, flow + branch)).append("-->").append(subNodeIdMap.getOrDefault(subFlow + subStartNode, subFlow + subStartNode));
            lineCounter.getAndIncrement();
            buff.append("\nsubgraph ").append(entry.getValue());
            buff.append(doGetFlowChartCode(defaultConfigs, nextConfigs, subNodeIdMap, nodeNameMap, entry.getValue(), flowCode, flowService, styles, lineCounter));
            buff.append("\nend");
        }

        return buff.toString();
    }

    private static String doGetFlowChartCode(List<FlowNextConfig> nextConfigs, Map<String, String> nodeIdMap, Map<String, String> nodeNameMap, String flow, String startNode, String currentNode, List<String> pastNodes, StringBuilder styles, AtomicInteger lineCounter) {
        StringBuilder buff = new StringBuilder();
        Set<String> handledTextNodes = new HashSet<>();
        Set<String> branchNodes = new HashSet<>();
        Set<String> endNodes = new HashSet<>();
        Set<Integer> pastLines = new HashSet<>();
        for (FlowNextConfig cfg : nextConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow())) {
                if (cfg.getCond() != null && !"".equals(cfg.getCond())) {
                    branchNodes.add(cfg.getNode());
                }
                endNodes.add(cfg.getNextNode());
            }
        }

        FlowNextConfig cfg;
        Iterator<FlowNextConfig> iterator = nextConfigs.iterator();

        while (true) {
            do {
                if (!iterator.hasNext()) {
                    styles.append(getStyles(nextConfigs, flow, startNode, currentNode, pastNodes, branchNodes, endNodes, pastLines, nodeIdMap));
                    return buff.toString();
                }

                cfg = iterator.next();
            } while (!flow.equalsIgnoreCase(cfg.getFlow()));

            boolean isCondition = cfg.getCond() != null && !"".equals(cfg.getCond());
            buff.append("\n").append(nodeIdMap.getOrDefault(cfg.getFlow() + cfg.getNode(), cfg.getFlow() + cfg.getNode()));
            if (!handledTextNodes.contains(cfg.getFlow() + cfg.getNode())) {
                buff.append(getNodeText(nodeNameMap, cfg.getNode(), branchNodes, startNode, currentNode, pastNodes));
                handledTextNodes.add(cfg.getFlow() + cfg.getNode());
            }

            int nodeIdx = pastNodes.indexOf(cfg.getNode());
            int nextNodeIdx = pastNodes.indexOf(cfg.getNextNode());
            boolean pastLine = nodeIdx != -1 && nextNodeIdx != -1 && nextNodeIdx - nodeIdx == 1;
            if (pastLine) {
                pastLines.add(lineCounter.get());
            }

            buff.append(pastLine ? "==>" : "-->");
            if (isCondition) {
                buff.append("|\"").append(cfg.getCond()).append("\"| ");
            }

            buff.append(nodeIdMap.getOrDefault(cfg.getFlow() + cfg.getNextNode(), cfg.getFlow() + cfg.getNextNode()));
            if (!handledTextNodes.contains(cfg.getFlow() + cfg.getNextNode())) {
                buff.append(getNodeText(nodeNameMap, cfg.getNextNode(), branchNodes, startNode, currentNode, pastNodes));
                handledTextNodes.add(cfg.getFlow() + cfg.getNextNode());
            }

            endNodes.remove(cfg.getNode());
            lineCounter.getAndIncrement();
        }
    }

    private static Map<String, String> getNodeIdMap(List<FlowNextConfig> nextConfigs, String flow) {
        Map<String, String> nodeIdMap = new HashMap<>();

        for (FlowNextConfig cfg : nextConfigs) {
            if (cfg.getFlow().equalsIgnoreCase(flow)) {
                nodeIdMap.put(cfg.getFlow() + cfg.getNode(), cfg.getId().toString());
            }
        }

        return nodeIdMap;
    }

    private static String getStartNode(List<FlowDefaultConfig> defaultConfigs, String flow) {
        String startNode = null;

        for (FlowDefaultConfig cfg : defaultConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow())) {
                startNode = cfg.getStartNode();
                break;
            }
        }

        return startNode;
    }

    private static String getStyles(List<FlowNextConfig> nextConfigs, String flow, String startNode, String currentNode, List<String> pastNodes, Set<String> branchNodes, Set<String> endNodes, Set<Integer> pastLines, Map<String, String> nodeIdMap) {
        StringBuilder buff = new StringBuilder();
        Set<String> styleNodes = new HashSet<>();

        for (FlowNextConfig cfg : nextConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow())) {
                if (!styleNodes.contains(cfg.getNode())) {
                    buff.append(getNodeStyle(cfg.getFlow(), cfg.getNode(), branchNodes, startNode, currentNode, pastNodes, endNodes, nodeIdMap));
                    styleNodes.add(cfg.getNode());
                }

                if (!styleNodes.contains(cfg.getNextNode())) {
                    buff.append(getNodeStyle(cfg.getFlow(), cfg.getNextNode(), branchNodes, startNode, currentNode, pastNodes, endNodes, nodeIdMap));
                    styleNodes.add(cfg.getNextNode());
                }
            }
        }

        for (Integer lineIndex : pastLines) {
            buff.append("\nlinkStyle ").append(lineIndex).append(" stroke:#3c3,stroke-width:3px");
        }

        return buff.toString();
    }

    private static String getNodeText(Map<String, String> nodeNameMap, String node, Set<String> branchNodes, String startNode, String currentNode, List<String> pastNodes) {
        String nodeName = getNodeName(nodeNameMap, node);
        StringBuilder buff = new StringBuilder();
        if (node.equalsIgnoreCase(startNode)) {
            buff.append("((").append(node).append(nodeName).append("))");
        } else if (branchNodes.contains(node)) {
            buff.append("{{").append(node).append(nodeName).append("}}");
        } else if (node.equalsIgnoreCase("END")) {
            buff.append("((").append(node).append(nodeName).append("))");
        } else if (!"BRANCH".equalsIgnoreCase(node) && !"SUB".equalsIgnoreCase(node)) {
            buff.append("[").append(node).append(nodeName).append("]");
        } else {
            buff.append(">").append(node).append(nodeName).append("]");
        }

        return buff.toString();
    }

    private static String getNodeStyle(String flow, String node, Set<String> branchNodes, String startNode, String currentNode, List<String> pastNodes, Set<String> endNodes, Map<String, String> nodeIdMap) {
        StringBuilder buff = new StringBuilder();
        String styleNode = nodeIdMap.getOrDefault(flow + node, flow + node);
        if (node.equalsIgnoreCase(startNode)) {
            buff.append("\nstyle ").append(styleNode).append(" fill:").append(pastNodes.contains(node) ? "#3c3" : "#fff").append(",stroke:#333,stroke-width:4px,color:#333");
        } else if (endNodes.contains(node)) {
            buff.append("\nstyle ").append(styleNode).append(" fill:").append(pastNodes.contains(node) ? "#3c3" : "#999").append(",stroke:#bbb,stroke-width:4px,color:#fff");
        } else if (node.equalsIgnoreCase(currentNode)) {
            buff.append("\nstyle ").append(styleNode).append(" fill:#ff0,stroke:#00f,stroke-width:2px,color:#000,stroke-dasharray: 5, 5");
        } else if (pastNodes.contains(node)) {
            buff.append("\nstyle ").append(styleNode).append(" fill:#3c3,stroke:#666");
        } else if ("BRANCH".equalsIgnoreCase(node) || "SUB".equalsIgnoreCase(node)) {
            buff.append("\nstyle ").append(styleNode).append(" stroke:#000,stroke-width:8px");
        }

        return buff.toString();
    }

    private static String getNodeName(Map<String, String> nodeNameMap, String node) {
        return nodeNameMap != null && nodeNameMap.containsKey(node) ? "<br/>" + nodeNameMap.get(node) : "";
    }
}
