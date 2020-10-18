package cn.veasion.flow;

import cn.veasion.flow.core.IFlowService;

/**
 * FlowTest
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class FlowTest {

    public static void main(String[] args) {
        IFlowService flowService = new TestFlowService();
        FlowManager flowManager = new FlowManager(flowService, true);
        flowManager.startFlowSync(new FlowIn("SO", "code1"));
    }

}
