package murach.email.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import jakarta.mail.MessagingException;

public class MailUtilGmail {

    private static final String BREVO_API_KEY = "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";

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        try {
            String contentKey = bodyIsHTML ? "\"htmlContent\":" : "\"textContent\":";

            // Chuẩn bị payload JSON gửi tới Brevo qua cổng HTTPS 443
            String jsonPayload = "{"
                    + "\"sender\":{\"name\":\"Murach Email List\",\"email\":\"" + escapeJson(from) + "\"},"
                    + "\"to\":[{\"email\":\"" + escapeJson(to) + "\"}],"
                    + "\"subject\":\"" + escapeJson(subject) + "\","
                    + contentKey + "\"" + escapeJson(body) + "\""
                    + "}";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", BREVO_API_KEY)
                    .header("content-type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                System.err.println("Brevo API error: " + response.statusCode() + " - " + response.body());
                throw new MessagingException("Brevo API error (" + response.statusCode() + "): " + response.body());
            } else {
                System.out.println("Email sent successfully via Brevo HTTPS: " + response.body());
            }
        } catch (MessagingException me) {
            throw me;
        } catch (Exception e) {
            throw new MessagingException("Failed to send email via Brevo: " + e.getMessage(), e);
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
