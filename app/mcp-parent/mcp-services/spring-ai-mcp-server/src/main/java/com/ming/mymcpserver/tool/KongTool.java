package com.ming.mymcpserver.tool;

import com.ming.mymcpserver.api.KongAdminClient;
import com.ming.mymcpserver.label.AutoRegisterMcpTool;
import com.ming.mymcpserver.util.MapPathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class KongTool implements AutoRegisterMcpTool {

    private final KongAdminClient kongAdminClient;

    @Tool(name = "listKongRoutesWithPlugin", description = "列出 kong routes 與其對應之 plugins 資訊")
    public Map<String, List<String>> listKongRoutesWithPlugin(@ToolParam(required = false, description = "kong workspace") String workspace) {
        val pluginsApiResult = kongAdminClient.listPlugins(workspace);
        val routesApiResult = kongAdminClient.listRoutes(workspace);

        return Mono.zip(
                pluginsApiResult,
                routesApiResult,
                (pluginInfo, routeInfo) -> {
                    // 合併兩個 api 結果
                    val routeIdWithName = new HashMap<String, String>();
                    val routeWithPlugins = new HashMap<String, List<String>>();

                    val routes = (List<Map<String, Object>>) routeInfo.get("data");
                    for (Map<String, Object> route : routes) {
                        val routeId = (String) route.get("id");
                        val routeName = (String) route.get("name");
                        routeIdWithName.put(routeId, routeName);
                        // 單個 workspace 不會有相同 route name , 不用檢核
                        routeWithPlugins.put(routeName, new ArrayList<>());
                    }

                    val plugins = (List<Map<String, Object>>) pluginInfo.get("data");
                    for (Map<String, Object> plugin : plugins) {
                        val routeId = MapPathUtils.<String>getFromMapOrList(plugin, "plugin.route.id");
                        if (routeId != null) {
                            val routeName = routeIdWithName.get(routeId);
                            val pluginName = (String) plugin.get("name");
                            routeWithPlugins
                                    .computeIfAbsent(routeName, (unuse) -> new ArrayList<>())
                                    .add(pluginName);
                        }
                    }

                    return routeWithPlugins;
                }
                )
                // todo 原本想直接回傳 mono , 但好像不能, 所以改成同步
                .block();
    }

}
