package com.caspian.agent.chatdashscope;

import com.alibaba.dashscope.aigc.imagegeneration.ImageGeneration;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationMessage;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationParam;
import com.alibaba.dashscope.aigc.imagegeneration.ImageGenerationResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.Status;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxModelName;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.LambdaStreamingResponseHandler;
import dev.langchain4j.model.audio.AudioTranscriptionModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponse;

@SpringBootTest
public class DashScopeTest {

    @Autowired
    QwenChatModel qwenChatModel;

    @Autowired
    QwenStreamingChatModel qwenStreamingChatModel;

    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.chat-model.model-name}")
    private String modelName;

    @Test
    void test(){
        QwenChatModel chatModel = QwenChatModel.builder().apiKey(apiKey).modelName(modelName).build();
        String chat = chatModel.chat("你是谁");
        System.out.println(chat);
    }

    @Test
    void chatTest() {
        String content = qwenChatModel.chat("你是谁");
        System.out.println(content);
    }

    @Test
    void chatTest2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        QwenStreamingChatModel chatModel = QwenStreamingChatModel.builder().apiKey(apiKey).modelName("qwen-plus").build();
        chatModel.chat("你是谁", new StreamingChatResponseHandler() {
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("onCompleteResponse: " + completeResponse);
                latch.countDown();
            }

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.println("onPartialResponse: " + partialResponse);
            }

            @Override
            public void onError(Throwable error) {

            }
        });
        latch.await();
    }

    @Test
    void chatTest3() throws InterruptedException {
        QwenStreamingChatModel chatModel = QwenStreamingChatModel.builder().apiKey(apiKey).modelName("qwen-plus").build();
        LambdaStreamingResponseHandler.onPartialResponseBlocking(
                chatModel,
                "你是谁",
                System.out::print
        );
    }

    @Test
    void imageTest() {
        WanxImageModel model = WanxImageModel
                .builder()
                .apiKey(apiKey)
                .modelName(WanxModelName.WANX_V1)
                .build();
        Response<Image> response = model.generate("绝美镜头，美女明星，古代服饰，面露微笑，电影节光照，明媚动人治愈人心");
        String content = response.content().url().toString();
        System.out.println(content);
    }

    @Test
    void imageTestV2() throws NoApiKeyException {
        String prompt = "绝美镜头，美女明星，古代服饰，面露微笑，电影节光照，明媚动人治愈人心";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(apiKey)
                .model(ImageSynthesis.Models.WANX_V1)
                .prompt(prompt)
                .style("<watercolor>")
                .n(1)
                .size("1024*1024")
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult imageSynthesisResult = imageSynthesis.call(param);
        System.out.println(JsonUtils.toJson(imageSynthesisResult));
    }

    @Test
    void imageTestV3() throws NoApiKeyException, UploadFileException {
        String prompt = "美女明星，明媚动人治愈人心";

        ImageGenerationMessage message = ImageGenerationMessage
                .builder().role("user")
                .content(Collections.singletonList(Collections.singletonMap("text", prompt)))
                .build();
        ImageGenerationParam param = ImageGenerationParam.builder()
                .apiKey(apiKey)
                .model("wan2.7-image-pro")
                .message(message)
                //  .messages(Collections.singletonList(message))
                .enableSequential(false)
                .n(1)
                .size("2k")
                .build();

        ImageGeneration imageGeneration = new ImageGeneration();
        ImageGenerationResult taskResult = imageGeneration.asyncCall(param);
        String taskId = taskResult.getOutput().getTaskId();
        ImageGenerationResult result = waitTask(taskId);
        System.out.println(JsonUtils.toJson(result));
    }

    public ImageGenerationResult waitTask(String taskId)
            throws ApiException, NoApiKeyException {
        ImageGeneration imageGeneration = new ImageGeneration();
        return imageGeneration.wait(taskId, apiKey);
    }


    @Test
    void textToSpeech() {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model("sambert-zhichu-v1")
                .text("今天天气怎么样")
                .sampleRate(48000)
                .format(SpeechSynthesisAudioFormat.MP3)
                .build();

        File file = new File("output.mp3");
        ByteBuffer audio = synthesizer.call(param);
        System.out.println("requestId: " + synthesizer.getLastRequestId());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
            System.out.println("synthesis done!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void speechToText() {
        Recognition recognizer = new Recognition();
        String projectRoot = System.getProperty("user.dir");
        File file = new File(projectRoot, "output.mp3");
        RecognitionParam param =
                RecognitionParam.builder()
                        .apiKey(apiKey)
                        .model("paraformer-realtime-v2")
                        .format("mp3")
                        .sampleRate(48000)
                        // “language_hints”只支持paraformer-v2和paraformer-realtime-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .build();
        String call = recognizer.call(param, file);
        System.out.println(call);
    }

    @Test
    void textToVideo() throws NoApiKeyException, InputRequiredException {
        VideoSynthesis vs = new VideoSynthesis();
        VideoSynthesisParam param =
                VideoSynthesisParam.builder()
                        .apiKey(apiKey)
                        .model("wan2.7-t2v-2026-04-25")
                        .prompt("第1个镜头 大远景开篇，镜头从接近地面的低机位开始向前移动，镜头沿草原方向推进，同时向上移动，将视角从贴地逐渐抬升至略高位置，使猎豹从左侧进入画面并与前方逃窜的羚羊共同处于同一追逐路径中，建立清晰的前后空间关系。 第2个镜头 向下移动回到接近地面高度，并沿猎豹运动方向向右移动，与其保持侧向平行关系进行稳定跟随，主体始终处于画面中部偏左位置，背景产生连续横向位移效果，在这一阶段镜头运动保持稳定一段时间以强化速度感与空间连续性。 第3个镜头 在维持向右移动的同时，沿猎豹运动路径内侧做小幅弧线移动，使画面产生轻微绕行变化但始终保持与主体同向。 第4个镜头 逐渐减缓横向移动速度并过渡为机位相对稳定，同时执行变焦推进，将视觉焦点逐步压缩至猎豹与羚羊之间不断缩短的距离上。 第5个镜头 再次向前移动并轻微向下移动，贴近地面逼近两者之间的空间，在猎豹前肢逼近羚羊后方的临界位置停住，形成强烈压迫感与张力，同时配合逐渐增强的交响配乐、持续加密的鼓点、风声与踏地声推进节奏，并在最后阶段压低音乐仅保留环境声与节奏声形成短暂停顿。")
                        .duration(15)
                        .resolution("720P")
                        .ratio("16:9")
                        .promptExtend(true)
                        .watermark(true)
                        .build();
        VideoSynthesisResult result = vs.call(param);
        System.out.println(JsonUtils.toJson(result));
    }


    @Test
    void pictureToText() throws NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"),
                        Collections.singletonMap("text", "图中描绘的是什么景象?"))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model("qwen3.6-plus")
                .messages(Collections.singletonList(userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);
        System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
    }

}
