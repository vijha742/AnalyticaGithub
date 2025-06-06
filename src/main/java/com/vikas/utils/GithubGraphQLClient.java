package com.vikas.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Component
public class GithubGraphQLClient {
    private final String githubGraphqlUrl;
    private final String githubToken;
    private final RestTemplate restTemplate;

    public GithubGraphQLClient(RestTemplate restTemplate, @Value("${github.api.graphql-url}") String githubGraphqlUrl, @Value("${github.api.token}") String githubToken) {
        this.restTemplate = restTemplate;
        this.githubGraphqlUrl = githubGraphqlUrl;
        this.githubToken = githubToken;
    }

    /**
     * Execute a GraphQL query against the GitHub API
     *
     * @param query GraphQL query string
     * @param variables Map of variables for the query
     * @return Map containing the 'data' element of the response
     */
    public Map<String, Object> executeQuery(String query, Map<String, ?> variables) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            var response =
                    restTemplate
                            .exchange(githubGraphqlUrl, HttpMethod.POST, entity, Map.class)
                            .getBody();

            return response != null ? (Map<String, Object>) response.get("data") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTotalFiles(String owner, String repo) {
        String url = String.format("https://api.github.com/repos/%s/%s/git/trees/HEAD?recursive=1", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange( url, HttpMethod.GET, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> tree = (List<Map<String, Object>>) response.getBody().get("tree");

            if (tree == null) return 0;
            return (int) tree.stream()
                    .filter(entry -> "blob".equals(entry.get("type")))
                    .count();
        }
        return 0;
    }
}
