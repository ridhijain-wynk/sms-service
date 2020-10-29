package in.wynk.sms.sender;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.sms.model.SMSDto;
import in.wynk.sms.model.enums.SMSPriority;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static in.wynk.sms.constants.SmsMarkers.PROMOTIONAL_MSG_ERROR;
import static in.wynk.sms.constants.SmsMarkers.TRANSACTIONAL_MSG_ERROR;

@Component
public class AirtelSMSSender extends AbstractSMSSender {

    private static final Logger logger = LoggerFactory.getLogger(AirtelSMSSender.class);

    private static final int MAX_CONNECTIONS = 200;
    private static final int MAX_NO_OF_THREADS = 200;


    private final PoolingHttpClientConnectionManager connectionManager;

    private final CloseableHttpClient httpClient;

    private final CloseableHttpClient defaultHttpClient;

    private final ExecutorService executorService = new ThreadPoolExecutor(50, MAX_NO_OF_THREADS, 10 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private final BlockingQueue<Integer> responseCodeQueue = new LinkedBlockingQueue<Integer>();

    private final Map<Integer, Long> responseCodeStats = new HashMap<Integer, Long>();

    private final Map<String, String> htmlEntityMap = new HashMap<>();

    private volatile boolean shouldRun = true;

    // Last 1 month response codes
    private final ConcurrentHashMap<LocalDate, ConcurrentHashMap<Integer, AtomicLong>> responseCodeWithCountPerDayMap = new ConcurrentHashMap<LocalDate, ConcurrentHashMap<Integer, AtomicLong>>(32, 0.75f, MAX_NO_OF_THREADS);

    private final List<String> toEmailIds = Arrays.asList("siddhant.zawar@wynk.in,pankaj.thakur@wynk.in");

    public AirtelSMSSender() {
        try {

            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();
            connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(60000).setConnectTimeout(10000).setSocketTimeout(7000).build();
            connectionManager.setMaxTotal(MAX_CONNECTIONS);
            connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS);
            ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    // Honor 'keep-alive' header
                    HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                    // otherwise keep alive for 30 seconds
                    return 30 * 1000;
                }
            };

            httpClient = HttpClients.custom().setConnectionManager(connectionManager).setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).setKeepAliveStrategy(keepAliveStrategy)
                    .evictIdleConnections(30L, TimeUnit.SECONDS).build();
            defaultHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).evictIdleConnections(30L, TimeUnit.SECONDS).build();
            executorService.execute(new ResponseCodeStatsThread());

            htmlEntityMap.put("&", "&amp;");
            htmlEntityMap.put("<", "&lt;");
            htmlEntityMap.put(">", "&gt;");
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }


    @Override
    @AnalyseTransaction(name = "sendSmsAirtel")
    public void sendMessage(String msisdn, String shortCode, String text, Boolean useDND, long createTimestamp, String priority, String smsId) {
        try {
            AnalyticService.update("message", text);
            SMSMsg sms = new SMSMsg();
            sms.shortcode = shortCode;
            sms.toMsisdn = msisdn;
            sms.message = filterHtmlEntity(text);
            sms.messageId = "" + System.currentTimeMillis(); // Utils.generateUUID(true,true);
            String mtRequestXML = createMTRequestXML(sms);
            postCoreJava(mtRequestXML, msisdn, createTimestamp, priority, smsId);
        } catch (RejectedExecutionException th) {
            logger.error("Error while Delivering SMS, ERROR: {}", th.getMessage(), th);
            logger.info("Task got rejected, ThreadPoolStats: {}", getThreadPoolStats());
        } catch (Throwable th) {
            logger.error("Error while Delivering SMS, ERROR: {}", th.getMessage(), th);
        }
    }

    public boolean sendSmsToSriLanka(SMSDto SMSDto) {
        boolean success = false;
        if (SMSDto != null) {
            int currentStatusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            String requestUrl = "http://sms.airtel.lk:5000/sms/send_sms.php?username=wynk&password=W123Nk&src=Wynk&dr=1";
            if (org.apache.commons.lang3.StringUtils.isNotBlank(SMSDto.getMsisdn())) {
                requestUrl = requestUrl.concat("&dst=").concat(SMSDto.getMsisdn().replace("+", ""));
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(SMSDto.getMessage())) {
                try {
                    requestUrl = requestUrl.concat("&msg=");
                    requestUrl = requestUrl.concat(URLEncoder.encode(SMSDto.getMessage(), "UTF-8"));
                } catch (Throwable th) {
                    logger.error("Exception in url Encoding for the msisdn {}", SMSDto.getMsisdn());
                }
            }
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Content-Type", "x-www-form-urlencoded");
            try {
                long startTime = System.currentTimeMillis();
                CloseableHttpResponse response = null;
                try {
                    response = defaultHttpClient.execute(request);
                } catch (Exception e) {
                    logger.error("Error sending sms to user for sri lanka" + e.getMessage() + e);
                }
                logger.info("Response Time to send Sri lanka SMS :" + (System.currentTimeMillis() - startTime) + " ms");
                HttpEntity entity = response.getEntity();
                String responseStr = EntityUtils.toString(entity);
                StatusLine statusLine = response.getStatusLine();
                currentStatusCode = statusLine.getStatusCode();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(responseStr) && responseStr.contains("Operation success")) {
                    logger.info("Successfully sent message to msisdn: { " + SMSDto.getMsisdn() + "}, responseCode: { " +
                            currentStatusCode + "}, response: { " + responseStr + "}");
                    logger.info("Time by SriLanka.SMS Priority: " + SMSDto.getPriority() + " : Time :" + (System.currentTimeMillis() - startTime) + " ms");
                    success = true;
                } else {
                    logger.error("Error Sending SMS to Msisdn: {" + SMSDto.getMsisdn() + "} SMS: { " + SMSDto.getMessage() + "}, ERROR: { " + responseStr + "}");
                    success = false;
                }
            } catch (Throwable th) {
                logger.error("Error Sending SMS to Msisdn: {" + SMSDto.getMsisdn() + "} SMS: { " + SMSDto.getMessage() + "}, ERROR: { " + th.getMessage() + "}", th);
                success = false;
            } finally {
                logger.debug("Going to populateResponseCodesDayWise");
                populateResponseCodesDayWise(currentStatusCode);
                logger.debug("Done populateResponseCodesDayWise");
                request.reset();
            }
        }
        return success;
    }

    public boolean sendSmsToSriLankaTunnel(SMSDto SMSDto) {
        boolean success = false;
        if (SMSDto != null) {
            int currentStatusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            String requestUrl = "http://10.200.186.1/cgi-local/sendsms.pl?login=wynk&pass=wynk&src=WYNK&type=text";
            if (org.apache.commons.lang3.StringUtils.isNotBlank(SMSDto.getMsisdn())) {
                requestUrl = requestUrl.concat("&msisdn=").concat(SMSDto.getMsisdn().replace("+", ""));
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(SMSDto.getMessage())) {
                try {
                    requestUrl = requestUrl.concat("&sms=");
                    requestUrl = requestUrl.concat(URLEncoder.encode(SMSDto.getMessage(), "UTF-8"));
                } catch (Throwable th) {
                    logger.error("Exception in url Encoding for the msisdn {}", SMSDto.getMsisdn());
                }
            }
            HttpGet request = new HttpGet(requestUrl);
            request.addHeader("Content-Type", "x-www-form-urlencoded");
            try {
                long startTime = System.currentTimeMillis();
                CloseableHttpResponse response = null;
                try {
                    response = defaultHttpClient.execute(request);
                } catch (Exception e) {
                    logger.error("Error sending sms to user for sri lanka" + e.getMessage() + e);
                }
                logger.info("Response Time to send Sri lanka SMS :" + (System.currentTimeMillis() - startTime) + " ms");
                HttpEntity entity = response.getEntity();
                String responseStr = EntityUtils.toString(entity);
                StatusLine statusLine = response.getStatusLine();
                currentStatusCode = statusLine.getStatusCode();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(responseStr) && responseStr.contains("Messages accepted")) {
                    logger.info("Successfully sent message to msisdn: { " + SMSDto.getMsisdn() + "}, responseCode: { " +
                            currentStatusCode + "}, response: { " + responseStr + "}");
                    logger.info("Time by SriLanka.SMS Priority: " + SMSDto.getPriority() + " : Time :" + (System.currentTimeMillis() - startTime) + " ms");
                    success = true;
                } else {
                    logger.error("Error Sending SMS to Msisdn: {" + SMSDto.getMsisdn() + "} SMS: { " + SMSDto.getMessage() + "}, ERROR: { " + responseStr + "}");
                    success = false;
                }
            } catch (Throwable th) {
                logger.error("Error Sending SMS to Msisdn: {" + SMSDto.getMsisdn() + "} SMS: { " + SMSDto.getMessage() + "}, ERROR: { " + th.getMessage() + "}", th);
                success = false;
            } finally {
                logger.debug("Going to populateResponseCodesDayWise");
                populateResponseCodesDayWise(currentStatusCode);
                logger.debug("Done populateResponseCodesDayWise");
                request.reset();
            }
        }
        return success;
    }

    private String filterHtmlEntity(String text) {
        for (Map.Entry<String, String> m : htmlEntityMap.entrySet()) {
            text = text.replaceAll(m.getKey(), m.getValue());
        }
        return text;

    }

    private boolean postCoreJava(String dataXml, String msisdn, long createTimestamp, String priority, String id) {
        AnalyticService.update("msisdn", msisdn);
        AnalyticService.update("smsAirtelRequest", dataXml);
        AnalyticService.update("priority", priority);
        if (priority.equals(SMSPriority.HIGH.name())) {
            return postSmsForHighPriority(dataXml, msisdn);
        } else {
            return postSmsForLowAndMediumPriority(dataXml, msisdn);
        }
    }

    private boolean postSmsForHighPriority(String dataXml, String msisdn) {
        String url = "https://mbnf.airtelworld.com:9443";
        AnalyticService.update("url", url);
        final String username = "bs1b";
        final String password = "bs1b";
        boolean success = false;
        try {
            success = sendSmsRequestToAirtel(dataXml, url, username, password);
        } catch (Exception e) {
            AnalyticService.update("smsAirtelException", e.toString());
            logger.error(TRANSACTIONAL_MSG_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
        return success;
    }

    private boolean postSmsForLowAndMediumPriority(String dataXml, String msisdn) {
        String url = "https://mbbf.airtelworld.com:9443";
        AnalyticService.update("url", url);
        final String username = "wynk12";
        final String password = "12wynk";
        boolean success = false;
        try {
            success = sendSmsRequestToAirtel(dataXml, url, username, password);
        } catch (Exception e) {
            AnalyticService.update("smsAirtelException", e.toString());
            logger.error(PROMOTIONAL_MSG_ERROR, "Error Sending SMS to Msisdn: {" + msisdn + "} SMS: { " + dataXml + "}, ERROR: { " + e.getMessage() + "}", e);
        }
        return success;
    }

    private boolean sendSmsRequestToAirtel(String dataXml, String url, String username, String password) throws Exception {
        boolean success;
        HttpPost request = new HttpPost(url);
        try {
            request.setEntity(new StringEntity(dataXml));
            if (StringUtils.isNotEmpty(username)) {
                String authString = username + ":" + password;
                String encValue = Base64.encodeBase64String(authString.getBytes());
                request.addHeader("Authorization", "Basic " + encValue);
            }
            request.addHeader("Content-Type", "text/xml");
            logger.info("SMS request created:" + request);
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseStr = EntityUtils.toString(entity);
            AnalyticService.update("smsAirtelResponse", responseStr);
            success = true;
        } finally {
            request.reset();
        }
        return success;
    }

    private void populateResponseCodesDayWise(int currentStatusCode) {
        try {
            LocalDate today = new LocalDate();
            responseCodeWithCountPerDayMap.putIfAbsent(today, new ConcurrentHashMap<Integer, AtomicLong>(5, 0.76f, MAX_NO_OF_THREADS));
            ConcurrentHashMap<Integer, AtomicLong> statusCodesForToday = responseCodeWithCountPerDayMap.get(today);
            statusCodesForToday.putIfAbsent(currentStatusCode, new AtomicLong(0));
            AtomicLong currentStatusCodeCount = statusCodesForToday.get(currentStatusCode);
            currentStatusCodeCount.incrementAndGet();
        } catch (Exception e) {
            logger.error("Exception occured while populateResponseCodesDayWise" + e);
        }
    }

    private String createMTRequestXML(SMSMsg mrObject) {
        if (mrObject == null) {
            return null;
        }
        String toMsisdn = mrObject.getToMsisdn().startsWith("+") ? mrObject.getToMsisdn().substring(1) : mrObject.getToMsisdn();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        strBuilder.append("<message>");
        strBuilder.append("<sms type=\"mt\">");
        if (mrObject.messageId != null && !mrObject.messageId.isEmpty()) {
            strBuilder.append("<destination messageid=\"").append(mrObject.messageId).append("\">");
        } else {
            strBuilder.append("<destination>");
        }
        strBuilder.append("<address>");
        strBuilder.append("<number type=\"international\">").append(toMsisdn).append("</number>");
        strBuilder.append("</address></destination>");
        if (mrObject.shortcode != null) {
            strBuilder.append("<source><address>").append("<alphanumeric>").append(mrObject.shortcode).append("</alphanumeric></address></source>");
        } else if (mrObject.getFromMsisdn() != null) {
            strBuilder.append("<source><address>").append(mrObject.getFromMsisdn()).append("</address></source>");
        }
        strBuilder.append("<rsr type=\"all\"/>");
        strBuilder.append("<ud encoding=\"unicode\" type=\"").append(mrObject.udType).append("\">");
        // String is converted to hexString to support non english text too.
        strBuilder.append(convertToHexString(mrObject.message, false)[1]).append("</ud>");
        strBuilder.append("</sms></message>");
        return strBuilder.toString();
    }
/*
    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
*//*        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setRedisHost("127.0.0.1");
        redisConfig.setRedisPort(6379);
        redisConfig.setRedisDB(0);
        redisConfig.setMaxIdle(8);
        redisConfig.setMaxActive(8);

        RedisServiceManager redisServiceManager = new RedisServiceManager(redisConfig);
        AirtelSMSSender smsSender = new AirtelSMSSender();
        smsSender.dndRedisServiceManager = redisServiceManager;*//*
        //smsSender.sendMessage("+919599822189", SMSConstants.SMS_MESSAGE_SHORTCODE, "test msg", false);

        //AirtelSMSSender smsSender = new AirtelSMSSender();
        //smsSender.sendMessage("+919403303795", SMSConstants.SMS_MESSAGE_SHORTCODE, "Congratulations! 1GB free Airtel data has been credited to your account. Watch your favourite shows Live on Airtel TV App with 300+ channels & 6000 movies for FREE.", false,System.currentTimeMillis(),"HIGH","");
    }*/

    class SMSMsg {

        public boolean isMT;

        public String rsr = "all";     // all | failure | success | delayed |
        // success_failure | success_delayed |
        // failure_delayed | sent | sent_delivered

        public String udType = "text";    // text or bindary

        public String udEncoding = "default"; // unicode or default

        public String vpType = "relative"; // absolute, relative

        // public Date vpDate;

        public String fromMsisdn;
        public String toMsisdn;

        public String message;
        public String messageId;
        public String shortcode;

        public String getToMsisdn() {
            return toMsisdn;
        }

        public void setToMsisdn(String toMsisdn) {
            this.toMsisdn = toMsisdn;
        }

        public String getFromMsisdn() {
            return fromMsisdn;
        }

        public void setFromMsisdn(String fromMsisdn) {
            this.fromMsisdn = fromMsisdn;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "SMSMsg{" +
                    "isMT=" + isMT +
                    ", rsr='" + rsr + '\'' +
                    ", udType='" + udType + '\'' +
                    ", udEncoding='" + udEncoding + '\'' +
                    ", vpType='" + vpType + '\'' +
                    ", fromMsisdn='" + fromMsisdn + '\'' +
                    ", toMsisdn='" + toMsisdn + '\'' +
                    ", message='" + message + '\'' +
                    ", messageId='" + messageId + '\'' +
                    ", shortcode='" + shortcode + '\'' +
                    '}';
        }
    }

    @Override
    public void shutdown() {
        try {
            shouldRun = false;
            if (null != httpClient) {
                httpClient.close();
            }
            if (null != executorService) {
                executorService.shutdown();
            }
        } catch (Throwable th) {
            logger.error("Error while destroying httpclient, ERROR: {}", th.getMessage(), th);
        }
    }

    @Override
    public String getConnectionPoolStats() {
        String response = null;
        if (null != connectionManager) {
            PoolStats stats = connectionManager.getTotalStats();
            response = stats.toString();
        }
        return response;
    }

    @Override
    public String getThreadPoolStats() {
        String response = StringUtils.EMPTY;
        if (null != executorService) {
            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
            response = poolExecutor.toString();
        }
        return response;
    }

    @Override
    public String getResponseCodeStats() {
        String response = String.valueOf(responseCodeStats);
        return response;
    }

    class ResponseCodeStatsThread implements Runnable {

        @Override
        public void run() {
            Deque<Integer> responseCodeList = new ArrayDeque<Integer>(500);
            while (shouldRun) {
                try {
                    if (!responseCodeQueue.isEmpty()) {
                        responseCodeQueue.drainTo(responseCodeList, 500);
                        while (CollectionUtils.isNotEmpty(responseCodeList)) {
                            Integer responseCode = responseCodeList.pop();
                            Long count = responseCodeStats.get(responseCode);
                            if (null == count || 0l == count) {
                                count = 0l;
                            }
                            responseCodeStats.put(responseCode, ++count);
                        }
                    }
                    Thread.sleep(5000);
                } catch (Throwable th) {
                    logger.error("Error while making stats for responseCodes, ERROR: {}", th.getMessage(), th);
                }
            }
        }
    }

    private String getYesterdayResponseCodeStats() {
        LocalDate yesterday = new LocalDate().minusDays(1);
        return String.valueOf(responseCodeWithCountPerDayMap.get(yesterday));
    }


}