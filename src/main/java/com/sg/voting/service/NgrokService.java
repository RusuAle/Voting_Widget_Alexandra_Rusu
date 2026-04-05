package com.sg.voting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class NgrokService {

    @Value("${server.port:8080}")
    private int port;

    @Value("${ngrok.authtoken:}")
    private String authToken;

    private String publicUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void startTunnel() {
        try {
            ProcessBuilder pb;

            if (authToken != null && !authToken.isBlank()) {
                new ProcessBuilder("ngrok", "config", "add-authtoken", authToken)
                        .redirectErrorStream(true)
                        .start()
                        .waitFor();
            }

            pb = new ProcessBuilder("ngrok", "http", String.valueOf(port), "--log=stdout");
            pb.redirectErrorStream(true);
            pb.start();

            Thread.sleep(2500);

            publicUrl = fetchPublicUrlFromNgrokApi();

            if (publicUrl != null) {
                System.out.println("NGROK TUNNEL ACTIVE");
                System.out.printf( "Public URL: %-40s %n", publicUrl);
                System.out.println("Share: '${publicUrl}/polls' with voters!");
            } else {
                System.out.println("\n Ngrok started but could not read public URL. Running locally.\n");
            }

        } catch (Exception e) {
            System.out.println("\nNgrok not found or failed to start: " + e.getMessage());
            System.out.println("   Download ngrok from https://ngrok.com/download and add it to PATH.\n");
        }
    }

    private String fetchPublicUrlFromNgrokApi() {
        try {
            URL url = new URL("http://localhost:4040/api/tunnels");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(sb.toString());
            JsonNode tunnels = root.get("tunnels");

            if (tunnels != null && tunnels.isArray()) {
                for (JsonNode tunnel : tunnels) {
                    String proto = tunnel.path("proto").asText("");
                    String pubUrl = tunnel.path("public_url").asText("");
                    if (proto.equals("https") && !pubUrl.isBlank()) {
                        return pubUrl;
                    }
                }
                if (tunnels.size() > 0) {
                    return tunnels.get(0).path("public_url").asText(null);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public boolean isActive() {
        return publicUrl != null && !publicUrl.isBlank();
    }
}