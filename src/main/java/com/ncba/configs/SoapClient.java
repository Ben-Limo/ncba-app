package com.ncba.configs;

import com.ncba.cis.soap.client.CountryISOCode;
import com.ncba.cis.soap.client.CountryISOCodeResponse;
import com.ncba.cis.soap.client.FullCountryInfo;
import com.ncba.cis.soap.client.FullCountryInfoResponse;
import org.slf4j.MDC;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class SoapClient extends WebServiceGatewaySupport {

    public CountryISOCodeResponse getCountryISOCode(String country) {
        CountryISOCode isoCode = new CountryISOCode();
        isoCode.setSCountryName(country);
        return (CountryISOCodeResponse) getWebServiceTemplate().marshalSendAndReceive(isoCode);
    }

    public FullCountryInfoResponse getFullCountryInfo(String isoCode) {
        FullCountryInfo countryInfo = new FullCountryInfo();
        countryInfo.setSCountryISOCode(isoCode);
        return (FullCountryInfoResponse) getWebServiceTemplate().marshalSendAndReceive(countryInfo);
    }
}
