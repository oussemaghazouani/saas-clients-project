package com.saasclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Client IA réutilisable (modèle Claude). Optionnel : n'est actif que si la variable
 * d'environnement {@code ANTHROPIC_API_KEY} est fournie. Sinon {@link #estActif()} renvoie
 * false et les services retombent sur leur moteur heuristique local. Toute erreur réseau
 * est silencieuse (retourne {@link Optional#empty()}) afin de ne jamais casser une opération métier.
 */
@Slf4j
@Component
public class AiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    /** Vrai si une clé API est configurée (donc si l'IA générative est disponible). */
    public boolean estActif() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Génère un texte à partir d'une consigne système et d'un prompt utilisateur.
     * @return le texte généré, ou {@link Optional#empty()} si l'IA est inactive ou en erreur.
     */
    public Optional<String> generer(String systeme, String prompt, int maxTokens) {
        if (!estActif()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systeme,
                "messages", new Object[]{ Map.of("role", "user", "content", prompt) }
            );

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .timeout(Duration.ofSeconds(30))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("IA indisponible : HTTP {}", resp.statusCode());
                return Optional.empty();
            }
            JsonNode content = objectMapper.readTree(resp.body()).path("content");
            if (content.isArray() && content.size() > 0) {
                String texte = content.get(0).path("text").asText();
                return (texte != null && !texte.isBlank()) ? Optional.of(texte.trim()) : Optional.empty();
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Appel IA échoué, repli sur le moteur local : {}", e.getMessage());
            return Optional.empty();
        }
    }
}
