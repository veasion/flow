package cn.veasion.flow.chart;

import cn.veasion.flow.model.FlowNextConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FlowChartHelper
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class FlowChartHelper {

    public static void main(String[] args) {
        List<FlowNextConfig> nextConfigs = new ArrayList<>();
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
        System.out.println(getFlowChartCode(nextConfigs, null, "test", "a", "f", Arrays.asList("a", "b", "c", "d", "e")));
    }

    public static String getFlowChartCode(List<FlowNextConfig> nextConfigs, Map<String, String> nodeNameMap, String flow, String startNode, String currentNode, List<String> pastNodes) {
        Map<String, String> flatFlowMap = new HashMap<>();
        Map<String, List<String>> condFlowMap = new HashMap<>();

        for (FlowNextConfig cfg : nextConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow())) {
                boolean isCondition = cfg.getCond() != null && !"".equals(cfg.getCond());
                if (isCondition) {
                    List<String> conds = condFlowMap.get(cfg.getNode());
                    if (conds == null) {
                        conds = new LinkedList<>();
                        conds.add(cfg.getCond());
                        condFlowMap.put(cfg.getNode(), conds);
                    }

                    conds.add(cfg.getNextNode());
                } else {
                    flatFlowMap.put(cfg.getNode(), cfg.getNextNode());
                }
            }
        }

        StringBuilder flowCodeBuff = new StringBuilder();
        Set<String> unpeatFlowCode = new HashSet<>();
        String name = getNodeName(nodeNameMap, startNode);
        String cond = getCond(condFlowMap, startNode);
        String position = getNodePosition(currentNode, pastNodes, startNode);
        flowCodeBuff.append(startNode).append("=>start: ").append(startNode);
        flowCodeBuff.append(name).append(cond).append(position).append("\n");

        for (FlowNextConfig cfg : nextConfigs) {
            if (flow.equalsIgnoreCase(cfg.getFlow()) && !unpeatFlowCode.contains(cfg.getNextNode())) {
                name = getNodeName(nodeNameMap, cfg.getNextNode());
                cond = getCond(condFlowMap, cfg.getNextNode());
                position = getNodePosition(currentNode, pastNodes, cfg.getNextNode());
                flowCodeBuff.append(cfg.getNextNode()).append("=>").append(getNodeType(cfg.getNextNode(), flatFlowMap, condFlowMap));
                flowCodeBuff.append(": ").append(cfg.getNextNode()).append(name).append(cond).append(position).append("\n");
                unpeatFlowCode.add(cfg.getNextNode());
            }
        }

        StringBuilder nodeCodeBuff = new StringBuilder();
        Set<String> unpeatNodeCode = new HashSet<>();
        nodeCodeBuff.append(startNode);
        appendBranchCode(startNode, nodeCodeBuff, unpeatNodeCode, flatFlowMap, condFlowMap);
        return flowCodeBuff.toString() + "\n" + formatCode(nodeCodeBuff.toString());
    }

    private static String getNodeName(Map<String, String> nodeNameMap, String node) {
        return nodeNameMap != null && nodeNameMap.containsKey(node) ? "\n" + nodeNameMap.get(node) : "";
    }

    private static String getCond(Map<String, List<String>> condFlowMap, String node) {
        return condFlowMap.containsKey(node) ? "\n【" + condFlowMap.get(node).get(0) + "】" : "";
    }

    private static String getNodePosition(String currentNode, List<String> pastNodes, String node) {
        if (node.equals(currentNode)) {
            return "|current";
        } else {
            return pastNodes.contains(node) ? "|past" : "";
        }
    }

    private static void appendBranchCode(String lastNode, StringBuilder nodeCodeBuff, Set<String> unpeatNodeCode, Map<String, String> flatFlowMap, Map<String, List<String>> condFlowMap) {
        String nextNode;
        do {
            nextNode = flatFlowMap.get(lastNode);
            if (nextNode == null) {
                nodeCodeBuff.append("\n");
                if (condFlowMap.containsKey(lastNode) && !unpeatNodeCode.contains(lastNode)) {
                    onCond(lastNode, nodeCodeBuff, unpeatNodeCode, flatFlowMap, condFlowMap);
                    unpeatNodeCode.add(lastNode);
                }
            } else {
                nodeCodeBuff.append("->");
                nodeCodeBuff.append(nextNode);
                lastNode = nextNode;
            }
        } while(nextNode != null);

    }

    private static void onCond(String condNode, StringBuilder nodeCodeBuff, Set<String> unpeatNodeCode, Map<String, String> flatFlowMap, Map<String, List<String>> condFlowMap) {
        List<String> conds = condFlowMap.get(condNode);
        boolean yes = true;
        for(int i = 1; i < conds.size(); ++i) {
            String nextNode = conds.get(i);
            String cond = yes ? "yes, bottom" : "no, right";
            yes = false;
            nodeCodeBuff.append(condNode).append("(").append(cond).append(")");
            nodeCodeBuff.append("->").append(nextNode);
            appendBranchCode(nextNode, nodeCodeBuff, unpeatNodeCode, flatFlowMap, condFlowMap);
        }
    }

    private static String getNodeType(String node, Map<String, String> flatFlowMap, Map<String, List<String>> condFlowMap) {
        if (condFlowMap.containsKey(node)) {
            return "condition";
        } else if (!"BRANCH".equalsIgnoreCase(node) && !"SUB".equalsIgnoreCase(node)) {
            return flatFlowMap.containsKey(node) ? "operation" : "end";
        } else {
            return "subroutine";
        }
    }

    private static String formatCode(String code) {
        Map<String, int[]> coordinateMap = new HashMap<>();
        int lineno = 0;
        String[] lines = code.split("\n");
        for (String line : lines) {
            int idx = 0;
            String lastTag = null;
            String[] tags = line.split("\\->");
            for (String tag : tags) {
                if (lineno == 0 && lastTag == null) {
                    coordinateMap.put(tag, new int[]{0, 0});
                }
                if (lastTag != null) {
                    if (coordinateMap.containsKey(tag)) {
                        continue;
                    }
                    String baseTag = stripTag(lastTag);
                    int[] coordinate = coordinateMap.get(baseTag);
                    if (isNoStep(lastTag)) {
                        coordinateMap.put(tag, new int[]{coordinate[0] + 1, coordinate[1]});
                    } else {
                        coordinateMap.put(tag, new int[]{coordinate[0], coordinate[1] + idx});
                    }
                }
                idx++;
                lastTag = tag;
            }
            lineno++;
        }

        StringBuilder buff = new StringBuilder();
        Map<String, List<FlowChartHelper.Direction>> directionMap = new HashMap<>();
        String[] codeLines = code.split("\n");

        for (String line : codeLines) {
            int idx = 0;
            String lastTag = null;
            String[] tags = line.split("\\->");
            for (String tag : tags) {
                if (idx > 0) {
                    String baseLastTag = stripTag(lastTag);
                    String align;
                    List<Direction> directions;
                    if (!isConditionTag(lastTag)) {
                        directions = directionMap.get(baseLastTag);
                        align = getAlign(directions, tag);
                        if (align == null) {
                            int[] lastCoordinate = coordinateMap.get(stripTag(lastTag));
                            int[] coordinate = coordinateMap.get(stripTag(tag));
                            List<String> aligns = getAligns(directions);
                            int lastX = lastCoordinate[0];
                            int lastY = lastCoordinate[1];
                            int x = coordinate[0];
                            int y = coordinate[1];
                            if (lastX == x) {
                                if (lastY < y) {
                                    align = getAlignOnTop(aligns);
                                } else {
                                    align = getAlignOnBottom(aligns);
                                }
                            } else if (lastX < x) {
                                if (lastY < y) {
                                    align = getAlignOnTopLeft(aligns);
                                } else if (lastY == y) {
                                    align = getAlignOnLeft(aligns);
                                } else {
                                    align = getAlignOnBottomLeft(aligns);
                                }
                            } else if (lastY < y) {
                                align = getAlignOnTopRight(aligns);
                            } else if (lastY == y) {
                                align = getAlignOnRight(aligns);
                            } else {
                                align = getAlignOnBottomRight(aligns);
                            }

                            if (directions == null) {
                                directions = new LinkedList<>();
                                directionMap.put(baseLastTag, directions);
                            }

                            directions.add(new Direction(tag, align));
                        }

                        if (align != null) {
                            buff.append("(").append(align).append(")");
                        }
                    } else {
                        directions = directionMap.computeIfAbsent(tag, k -> new LinkedList<>());
                        directions.add(new Direction("", isYesStep(lastTag) ? "top" : "left"));
                    }
                }

                if (idx++ > 0) {
                    buff.append("->");
                }

                buff.append(tag);
                lastTag = tag;
            }

            buff.append("\n");
        }

        return buff.toString();
    }

    private static String stripTag(String tag) {
        int idx = tag.indexOf("(");
        return idx != -1 ? tag.substring(0, idx) : tag;
    }

    private static boolean isConditionTag(String tag) {
        return isYesStep(tag) || isNoStep(tag);
    }

    private static boolean isYesStep(String tag) {
        return tag.contains("(yes");
    }

    private static boolean isNoStep(String tag) {
        return tag.contains("(no");
    }

    private static String getAlign(List<FlowChartHelper.Direction> directions, String tag) {
        if (directions == null) {
            return null;
        } else {
            for (Direction direction : directions) {
                if (direction.to.equals(tag)) {
                    return direction.align;
                }
            }
            return null;
        }
    }

    private static List<String> getAligns(List<FlowChartHelper.Direction> directions) {
        return directions == null ? Collections.emptyList() : directions.stream().map((d) -> d.align).collect(Collectors.toList());
    }

    private static String getAlignOnTop(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("bottom", "left", "right"));
    }

    private static String getAlignOnBottom(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("top", "left", "right"));
    }

    private static String getAlignOnLeft(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("right", "bottom", "top"));
    }

    private static String getAlignOnRight(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("left", "bottom", "top"));
    }

    private static String getAlignOnTopLeft(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("right", "bottom", "left"));
    }

    private static String getAlignOnBottomLeft(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("right", "top", "left"));
    }

    private static String getAlignOnTopRight(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("left", "bottom", "right"));
    }

    private static String getAlignOnBottomRight(List<String> presentAligns) {
        return getAlignByProirity(presentAligns, Arrays.asList("left", "top", "right"));
    }

    private static String getAlignByProirity(List<String> presentAligns, List<String> aligns) {
        Iterator<String> alignIt = aligns.iterator();
        String align;
        do {
            if (!alignIt.hasNext()) {
                return aligns.get(0);
            }
            align = alignIt.next();
        } while(presentAligns.contains(align));
        return align;
    }

    private static class Direction {
        private String to;
        private String align;

        Direction(String to, String align) {
            this.to = to;
            this.align = align;
        }

        public String toString() {
            return this.to + "(" + this.align + ")";
        }
    }
}
