package com.ncba.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

public class SoapLoggingInterceptor implements ClientInterceptor {
    private static final Logger log = LoggerFactory.getLogger("soap.client");
    private static final String START_TIME = SoapLoggingInterceptor.class.getName() + ".start";
    private static final String OUTCOME = SoapLoggingInterceptor.class.getName() + ".outcome";

    private final String serviceName;
    private final String endpoint;
    private final boolean logPayload;
    private final int maxChars;

    public SoapLoggingInterceptor(String serviceName, String endpoint,
                                  boolean logPayload, int maxChars) {
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.logPayload = logPayload;
        this.maxChars = maxChars;
    }

    @Override
    public boolean handleRequest(MessageContext ctx) {
        ctx.setProperty(START_TIME, System.nanoTime());
        logMessage("request", ctx.getRequest(), "SOAP request sent", false);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext ctx) {
        ctx.setProperty(OUTCOME, "SUCCESS");
        logMessage("response", ctx.getResponse(), "SOAP response received", false);
        return true;
    }

    @Override
    public boolean handleFault(MessageContext ctx) {
        ctx.setProperty(OUTCOME, "SOAP_FAULT");
        logMessage("fault", ctx.getResponse(), "SOAP fault received", true);
        return true;
    }

    @Override
    public void afterCompletion(MessageContext ctx, Exception ex) throws WebServiceClientException {
        Object start = ctx.getProperty(START_TIME);
        long durationMs = start == null ? -1 : (System.nanoTime() - (long) start) / 1_000_000;
        String outcome = ex != null ? "ERROR" : String.valueOf(ctx.getProperty(OUTCOME));

        var event = ex != null ? log.atError().setCause(ex) : log.atInfo();
        event.setMessage("SOAP call completed")
                .addKeyValue("soap.service", serviceName)
                .addKeyValue("soap.endpoint", endpoint)
                .addKeyValue("soap.action", soapAction(ctx.getRequest()))
                .addKeyValue("soap.outcome", outcome)
                .addKeyValue("soap.duration_ms", durationMs)
                .log();
    }

    private void logMessage(String direction, WebServiceMessage msg, String text, boolean warn) {
        var event = warn ? log.atWarn() : log.atInfo();
        event.setMessage(text)
                .addKeyValue("soap.service", serviceName)
                .addKeyValue("soap.endpoint", endpoint)
                .addKeyValue("soap.direction", direction)
                .addKeyValue("soap.action", soapAction(msg));

        event.log();
    }

    private String soapAction(WebServiceMessage msg) {
        if (msg instanceof SoapMessage sm && sm.getSoapAction() != null) {
            return sm.getSoapAction().replace("\"", "");
        }
        return "unknown";
    }



}
