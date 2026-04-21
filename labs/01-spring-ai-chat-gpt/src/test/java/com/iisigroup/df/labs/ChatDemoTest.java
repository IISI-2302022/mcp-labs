package com.iisigroup.df.labs;

import com.iisigroup.df.labs.base.MySpringBootTest;
import com.iisigroup.df.labs.tool.DateTimeTool;
import com.iisigroup.df.labs.config.LoggingConfig;
import com.iisigroup.df.labs.config.MemoryConfig;
import com.iisigroup.df.labs.model.ActorFilms;
import com.iisigroup.df.labs.tool.UUIDTool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Import({MemoryConfig.class, LoggingConfig.class})
@MySpringBootTest
public class ChatDemoTest {
    @DynamicPropertySource
    public static void setup(DynamicPropertyRegistry registry) {
        registry.add("spring_ai_openai_api_key", () -> "輸入自己的 API KEY");
    }

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private SimpleLoggerAdvisor simpleLoggerAdvisor;

    @Autowired
    private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    private ChatClient client;

    @PostConstruct
    public void init() {
        this.client = builder.build();
    }

    @Test
    public void syncChatFluent() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .user(userPrompt)
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void asyncChatFluent() throws InterruptedException {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .user(userPrompt)
                .stream()
                .content();
        content.subscribe((contentSplit) -> log.info("response content: {}", contentSplit));

        Thread.sleep(10000);
    }

    @Test
    public void syncChatNonFluent() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);
        val content = client
                .prompt(new Prompt(userPrompt))
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void syncChatReturnEntity() {
        val userPrompt = "Generate the filmography for a random actor.";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt()
                .user(userPrompt)
                .call()
                .entity(ActorFilms.class);
        log.info("response content: {}", entity);
    }

    @Test
    public void syncChatReturnEntityWithParameterizedType() {
        val userPrompt = "Generate the filmography of 5 movies for Tom Hanks and Bill Murray.";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt()
                .user(userPrompt)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
        log.info("response content: {}", entity);
    }

    @Test
    public void syncChatWithPromptTemplate() {
        val userPrompt = "Tell me the names of 5 movies whose soundtrack was composed by {composer}";

        log.info("request userPrompt: {}", userPrompt);

        val content = this.client.prompt()
                .user(u -> u
                        .text(userPrompt)
                        .param("composer", "John Williams"))
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithPromptTemplateAndReplacement() {
        val userPrompt = "Tell me the names of 5 movies whose soundtrack was composed by <composer>";

        log.info("request userPrompt: {}", userPrompt);

        val content = this.client.prompt()
                .user(u -> u
                        .text(userPrompt)
                        .param("composer", "John Williams"))
                .templateRenderer(
                        StTemplateRenderer.builder()
                                .startDelimiterToken('<')
                                .endDelimiterToken('>')
                                .build()
                )
                .call()
                .content();

        log.info("response content: {}", content);
    }


    @Test
    public void asyncChatReturnEntityWithParameterizedType() {
        val converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {
        });

        val userPrompt = """
                  Generate the filmography for a random actor.
                  {format}
                """;

        log.info("request userPrompt: {}", userPrompt);

        val contentFlux = client
                .prompt()
                .user((promptUserSpec) ->
                        promptUserSpec
                                .text(userPrompt)
                                .param("format", converter.getFormat())
                )
                .stream()
                .content();

        val content = contentFlux.collectList().block().stream().collect(Collectors.joining());

        val actorFilms = converter.convert(content);

        log.info("response content: {}", actorFilms);

    }


    @Test
    public void syncChatForEntityWithErrorRetry() {
        val parameterizedTypeReference = new ParameterizedTypeReference<List<ActorFilms>>() {
        };
        val validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(parameterizedTypeReference)
                .maxRepeatAttempts(3)
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 1000)
                .build();

        val userPrompt = "Generate the filmography of 5 movies for Tom Hanks and Bill Murray.";
        log.info("request userPrompt: {}", userPrompt);
        val entity = client
                .prompt()
                .advisors(validationAdvisor)
                .user(userPrompt)
                .call()
                .entity(parameterizedTypeReference);

        log.info("response content: {}", entity);
    }

    @Test
    public void syncChatWithMetadata() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);
        val content = client.prompt()
                .user(u ->
                        u.text(userPrompt)
                                // 可以給 advisor 存取
                                .metadata("key1", "value1")
                )
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithLogging() {
        val userPrompt = "你好";
        log.info("request userPrompt: {}", userPrompt);

        val content = client.prompt()
                .user(userPrompt)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithMemory() {

        val conversationId = "conversation-id-123";

        // todo 自己改
        val name = "ming";

        val userPrompt1 = "我叫做 {name} , 你好";

        log.info("request userPrompt1: {}", userPrompt1);

        val content = client.prompt()
                .user(u ->
                        u
                                .text(userPrompt1)
                                .param("name", name)
                )
                .advisors((advisorSpec) ->
                        advisorSpec
                                .advisors(messageChatMemoryAdvisor)
                                .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        log.info("response content1: {}", content);


        val userPrompt2 = "你知道我叫做啥嗎 ?";
        log.info("request userPrompt2: {}", userPrompt2);

        val content1 = client.prompt()
                .user(userPrompt2)
                .advisors((advisorSpec) ->
                        advisorSpec
                                .advisors(messageChatMemoryAdvisor)
                                .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        log.info("response content1: {}", content1);

    }

    @Test
    public void syncChatWithFunctionCall() {
        val userPrompt = "What day is tomorrow?";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new DateTimeTool())
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithFunctionCall1() {
        val userPrompt = "Can you set an alarm 10 minutes from now?";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new DateTimeTool())
                .call()
                .content();

        log.info("response content: {}", content);
    }

    @Test
    public void syncChatWithFunctionCallAndToolContext() {
        val userPrompt = "產生 uuid";

        log.info("request userPrompt: {}", userPrompt);

        val content = client
                .prompt(userPrompt)
                .tools(new UUIDTool())
                .toolContext(
                        Map.of("defaultLength", 5)
                )
                .call()
                .content();

        log.info("response content: {}", content);
    }


}
