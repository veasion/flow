package cn.veasion.flow;

import cn.veasion.flow.core.IFlowService;
import cn.veasion.flow.model.BaseBean;
import cn.veasion.flow.model.FlowDefaultConfig;
import cn.veasion.flow.model.FlowNextConfig;
import cn.veasion.flow.model.FlowNodeConfig;
import cn.veasion.flow.model.FlowRun;
import cn.veasion.flow.model.FlowRunTrack;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TestFlowService
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class TestFlowService implements IFlowService {

    @Override
    public List<FlowDefaultConfig> queryFlowDefaultConfig() {
        List<FlowDefaultConfig> list = new ArrayList<>();
        FlowDefaultConfig config = new FlowDefaultConfig();
        config.setFlow("SO");
        config.setStartNode("START");
        config.setErrorNode("SO_ERROR");
        list.add(config);

        return batchSetId(list);
    }

    @Override
    public List<FlowNextConfig> queryFlowNextConfig() {
        List<FlowNextConfig> list = new ArrayList<>();
        FlowNextConfig config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("START");
        config.setNextFlow("SO");
        config.setNextNode("IS_PAY");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("IS_PAY");
        config.setNextFlow("SO");
        config.setNextNode("PAY_DONE");
        config.setCond("next == 'yes'");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("IS_PAY");
        config.setNextFlow("SO");
        config.setNextNode("WAIT_PAY");
        config.setCond("next == 'no'");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("PAY_DONE");
        config.setNextFlow("SO");
        config.setNextNode("SWITCH_1");
        config.setCond("so.switch == 1");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("PAY_DONE");
        config.setNextFlow("SO");
        config.setNextNode("SWITCH_2");
        config.setCond("so.switch == 2");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("PAY_DONE");
        config.setNextFlow("SO");
        config.setNextNode("SWITCH_3");
        config.setCond("so.switch == 3");
        list.add(config);
        config = new FlowNextConfig();
        config.setFlow("SO");
        config.setNode("PAY_DONE");
        config.setNextFlow("SO");
        config.setNextNode("SWITCH_DEFAULT");
        list.add(config);

        return batchSetId(list);
    }

    @Override
    public List<FlowNodeConfig> queryFlowNodeConfig() {
        List<FlowNodeConfig> list = new ArrayList<>();
        FlowNodeConfig config = new FlowNodeConfig();
        config.setCode("START");
        config.setName("开始");
        config.setIsVirtual(1);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("IS_PAY");
        config.setName("是否支付");
        config.setIsVirtual(0);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("WAIT_PAY");
        config.setName("等待支付");
        config.setIsVirtual(0);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("PAY_DONE");
        config.setName("支付完成");
        config.setIsVirtual(0);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("SWITCH_1");
        config.setName("switch1");
        config.setIsVirtual(1);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("SWITCH_2");
        config.setName("switch2");
        config.setIsVirtual(1);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("SWITCH_3");
        config.setName("switch3");
        config.setIsVirtual(1);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("SWITCH_DEFAULT");
        config.setName("switch_default");
        config.setIsVirtual(1);
        list.add(config);
        config = new FlowNodeConfig();
        config.setCode("SO_ERROR");
        config.setName("错误节点");
        config.setIsVirtual(0);
        list.add(config);

        return batchSetId(list);
    }

    @Override
    public FlowRun queryFlowRun(String flow, String flowCode) {
        if ("last".equals(flowCode)) {
            FlowRun flowRun = new FlowRun();
            flowRun.setFlow("SO");
            flowRun.setNode("IS_PAY");
            flowRun.setFlowCode(flowCode);
            flowRun.setStatus(3);
            flowRun.setRunData("{\"flowCode\":\"" + flowCode + "\",\"data\":{\"next\":\"yes\",\"so\":{}}}");
            return flowRun;
        }
        return null;
    }

    @Override
    public List<FlowRunTrack> queryFlowRunTrack(String flow, String flowCode) {
        return null;
    }

    @Override
    public void saveFlowRun(FlowRun flowRun) {
        System.out.println("saveFlowRun:");
        System.out.println(JSONObject.toJSONString(flowRun));
    }

    @Override
    public void updateFlowRun(FlowRun flowRun) {
        System.out.println("updateFlowRun:");
        System.out.println(JSONObject.toJSONString(flowRun));
    }

    @Override
    public void saveFlowRunTrack(FlowRunTrack flowRunTrack) {
        System.out.println("saveFlowRunTrack:");
        System.out.println(JSONObject.toJSONString(flowRunTrack));
    }

    @Override
    public IFlowNode getFlowNode(String code) {
        IFlowNode flowNode = null;
        if ("IS_PAY".equals(code)) {
            flowNode = buildFlowNode(code, (context) -> {
                context.set("so", new HashMap<String, Object>());
                context.nextYes();
            });
        } else if ("PAY_DONE".equals(code)) {
            flowNode = buildFlowNode(code, (context) -> {
                Map<String, Object> map = context.getData("so");
                map.put("switch", 1);
                context.getTrackMap().put("switch", map.get("switch"));
            });
        } else if ("WAIT_PAY".equals(code)) {
            flowNode = buildFlowNode(code, (context) -> {
                System.out.println("等待支付...");
            });
        } else if ("SO_ERROR".equals(code)) {
            flowNode = buildFlowNode(code, (context) -> {
                System.err.println("运行错误节点");
            });
        }
        if (flowNode == null) {
            return null;
        }
        final IFlowNode node = flowNode;
        return new IFlowNode() {
            @Override
            public void onFlow(FlowContext context) throws Exception {
                System.out.println("==> " + getCode());
                node.onFlow(context);
            }

            @Override
            public String getCode() {
                return node.getCode();
            }
        };
    }

    private IFlowNode buildFlowNode(String code, Consumer<FlowContext> consumer) {
        return new IFlowNode() {
            @Override
            public void onFlow(FlowContext context) throws Exception {
                consumer.accept(context);
            }

            @Override
            public String getCode() {
                return code;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> batchSetId(List<? extends BaseBean> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setId((long) i);
        }
        return (List<T>) list;
    }
}
