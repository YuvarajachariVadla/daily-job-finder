package com.jobs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

/**
 * DailyJobFinder – Collect jobs using SerpAPI and email daily report.
 */
public class DailyJobFinder {

    // ----- CONFIG -----
    private static final String SERPAPI_KEY = "69ab69ece7c5bcfa2614447f3bf5d462aef50608a54f4f0460fc5c29a80ded38";
    private static final String FROM_EMAIL = "vadlayuvaraj8247@gmail.com";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_USER = "vadlayuvaraj8247@gmail.com";
    private static final String SMTP_PASS = "bgaqhrrfgquvgfkl";
    private static final String TO_EMAIL = "vadlayuvaraj8247@gmail.com";

    // --- Job model class ---
    static class Job {
        String title;
        String company;
        String link;
        String date;
        String source;

        Job(String title, String company, String link, String date,String source) {
            this.title = title;
            this.company = company;
            this.link = link;
            this.date = date;
            this.source = source;

        }
    }

    public static void main(String[] args) {
        try {
            Map<String, List<Job>> results = new LinkedHashMap<>();

            // --- Search for general IT job roles ---
            List<String> IT_QUERIES = Arrays.asList(
                    "Java Developer jobs",
                    "Backend Developer jobs",
                    "Frontend Developer jobs",
                    "Full Stack Developer jobs",
                    "Software Engineer jobs",
                    "Data Engineer jobs",
                    "AI Engineer jobs",
                    "Machine Learning Engineer jobs",
                    "SDET jobs",
                    "System Engineer jobs",
                    "Database Administrator jobs",
                    "Business Analyst IT jobs",
                    "IT Support jobs",
                    "Product Engineer jobs",
                    "Data Analyst jobs",
                    "Data Scientist jobs"
            );

            for (String query : IT_QUERIES) {
                List<Job> jobs = fetchJobs(query);
                Collections.reverse(jobs); // newest first
                if (!jobs.isEmpty()) {
                    results.put(query, jobs);
                }
            }

            // --- Build Email Content ---
            String htmlContent = buildHtml(results);

            // --- Send Email ---
            sendEmail("Daily Jobs Report - " + LocalDate.now(ZoneId.of("Asia/Kolkata")), htmlContent);

            System.out.println("Email sent successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Fetch jobs from SerpAPI ---
    private static List<Job> fetchJobs(String query) throws IOException {
        String url = "https://serpapi.com/search.json?engine=google_jobs&q=" +
                query.replace(" ", "+") +
                "&location=India&hl=en&api_key=" + SERPAPI_KEY;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(URI.create(url));
            String jsonResponse = client.execute(request, response -> EntityUtils.toString(response.getEntity()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            // Debug: Print full JSON response (optional)
//            System.out.println(root.toPrettyString());

            List<Job> jobs = new ArrayList<>();
            if (root.has("jobs_results")) {
                for (JsonNode job : root.get("jobs_results")) {
                    String title = job.path("title").asText("Unknown Title");
                    String company = job.path("company_name").asText("Unknown Company");

                    // ✅ Extract apply link with multiple fallbacks
                    String link = null;

                    // 1. Best option: apply_options
                    if (job.has("apply_options") && job.get("apply_options").isArray()
                            && job.get("apply_options").size() > 0) {
                        link = job.get("apply_options").get(0).path("link").asText();
                    }

                    // 2. Next option: related_links
                    if ((link == null || link.isBlank()) && job.has("related_links")
                            && job.get("related_links").isArray() && job.get("related_links").size() > 0) {
                        link = job.get("related_links").get(0).path("link").asText();
                    }

                    // 3. Detected extensions
                    if ((link == null || link.isBlank()) && job.has("detected_extensions")
                            && job.get("detected_extensions").has("apply_link")) {
                        link = job.get("detected_extensions").path("apply_link").asText();
                    }

                    // 4. Plain link field
                    if ((link == null || link.isBlank()) && job.has("link")) {
                        link = job.path("link").asText();
                    }

                    // 5. Fallback: share_link
                    if ((link == null || link.isBlank()) && job.has("share_link")) {
                        link = job.path("share_link").asText();
                    }

                    // Final fallback
                    if (link == null || link.isBlank()) {
                        link = "Not Available";
                    }

                    // Date handling
                    String date = job.path("detected_extensions").path("posted_at").asText();
                    String source = job.has("via") ? job.get("via").asText("Google Jobs") : "Google Jobs";

                    jobs.add(new Job(title, company, link, date.isEmpty() ? "Today" : date, source));

                    
                }
            }
            return jobs;
        }
    }

    // --- Build email HTML content ---
    private static String buildHtml(Map<String, List<Job>> results) {
        StringBuilder sb = new StringBuilder();

        // Email heading
        sb.append("<h2 style='color:#2E86C1;'>Daily IT Job Report</h2>");

        if (results.isEmpty()) {
            sb.append("<p>No jobs posted in the past 3 days. Please check again..</p>");
        } else {
            for (Map.Entry<String, List<Job>> entry : results.entrySet()) {
                sb.append("<h3 style='color:#2874A6;'>").append(entry.getKey()).append("</h3>");
                sb.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse;'>");
                sb.append("<tr style='background-color:#4CAF50; color:white;'>")
                        .append("<th>Job Title</th>")
                        .append("<th>Company</th>")
                        .append("<th>Source</th>")
                        .append("<th>Link</th>")
                        .append("<th>Date Posted</th>")
                        .append("</tr>");

                boolean alternate = false;
                for (Job job : entry.getValue()) {
                    String rowColor = alternate ? "#f2f2f2" : "#ffffff";

                    sb.append("<tr style='background-color:").append(rowColor).append(";'>")
                            .append("<td>").append(job.title).append("</td>")
                            .append("<td>").append(job.company).append("</td>")
                            .append("<td>").append(job.source).append("</td>")

                            .append("<td>");
                    if (job.link != null && !job.link.trim().isEmpty()
                            && !job.link.equalsIgnoreCase("Not Available")) {
                        sb.append("<a href='").append(job.link).append("' target='_blank'>Apply</a>");
                    } else {
                        sb.append("Not Available");
                    }
                    sb.append("</td>");

                    sb.append("<td>").append(job.date).append("</td>")
                            .append("</tr>");

                    alternate = !alternate;
                }
                sb.append("</table><br>");
            }
        }

        // Add message before the image
        sb.append("<p style='text-align:center; font-style:italic; color:#555;'>Keep learning, keep applying! Your next opportunity is just around the corner.</p>");
        // Adding image at the end of the email
        sb.append("<div style='text-align:center; margin-top:20px;'>")
                .append("<img src='cid:image' alt='Work Image' style='width:300px;'>")
                .append("</div>");

        return sb.toString();
    }

    // --- Send Email ---
    private static void sendEmail(String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        // Create the message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO_EMAIL));
        message.setSubject(subject);

        // Create multipart for HTML + image
        Multipart multipart = new jakarta.mail.internet.MimeMultipart("related");

        // HTML part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
        multipart.addBodyPart(htmlPart);

        // Image part
        MimeBodyPart imagePart = new MimeBodyPart();
        File imageFile = new File("C:\\Users\\user\\eclipse-workspace\\dailyjobfinder\\src\\main\\resources\\download.jpg");
        imagePart.setDataHandler(new jakarta.activation.DataHandler(new jakarta.activation.FileDataSource(imageFile)));
        imagePart.setHeader("Content-ID", "<image>");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);

        // Set the multipart content
        message.setContent(multipart);

        // Send email
        Transport.send(message);
    }
}
