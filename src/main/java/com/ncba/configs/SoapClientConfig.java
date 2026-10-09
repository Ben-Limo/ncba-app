package com.ncba.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.time.Duration;

@Configuration
public class SoapClientConfig {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.ncba.cis.soap.client");
        return marshaller;
    }

    @Bean
    SoapClient soapClient(Jaxb2Marshaller marshaller,
                                  @Value("${soap.country.info.url}") String url,
                                  @Value("${soap.log.payload:true}") boolean logPayload,
                                  @Value("${soap.log.max-payload-chars:8000}") int maxChars) {

        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofSeconds(5));
        sender.setReadTimeout(Duration.ofSeconds(30));

        SoapClient client = new SoapClient();
        client.setDefaultUri(url);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setMessageSender(sender);

        client.setInterceptors(new ClientInterceptor[] {
                new SoapLoggingInterceptor("country-info-service", url, logPayload, maxChars)
        });
        return client;
    }
}
