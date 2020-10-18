package cn.veasion.flow.model;

import cn.veasion.flow.IFlowNode;

import java.io.Serializable;
import java.util.List;

/**
 * FlowNextNode
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class FlowNextNode implements Serializable {

    private FlowNextNode prev;
    private FlowNodeConfig node;
    private IFlowNode flowNode;
    private FlowNextConfig flowNextConfig;
    private List<FlowNextNode> nextNodes;

    public FlowNextNode getPrev() {
        return prev;
    }

    public void setPrev(FlowNextNode prev) {
        this.prev = prev;
    }

    public FlowNodeConfig getNode() {
        return node;
    }

    public void setNode(FlowNodeConfig node) {
        this.node = node;
    }

    public IFlowNode getFlowNode() {
        return flowNode;
    }

    public void setFlowNode(IFlowNode flowNode) {
        this.flowNode = flowNode;
    }

    public FlowNextConfig getFlowNextConfig() {
        return flowNextConfig;
    }

    public void setFlowNextConfig(FlowNextConfig flowNextConfig) {
        this.flowNextConfig = flowNextConfig;
    }

    public List<FlowNextNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<FlowNextNode> nextNodes) {
        this.nextNodes = nextNodes;
    }
}
