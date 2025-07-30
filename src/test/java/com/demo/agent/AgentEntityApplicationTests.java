package com.demo.agent;

import com.demo.agent.service.ai.Assistant;
import com.demo.agent.service.ai.AssistantStream;
import com.demo.agent.tool.PersistentChatMemoryStore;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@SpringBootTest
class AgentEntityApplicationTests {

    class Tools {

        @Tool
        int add(int a, int b) {
            return a + b;
        }

        @Tool
        int multiply(int a, int b) {
            return a * b;
        }
    }

    @Autowired
    private PersistentChatMemoryStore persistentChatMemoryStore;

    /**
     * 测试每个用户记忆
     */
    @Test
    void testWithMemoryForEachUserExampl() {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";
        ChatModel model =   OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        System.out.println(assistant.chat(1, "Hello, my name is Klaus"));
        // Hi Klaus! How can I assist you today?

        System.out.println(assistant.chat(2, "Hello, my name is Francine"));
        // Hello Francine! How can I assist you today?

        System.out.println(assistant.chat(1, "What is my name?"));
        // Your name is Klaus.

        System.out.println(assistant.chat(2, "What is my name?"));
        // Your name is Francine.
    }

    /**
     * 测试工具调用
     */
    @Test
    void testWithToolUse() {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";
        ChatModel model =   OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new Tools())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String answer = assistant.chat(1,"What is 1+2 and 3*4?");
        System.out.println(answer);
    }

    /**
     * 测试流式输出
     */
    @Test
    void testWithStreamOutput() throws InterruptedException {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";
        StreamingChatModel model =   OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        AssistantStream assistant = AiServices.builder(AssistantStream.class)
                .streamingChatModel(model)
                .tools(new Tools())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        TokenStream tokenStream = assistant.chat(Long.valueOf(1),"你有什么工具");

        tokenStream
            .onPartialResponse((String partialResponse) -> {
                System.out.println("[Partial] " + partialResponse);
            })
            .onRetrieved((List<Content> contents) -> {
                System.out.println("------ Retrieved Contents ------");
                System.out.println(contents);
                System.out.println("-------------------------------");
            })
            .onToolExecuted((ToolExecution toolExecution) -> {
                System.out.println("------ Tool Executed ------");
                System.out.println(toolExecution);
                System.out.println("--------------------------");
            })
            .onCompleteResponse((ChatResponse response) -> {
                System.out.println("====== Complete Response ======");
                System.out.println(response);
                System.out.println("==============================");
            })
            .onError((Throwable error) -> {
                System.err.println("!!! Error Occurred !!!");
                error.printStackTrace();
            })
            .start();
        Thread.sleep(500000);
    }

    /**
     * 测试工具注册
     */
    @Test
    void testWithTool() throws InterruptedException {



        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";
        StreamingChatModel model =   OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();



        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();

        // stdio连接
        StdioMcpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of("node", "node_modules/howtocook-mcp/build/index.js"))
                .logEvents(true)
                .build();

        // sse连接
        HttpMcpTransport httpMcpTransport = new HttpMcpTransport.Builder()
                .sseUrl("https://mcp.amap.com/sse?key=8f620b0295ef01b81cd9fae881656a71")
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key("MyMCPClient")
                .transport(httpMcpTransport)
                .build();

        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        AssistantStream assistant = AiServices.builder(AssistantStream.class)
                .streamingChatModel(model)
                .toolProvider(toolProvider)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

        Long l = 1L;

        TokenStream tokenStream = assistant.chat(l,"广州今天的天气");

        tokenStream
                .onPartialResponse((String partialResponse) -> {
                    System.out.println("[Partial] " + partialResponse);
                })
                .onRetrieved((List<Content> contents) -> {
                    System.out.println("------ Retrieved Contents ------");
                    System.out.println(contents);
                    System.out.println("-------------------------------");
                })
                .onToolExecuted((ToolExecution toolExecution) -> {
                    System.out.println("------ Tool Executed ------");
                    System.out.println(toolExecution);
                    System.out.println("--------------------------");
                })
                .onCompleteResponse((ChatResponse response) -> {
                    System.out.println("====== Complete Response ======");
                    System.out.println(response);
                    System.out.println("==============================");
                })
                .onError((Throwable error) -> {
                    System.err.println("!!! Error Occurred !!!");
                    error.printStackTrace();
                })
                .start();
        Thread.sleep(500000);
    }


    /**
     * 测试RAG
     */
    @Test
    void testWithRAG() {
        String apiKey = "sk-hebdhedifmonpceqzyncovsvaxnukrugdghnqgrqnozzmkni";
        ChatModel model =   OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen3-8B")
                .build();

        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument("D:/java-project/EasyAgent/EasyAgent-backend/rag/RAG.txt", documentParser);


        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);


        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();


        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);



        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);


        // The final step is to build our AI Service,
        // configuring it to use the components we've created above.
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .contentRetriever(contentRetriever)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();


        String answer = assistant.chat(1,"LangChain4j集成了多少个LLM提供商");
        System.out.println(answer);
    }

}
