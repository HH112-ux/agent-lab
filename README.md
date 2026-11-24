# my-agent · AI 智能体项目一期：通用智能体搭建

Caspian 的学习复刻仓库。源码从 `../代码/` 全量复刻，命名统一为个人规范，密钥已脱敏。

- 📖 **完整讲义**：[`docs/AI智能体完整讲义.md`](docs/AI智能体完整讲义.md) —— 三轨素材（LangChain4j 基础 / Spring AI 工具链 / Agent 架构）合并为 20 章。
- 🐛 **已知问题清单**：见讲义第 19 章（含 1 条影响运行的 ReAct 语义缺陷）。

## 命名规范

| 项 | 规则 |
|---|---|
| `groupId` | 统一 `com.caspian`（原 `org.example` / `com.szb` / `com.example`） |
| Java 包根 | 统一 `com.caspian.agent.<模块叶名>`，模块内子包结构原样保留 |
| 模块目录名 / `artifactId` | **保留原名**，与 `../代码/` 一一对应 |

完整映射表见讲义 §16.2。

## 模块清单

| 目录 | 包根 | 讲什么 | 端口 |
|---|---|---|---|
| `langchain4j-parent/` | `com.caspian.agent.*` | LangChain4j 7 讲（quick-start / dashscope / memory / ai-service / tools / case / rag） | 8080–8084 |
| `chap1-langchain4j` | `com.caspian.agent.langchain4j` | 提示词与 embedding 起步 | — |
| `chap1-spring-ai` | `com.caspian.agent.springai` | Spring AI 最小闭环（代码注释生成） | 8080 |
| `chap2-1-function-calling` | `com.caspian.agent.functioncalling` | **零依赖手写函数调用协议** | — |
| `chap2-2-spring-ai-java-dev-tools` | `com.caspian.agent.devtools` | Spring AI `ToolCallback` 本地工具箱 | 8081 |
| `chap-2-3-mcp-server` | `com.caspian.agent.mcpserver` | 原生 MCP SDK，STDIO 服务端 | STDIO |
| `chap-2-3-mcp-client` | `com.caspian.agent.mcpclient` | Spring AI MCP 客户端 | 8081 |
| `chap-3-1-single-agent` | `com.caspian.agent.singleagent` | 单 Agent：ReAct + PlanAct + 三级记忆 | CLI |
| `chap-4-multi-agent` | `com.caspian.agent.multiagent` | 多 Agent：角色 / A2A / 编排 / 冲突 | 8080 |
| `chap-5-graph-workflow` | `com.caspian.agent.graphworkflow` | Graph 工作流：State / Node / Edge | 8080 |
| `langchain4j-courseware/` | — | day01–day08 原始讲稿（已脱敏） | — |

## 快速开始

```bash
# 1) 注入密钥（原始值请到 ../代码/ 的配置文件里取，不要写回仓库）
export DASHSCOPE_API_KEY=你的百炼或方舟key
export VOLCANO_API_KEY=你的方舟key           # chap-3-1 用
export JINA_API_KEY=你的jina密钥             # 可选，chat-rag 重排序
export CHROMA_BASE_URL=http://localhost:8000 # 可选，chat-rag 向量库
export MCP_SERVER_JAR=$(pwd)/chap-2-3-mcp-server/target   # MCP 联调用

# 2) 编译单个模块
cd chap-3-1-single-agent && mvn -q compile exec:java

# 3) MCP 需要先打包 Server 再启 Client
cd chap-2-3-mcp-server && mvn -q clean package -DskipTests
cd ../chap-2-3-mcp-client && mvn -q spring-boot:run
```

## 环境提示

- 编译目标 Java 17。**建议用 JDK 17 或 21**：JDK 25/27 下 `langchain4j-parent` 的 Lombok 注解处理器不工作，会报 `找不到符号: 变量 log`（原始代码同样如此，非复刻引入）。
- 多个模块共用 8080/8081，**同一时刻只能起一个**。

## 复刻时的改动（除包名外）

- 全部明文 API key → 环境变量占位符。
- `chap-2-3-mcp-client` 里教师机 jar 绝对路径 → `${MCP_SERVER_JAR}`。
- `chap-rag` 里硬编码的外部 Chroma 地址与 Jina key → 环境变量。
- 修正 4 处拼写不规范标识符（`chatMedel` / `converEncoding` / `queryprocessTool` / 无用 import）。
- 两个 `static/index.html` 从模块根移到 `src/main/resources/static/`（原先 Spring 不会托管）。
- **修复了 2 处影响 Agent 正确性的缺陷**（其余已知问题原样保留在讲义第 19 章）：
  - §19.3 ReAct 循环中工具执行结果未回喂给模型 —— 已补 `conversation.add(observation)`，伪模型断言验证通过。
  - §19.4 长期记忆只沉淀 `summary`、丢失结构化风险 —— 已新增 `风险|等级|描述|修正建议` 行式协议 + `parseRiskItems()`，落库补齐 `risks[]` 与三级计数。
