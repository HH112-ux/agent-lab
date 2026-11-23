package com.caspian.agent.springai;
// 三步：1.接收代码 2.组装Prompt调用大模型 3.返回注释后的代码

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CodeCommentController {

    private final ChatModel chatModel;

    public CodeCommentController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
    // POST /api/comment
    @PostMapping("/comment")
    public String generateComment(@RequestBody String code) {
        String promptText = """
                你是一个资深JAVA开发者。请为以下的代码添加请晰的中文注释(类注释，方法注释，关键行内注释)，
                要求只返回添加注释后的完整代码，不要其他解释：
                ```java
                %s 
                ```
                """.formatted(code);

        return chatModel.call(new Prompt(new UserMessage(promptText))).getResult().getOutput().getText();
    }
}
