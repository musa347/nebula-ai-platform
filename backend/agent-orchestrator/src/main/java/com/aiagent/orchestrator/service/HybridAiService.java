package com.aiagent.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Hybrid AI service with fallback logic:
 * 1. Gemini (best quality, free) - via OpenAI-compatible API
 * 2. Groq (fast, free) - via OpenAI-compatible API
 * 3. Ollama (local backup)
 */
@Service
public class HybridAiService {

    private static final Logger log = LoggerFactory.getLogger(HybridAiService.class);

    @Value("${ai.provider.primary:gemini}")
    private String primaryProvider;

    @Value("${ai.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${ai.groq.enabled:false}")
    private boolean groqEnabled;

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${ai.groq.model:llama-3.1-70b-versatile}")
    private String groqModel;

    @Value("${ai.ollama.enabled:true}")
    private boolean ollamaEnabled;

    @Value("${spring.ai.ollama.chat.options.model:qwen2.5-coder:1.5b}")
    private String ollamaModel;

    private final OllamaChatModel ollamaChatModel;
    private final RestTemplate restTemplate = new RestTemplate();

    public HybridAiService(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    public String callWithFallback(String prompt) {
        String[] providers = determineProviderOrder();

        for (String provider : providers) {
            try {
                log.info("Attempting AI call with provider: {}", provider);
                String response = callProvider(provider, prompt);

                if (response != null && !response.trim().isEmpty()) {
                    log.info("Successfully received response from: {}", provider);
                    return response;
                }
            } catch (Exception e) {
                log.warn("Provider {} failed: {}", provider, e.getMessage());
            }
        }

        throw new RuntimeException("All AI providers failed");
    }

    private String[] determineProviderOrder() {
        if ("gemini".equalsIgnoreCase(primaryProvider) && geminiEnabled) {
            return new String[]{"gemini", "groq", "ollama"};
        } else if ("groq".equalsIgnoreCase(primaryProvider) && groqEnabled) {
            return new String[]{"groq", "gemini", "ollama"};
        } else {
            return new String[]{"ollama", "gemini", "groq"};
        }
    }

    private String callProvider(String provider, String prompt) {
        switch (provider.toLowerCase()) {
            case "gemini":
                if (!geminiEnabled) return null;
                return callGemini(prompt);

            case "groq":
                if (!groqEnabled) return null;
                return callGroq(prompt);

            case "ollama":
                if (!ollamaEnabled) return null;
                return callOllama(prompt);

            default:
                return null;
        }
    }

    private String callGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.warn("Gemini API key not configured");
            return null;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            Map<String, Object> request = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            parts.put("text", prompt);
            contents.put("parts", new Object[]{parts});
            request.put("contents", new Object[]{contents});

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("maxOutputTokens", 2048);
            request.put("generationConfig", generationConfig);

            Map response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("candidates")) {
                java.util.List candidates = (java.util.List) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map candidate = (Map) candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    java.util.List responseParts = (java.util.List) content.get("parts");
                    Map firstPart = (Map) responseParts.get(0);
                    String text = (String) firstPart.get("text");
                    // Unescape HTML entities
                    String unescaped = unescapeHtml(text);
                    log.info("Gemini response (first 200 chars): {}", unescaped.substring(0, Math.min(200, unescaped.length())));
                    return unescaped;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Gemini call failed", e);
            throw new RuntimeException("Gemini error: " + e.getMessage(), e);
        }
    }

    private String callGroq(String prompt) {
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            log.warn("Groq API key not configured");
            return null;
        }

        try {
            OpenAiChatModel groqChatModel = new OpenAiChatModel(
                    new org.springframework.ai.openai.api.OpenAiApi(
                            "https://api.groq.com/openai",
                            groqApiKey
                    ),
                    OpenAiChatOptions.builder()
                            .withModel(groqModel)
                            .withMaxTokens(2048)
                            .build()
            );

            ChatResponse response = groqChatModel.call(new Prompt(prompt));
            return response.getResult().getOutput().getContent();

        } catch (Exception e) {
            log.error("Groq call failed", e);
            throw new RuntimeException("Groq error: " + e.getMessage(), e);
        }
    }

    private String callOllama(String prompt) {
        try {
            OllamaOptions options = OllamaOptions.create()
                    .withModel(ollamaModel)
                    .withNumPredict(2048)
                    .withFormat("json");

            ChatResponse response = ollamaChatModel.call(new Prompt(prompt, options));
            return response.getResult().getOutput().getContent();

        } catch (Exception e) {
            log.error("Ollama call failed", e);
            throw new RuntimeException("Ollama error: " + e.getMessage(), e);
        }
    }

    private String unescapeHtml(String text) {
        if (text == null) return null;
        return text
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#39;", "'");
    }
}
