package com.caspian.agent.graphworkflow.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class KnowledgeBaseTool {
    @Tool(description = "搜索技术知识库，查找与用户问题相关的FAQ和技术解决方案")
    public String searchKnowledge(@ToolParam(description = "搜索关键词") String keyword) {
        if (keyword.contains("500") || keyword.contains("报错") || keyword.contains("错误")) {
            return "FAQ#127: 服务器500错误排查步骤:\n" +
                    "1. 检查服务日志定位具体错误堆栈\n" +
                    "2. 确认数据库连接是否正常\n" +
                    "3. 验证API接口参数格式是否符合规范\n" +
                    "4. 检查服务器内存和CPU使用情况\n" +
                    "5. 如仍无法解决，请联系运维团队";
        }
        if (keyword.contains("登录") || keyword.contains("login") || keyword.contains("帐号")) {
            return "FAQ#089: 登录问题排查指南:\n" +
                    "1. 确认账号密码是否正确（注意大小写）\n" +
                    "2. 检查网络连接是否正常\n" +
                    "3. 清除浏览器缓存和Cookie后重试\n" +
                    "4. 尝试使用「忘记密码」功能重置密码\n" +
                    "5. 如账号被锁定，请联系客服解锁";
        }

        return "知识库检索结果: 暂未找到与'" + keyword + "'直接相关的文档。\n" +
                "建议: 1) 尝试更换关键词搜索 2) 联系技术支持团队获取帮助";
    }
}