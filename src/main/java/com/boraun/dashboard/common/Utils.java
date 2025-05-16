package com.boraun.dashboard.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
@Slf4j
public class Utils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String getHeaderParameter(HttpServletRequest request, String parameterName) {
        return request.getHeader(parameterName);
    }

    public String getClientIP(HttpServletRequest request) {
        String[] IP_HEADER_CANDIDATES = new String[]{"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};
        String[] var3 = IP_HEADER_CANDIDATES;
        int var4 = IP_HEADER_CANDIDATES.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String header = var3[var5];
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    public HashMap<String, String> getHeaders(HttpServletRequest request) {
        HashMap<String, String> headers = new HashMap<>();
        String headerName = "";
        String headerValue = "";
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                headerName = headerNames.nextElement();
                headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    public String getHeader(HttpServletResponse response, String parameterName) {
        return response.getHeader(parameterName);
    }

    public HashMap<String, String> getHeaders(HttpServletResponse response) {
        HashMap<String, String> headers = new HashMap<>();
        AtomicReference<String> headerName = new AtomicReference<>("");
        AtomicReference<String> headerValue = new AtomicReference<>("");
        Collection<String> headerNames = response.getHeaderNames();
        headerNames.forEach(s -> {
            headerName.set(s);
            headerValue.set(response.getHeader(s));
            headers.put(headerName.get(), headerValue.get());
        });
        return headers;
    }

    public boolean isNull(Object object) {
        if (object instanceof String) {
            String val = ((String) object).trim();
            if (val == null) return true;
            if (val.equalsIgnoreCase("")) return true;
        }
        return Objects.isNull(object);
    }

    public int getRandom(int from, int to) {
        Random rn = new Random();
        return rn.nextInt(to - from + 1) + from;
    }

    public Date getDate(int numOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, numOfMonth);
        return calendar.getTime();
    }

    public Date getDate(Optional<String> optionalDate) {
        String dateValue = optionalDate.orElse(getDate("dd-MM-yyyy"));
        return getDate(dateValue, "dd-MM-yyyy");
    }

    public String getDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public String getDate() {
        return getDate("dd-MMM-yyyy HH:mm:ss");
    }

    public String getTimestamp() {
        return getDate("yyyyMMddHHmmssS");
    }

    public Date getDate(String dateValue, String formatter) {
        dateValue = dateValue.trim();
        SimpleDateFormat f = new SimpleDateFormat(formatter);
        try {
            Date d = f.parse(dateValue);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }


    public boolean nonNull(Object object) {
        return !isNull(object);
    }

    public String toJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        try {
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jsonString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error at toJsonString because " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    public String toJsonString(Object object, boolean prettyFormat) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        try {
            if (prettyFormat) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                jsonString = mapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            log.error("Error at toJsonString because " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    public Object toObject(String jsonData, Class clazz) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        try {
            Object object = mapper.readValue(jsonData, clazz);
            return object;
        } catch (JsonProcessingException e) {
            log.error("Error at toJsonString because " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Object toObject(HashMap<String, String> hashMap, Class clazz) {
        ObjectMapper mapper = new ObjectMapper();
        Object object = mapper.convertValue(hashMap, clazz);
        return object;
    }

    public static boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
        String ajaxHeader = httpServletRequest.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(ajaxHeader);
    }



    public int getPageNumber(Optional<Integer> pageNumber) {
        return (pageNumber.orElse(0) < 1) ? 0 : pageNumber.get() - 1;
    }

    public boolean isUserAuthenticated() {
        boolean result = (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated() && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken));
        return result;
    }

    public String getRandomSecuredString(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, (char[]) null, new SecureRandom());
    }

    public String getExtensionFilename(MultipartFile file) {
        return getExtensionFilename(file.getOriginalFilename());
    }

    public static byte[] compressFileImage(MultipartFile file) throws IOException {
        log.info("Original Image Byte Size - " + file.getBytes().length);
        float quality = 0.3f;
        String imageName = file.getOriginalFilename();
        assert imageName != null;
        String imageExtension = imageName.substring(imageName.lastIndexOf(".") + 1);
        ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(imageExtension).next();
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        if (!Utils.getExtensionFilename(file).equalsIgnoreCase("png")) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(quality);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(baos);
        imageWriter.setOutput(imageOutputStream);
        BufferedImage originalImage = null;
        try (InputStream inputStream = file.getInputStream()) {
            originalImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            String info = String.format("compressImage - bufferedImage (file %s)- IOException - message: %s ", imageName, e.getMessage());
            log.error(info);
            return baos.toByteArray();
        }
        IIOImage image = new IIOImage(originalImage, null, null);
        try {
            imageWriter.write(null, image, imageWriteParam);
        } catch (IOException e) {
            String info = String.format("compressImage - imageWriter (file %s)- IOException - message: %s ", imageName, e.getMessage());
            log.error(info);
        } finally {
            imageWriter.dispose();
        }
        log.info("Compressed Image Byte Size - " + baos.toByteArray().length);
        return baos.toByteArray();
    }

    public String formatLongToDisplayString(Long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.2fk", number / 1000.0);
        } else {
            return String.format("%.2fm", number / 1000000.0);
        }
    }

    public boolean sendEmail(String host, int port, boolean authenticationEnable, SendEmailSecurityOption sslOption, String username, String password, @NonNull String fromEmailAddress, @NonNull String toEmailAddressCommaOption, String ccEmailAddressCommaOption, @NonNull String subject, @NonNull String body, List<String> attachmentRelativePath) {
        if (fromEmailAddress == null) {
            throw new NullPointerException("fromEmailAddress is marked non-null but is null");
        } else if (toEmailAddressCommaOption == null) {
            throw new NullPointerException("toEmailAddressCommaOption is marked non-null but is null");
        } else if (subject == null) {
            throw new NullPointerException("subject is marked non-null but is null");
        } else if (body == null) {
            throw new NullPointerException("body is marked non-null but is null");
        } else {
            log.info("Sending Email via " + host + ":" + port + " with " + username + " auth: " + authenticationEnable + " ssl: " + sslOption);
            Properties prop = new Properties();
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", String.valueOf(port));
            prop.put("mail.smtp.auth", String.valueOf(authenticationEnable));
            prop.put("mail.smtp.ssl.trust", "*");
            switch (sslOption) {
                case SSL:
                    prop.put("mail.smtp.ssl.enable", "true");
                case None:
                default:
                    break;
                case TTSL:
                    prop.put("mail.smtp.starttls.enable", "true");
                    break;
                case TTSLv12:
                    prop.put("mail.smtp.starttls.enable", "true");
                    prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
            }

            return sendEmail(prop, username, password, fromEmailAddress, toEmailAddressCommaOption, ccEmailAddressCommaOption, (String) null, subject, body, attachmentRelativePath);
        }
    }

    public boolean sendEmail(Properties properties, final String username, final String password, @NonNull String fromEmailAddress, @NonNull String toEmailAddressCommaOption, String ccEmailAddressCommaOption, String bccEmailAddressCommaOption, @NonNull String subject, @NonNull String body, List<String> attachmentRelativePath) {
        if (fromEmailAddress == null) {
            throw new NullPointerException("fromEmailAddress is marked non-null but is null");
        } else if (toEmailAddressCommaOption == null) {
            throw new NullPointerException("toEmailAddressCommaOption is marked non-null but is null");
        } else if (subject == null) {
            throw new NullPointerException("subject is marked non-null but is null");
        } else if (body == null) {
            throw new NullPointerException("body is marked non-null but is null");
        } else {
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            Message msg = new MimeMessage(session);

            try {
                msg.setFrom(new InternetAddress(fromEmailAddress));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmailAddressCommaOption, false));
                if (Utils.nonNull(ccEmailAddressCommaOption)) {
                    msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmailAddressCommaOption, false));
                }

                if (Utils.nonNull(bccEmailAddressCommaOption)) {
                    msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccEmailAddressCommaOption, false));
                }

                msg.setSubject(subject);
                MimeBodyPart text = new MimeBodyPart();
                text.setDataHandler(new DataHandler(new HTMLDataSource(body)));
                List<MimeBodyPart> attachments = null;
                if (nonNull(attachmentRelativePath)) {
                    for (int i = 0; i < attachmentRelativePath.size(); ++i) {
                        MimeBodyPart attachment = new MimeBodyPart();
                        FileDataSource fileDataSource = new FileDataSource((String) attachmentRelativePath.get(i));

                        try {
                            attachment.setDataHandler(new DataHandler(fileDataSource));
                            attachment.setFileName(fileDataSource.getName());
                            if (attachments == null) {
                                attachments = new ArrayList();
                            }

                            attachments.add(attachment);
                        } catch (MessagingException var19) {
                            MessagingException e = var19;
                            e.printStackTrace();
                        }
                    }
                }

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(text);
                if (attachments != null && attachments.size() > 0) {
                    attachments.forEach((mimeBodyPart) -> {
                        try {
                            multipart.addBodyPart(mimeBodyPart);
                        } catch (MessagingException var3) {
                            MessagingException e = var3;
                            e.printStackTrace();
                        }

                    });
                }

                msg.setContent(multipart);
                msg.setSentDate(new Date());
                Transport.send(msg);
                log.info("Email has been sent successfully");
                return true;
            } catch (MessagingException var20) {
                MessagingException e = var20;
                log.error("Email sending failed because => " + e.getLocalizedMessage());
                log.error(e.getMessage(), e.fillInStackTrace());
                e.printStackTrace();
                return false;
            }
        }
    }

    public enum TemplateBodyType {
        HTML, TEXT;

        private TemplateBodyType() {
        }
    }


    public enum SendEmailSecurityOption {
        None, SSL, TTSL, TTSLv12;

        private SendEmailSecurityOption() {
        }
    }

    static class HTMLDataSource implements DataSource {
        private String html;

        public HTMLDataSource(String htmlString) {
            this.html = htmlString;
        }

        public InputStream getInputStream() throws IOException {
            if (this.html == null) {
                throw new IOException("html message is null!");
            } else {
                return new ByteArrayInputStream(this.html.getBytes());
            }
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        public String getContentType() {
            return "text/html; charset=utf-8";
        }

        public String getName() {
            return "HTMLDataSource";
        }
    }

    public String getBaseUrl(HttpServletRequest request) {
        String path = request.getContextPath();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
    }

    public String getRandomSecuredStringForMail(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(new SecureRandom().nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public String toBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public boolean compareValidDate(Date inputDate, int hour) {
        long MillisecondPerHour = (long) (hour * 60 * 60) * 1000L;
        return Math.abs((new Date()).getTime() - inputDate.getTime()) > MillisecondPerHour;
    }

    public String getLastPackageName(Class mainClazz) {
        String[] tmp = mainClazz.getPackage().getName().split("\\.");
        return tmp[tmp.length - 1];
    }

    public String getExtensionFilename(String filename) {
        int pos = filename.lastIndexOf(".");
        return filename.substring(pos + 1);
    }


    public String getFormatThousand(BigDecimal value, int scaleSize) {
        BigDecimal bd = value.setScale(scaleSize, RoundingMode.HALF_UP);
        bd = bd.stripTrailingZeros();
        if (bd.scale() <= 0) {
            return String.format("%,d", bd.longValue());
        }
        StringBuilder pattern = new StringBuilder("#,##0");
        if (bd.scale() > 0) {
            pattern.append(".");
            pattern.append("0".repeat(bd.scale()));
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        return decimalFormat.format(bd);
    }
}
