package com.caspian.agent.mcpserver;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class McpServerApp {
    private static final Logger log = LoggerFactory.getLogger(McpServerApp.class);


    // ========工具定义方法========

    private static McpSchema.Tool checkPortTool() {
        return McpSchema.Tool.builder()
                .name("check_port")
                .description("检查指定端口是否被占用，并返回占用的进程信息")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("port", Map.of("type", "integer", "description", "端口号(1-65535)")),
                        java.util.List.of("port"), null, null, null
                        )).build();
    }

    private static McpSchema.Tool queryProcessTool() {
        return McpSchema.Tool.builder()
                .name("query_process")
                .description("查询指定名称的运行中的进程")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("name", Map.of("type", "string", "description", "进程名称关键词")),
                        java.util.List.of("name"), null, null, null)).build();
    }

    private static McpSchema.Tool runMavenTool() {
        return McpSchema.Tool.builder()
                .name("run_maven")
                .description("在指定目录执行maven命令")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("command", Map.of("type", "string", "description", "Maven命令(如clean install version等等)"),
                                "directory", Map.of("type", "string", "description", "项目的目录路径")),
                        java.util.List.of("command"), null, null, null))
                .build();
    }

    // ========工具执行方法==========

    private static McpSchema.CallToolResult handlePort(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        int port = ((Number) args.get("port")).intValue();

        return McpSchema.CallToolResult.builder()
                .addTextContent(exec("lsof", "-i", ":" + port))
                .build();
    }

    private static McpSchema.CallToolResult handleProcess(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String name = (String) args.get("name");
        return McpSchema.CallToolResult.builder()
                .addTextContent(exec("pgrep", "-fl", name))
                .build();
    }

    private static McpSchema.CallToolResult handleMaven(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String command = (String) args.get("command");
        String dir = (String) args.getOrDefault("directory", ".");
        String output = exec(new File(dir), "mvn", command);
        if (output.length() > 5000) {
            output = output.substring(0, 5000) + "\n... (输出已截断)";
        }
        return McpSchema.CallToolResult.builder()
                .addTextContent(output)
                .build();
    }



    // 重载的执行方法，不传目录
    private static String exec(String... cmd) {
        return exec(null, cmd);
    }


    // 执行命令的方法
    private static String exec(File dir, String... cmd) {
        try {
            var pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            if (dir != null) {
                pb.directory(dir);
            }
            var process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            return output.isBlank() ? "命令执行完成(退出码：" + exitCode + ")" : output;
        } catch (Exception e) {
            return "执行失败: " + e.getMessage();
        }

    }


    public static void main(String [] args) {
        var jsonMapper = new JacksonMcpJsonMapperSupplier().get();

        var transport = new StdioServerTransportProvider(jsonMapper);

        McpSyncServer server = McpServer.sync(transport)
                .serverInfo("dev-tools-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .toolCall(checkPortTool(), McpServerApp::handlePort)
                .toolCall(queryProcessTool(), McpServerApp::handleProcess)
                .toolCall(runMavenTool(), McpServerApp::handleMaven)
                .build();
        log.info("MCP Server started - listen on STOIO");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
