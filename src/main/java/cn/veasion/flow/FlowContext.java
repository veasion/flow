package cn.veasion.flow;

import com.alibaba.fastjson.JSONObject;

import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程上下文
 *
 * @author luozhuowei
 * @date 2020/10/18
 */
public class FlowContext {

    private String flowCode;
    private FlowContext parent;
    private ScriptContext scriptContext;
    private Map<String, Object> trackMap = new HashMap<>();
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public FlowContext(String flowCode) {
        this.flowCode = flowCode;
    }

    public void next() {
        this.set("next", "default");
    }

    public void nextYes() {
        this.set("next", "yes");
    }

    public void nextNo() {
        this.set("next", "no");
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object get(String key) {
        Object value = data.get(key);
        return value == null && parent != null ? parent.get(key) : value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) this.get(key);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setParent(FlowContext parent) {
        this.parent = parent;
    }

    public FlowContext getParent() {
        return parent;
    }

    public ScriptContext getScriptContext() {
        return scriptContext;
    }

    public void setScriptContext(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
    }

    public Map<String, Object> getTrackMap() {
        return trackMap;
    }

    public static String convertRunData(FlowContext context) {
        JSONObject json = new JSONObject();
        json.put("flowCode", context.flowCode);
        json.put("data", context.data);
        if (context.parent != null) {
            json.put("parent", context.parent);
        }
        return json.toJSONString();
    }

    public static FlowContext convertFlowContext(String runData) {
        JSONObject json = JSONObject.parseObject(runData);
        String flowCode = json.getString("flowCode");
        FlowContext context = new FlowContext(flowCode);
        JSONObject data = json.getJSONObject("data");
        for (String key : data.keySet()) {
            context.set(key, data.get(key));
        }
        if (json.containsKey("parent")) {
            context.parent = convertFlowContext(json.getString("parent"));
        }
        return context;
    }

}