package com.ming.mymcpserver.tool;

import com.ming.mymcpserver.api.KongAdminClient;
import com.ming.mymcpserver.util.MapPathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class KongTool {

    private final KongAdminClient kongAdminClient;

    @McpTool(name = "listKongRoutesWithPlugin", description = "列出 kong routes 與其對應之 plugins 資訊")
    public Mono<Map<String, List<String>>> listKongRoutesWithPlugin(@McpToolParam(required = false, description = "kong workspace") String workspace) {
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
        );
    }

}
