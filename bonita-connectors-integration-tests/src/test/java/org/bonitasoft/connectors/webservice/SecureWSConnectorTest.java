package org.bonitasoft.connectors.webservice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;

import org.bonitasoft.connectors.ws.cxf.SecureWSConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class SecureWSConnectorTest {

    private static final String nsSchema = "http://www.weather.gov/forecasts/xml/DWMLgen/schema/DWML.xsd";

    private static final String soapSchema = "http://schemas.xmlsoap.org/soap/envelope/";

    private static final String xsiSchema = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String encodingStyle = "http://schemas.xmlsoap.org/soap/encoding/";

    protected static final Logger LOG = Logger.getLogger(SecureWSConnectorTest.class.getName());

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOG.info("==== Starting test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOG.info("==== Failed test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void succeeded(final Description d) {
            LOG.info("==== Succeeded test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

    };

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test weather webservice.")
    @Test
    public void testBugTruc() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), "http://www.w3.org/2003/05/soap/bindings/HTTP/", "http://wsf.cdyne.com/WeatherWS/Weather.asmx",
                "Weather", "WeatherSoap12", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("White Plains"));
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test weather webservice with boolean parameters not set.")
    @Test
    public void testBugTrucWithBooleanParametersNotSet() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = executeWithBooleanParametersNotSet(request.toString(), "http://www.w3.org/2003/05/soap/bindings/HTTP/",
                "http://wsf.cdyne.com/WeatherWS/Weather.asmx",
                "Weather", "WeatherSoap12", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("White Plains"));
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Get a ConnectorException when wrong request format.")
    @Test(expected = ConnectorException.class)
    public void testWrongRequestFormat() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("<soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), "http://www.w3.org/2003/05/soap/bindings/HTTP/", "http://wsf.cdyne.com/WeatherWS/Weather.asmx",
                "Weather", "WeatherSoap12", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("False"));

    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Get a ConnectorException with wrong argument.")
    @Test
    public void testWrongArgument() throws Exception {
        final String zipCode = "ABCDEF";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), "http://www.w3.org/2003/05/soap/bindings/HTTP/", "http://wsf.cdyne.com/WeatherWS/Weather.asmx",
                "Weather", "WeatherSoap12", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("false"));

    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Get a ConnectorException with wrong binding.")
    @Test(expected = ConnectorException.class)
    public void testWrongBinding() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://wsf.cdyne.com/WeatherWS/Weather.asmx", "Weather",
                "WeatherSoap12", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("false"));
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Get a ConnectorException with inexisting port.")
    @Test(expected = ConnectorException.class)
    public void testInexistingPort() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://wsf.cdyne.com/WeatherWS/Weather.asmx", "Weather",
                "InexistingPort", "http://ws.cdyne.com/WeatherWS/", null, null, null, null);
        assertTrue(response, response.contains("false"));

    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Get a ConnectorException with wrong namespace.")
    @Test(expected = ConnectorException.class)
    public void testWrongNS() throws Exception {
        final String zipCode = "10012";// White Plains, NY
        final StringBuilder request = new StringBuilder("");
        request.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">");
        request.append("<soap:Header/>");
        request.append("<soap:Body>");
        request.append("<weat:GetCityWeatherByZIP>");
        request.append("<weat:ZIP>" + zipCode + "</weat:ZIP>");
        request.append("</weat:GetCityWeatherByZIP>");
        request.append("</soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://wsf.cdyne.com/WeatherWS/Weather.asmx", "Weather",
                "WeatherSoap12", "InexistingNS", null, null, null, null);
        assertTrue(response, response.contains("false"));

    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test weather webservice.")
    @Test
    public void testWeather() throws Exception {
        final String zipCode = "19110";// Philadelphia
        // final String zipCode = "33157";// Miami
        final StringBuilder request = new StringBuilder("");
        request.append("<S:Envelope xmlns:S=\"" + soapSchema + "\" xmlns:xsi=\"" + xsiSchema + "\" xmlns:schNS=\"" + nsSchema + "\"> ");
        request.append("  <S:Body>");
        request.append("    <LatLonListZipCode S:encodingStyle=\"" + encodingStyle + "\">");
        request.append("      <schNS:zipCodeList xsi:type=\"schNS:zipCodeListType\">" + zipCode + "</schNS:zipCodeList>");
        request.append("    </LatLonListZipCode>");
        request.append("  </S:Body>");
        request.append("</S:Envelope>");
        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php",
                "ndfdXML", "ndfdXMLPort", "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl", null, null, null);
        assertTrue(response, response.contains("&lt;latLonList&gt;39.9525,-75.1657&lt;/latLonList&gt;"));
    }

    @Ignore("remote service is down")
    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice", "soap11" },
            story = "Test conversion rate webservice with soap11.")
    @Test
    public void testConversionRateSoap11() throws Exception {
        final StringBuilder request = new StringBuilder();
        request.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        request.append("  <soap:Body>");
        request.append("    <ConversionRate xmlns=\"http://www.webserviceX.NET/\">");
        request.append("      <FromCurrency>AFA</FromCurrency>");
        request.append("      <ToCurrency>TRY</ToCurrency>");
        request.append("    </ConversionRate>");
        request.append("  </soap:Body>");
        request.append("</soap:Envelope>");
        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://www.webservicex.net/CurrencyConvertor.asmx",
                "CurrencyConvertor", "CurrencyConvertorSoap", "http://www.webserviceX.NET/", "http://www.webserviceX.NET/ConversionRate", null, null);
        assertTrue(response, response.contains("ConversionRateResponse"));
        assertTrue(response, response.contains("ConversionRateResult"));
    }

    @Ignore("remote service is down")
    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice", "soap12" },
            story = "Test conversion rate webservice with soap12.")
    @Test
    public void testConversionRateSoap12() throws Exception {
        final StringBuilder request = new StringBuilder();
        request.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
        request.append("  <soap12:Body>");
        request.append("    <ConversionRate xmlns=\"http://www.webserviceX.NET/\">");
        request.append("      <FromCurrency>EUR</FromCurrency>");
        request.append("      <ToCurrency>USD</ToCurrency>");
        request.append("    </ConversionRate>");
        request.append("  </soap12:Body>");
        request.append("</soap12:Envelope>");

        final String response = execute(request.toString(), SOAPBinding.SOAP12HTTP_BINDING, "http://www.webservicex.net/CurrencyConvertor.asmx",
                "CurrencyConvertor", "CurrencyConvertorSoap", "http://www.webserviceX.NET/", null, null, null);
        assertTrue(response, response.contains("ConversionRateResponse"));
        assertTrue(response, response.contains("ConversionRateResult"));
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testEnvelopeParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", null);
        parameters.put("binding", SOAPBinding.SOAP12HTTP_BINDING);
        parameters.put("endpointAddress", "http://www.webservicex.net/CurrencyConvertor.asmx");
        parameters.put("serviceName", "CurrencyConvertor");
        parameters.put("portName", "CurrencyConvertorSoap");
        parameters.put("serviceNS", "http://www.webserviceX.NET/");

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testBindingParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", "webservice request");
        parameters.put("binding", null);
        parameters.put("endpointAddress", "http://www.webservicex.net/CurrencyConvertor.asmx");
        parameters.put("serviceName", "CurrencyConvertor");
        parameters.put("portName", "CurrencyConvertorSoap");
        parameters.put("serviceNS", "http://www.webserviceX.NET/");

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testEndPointAddressParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", "webservice request");
        parameters.put("binding", SOAPBinding.SOAP12HTTP_BINDING);
        parameters.put("endpointAddress", null);
        parameters.put("serviceName", "CurrencyConvertor");
        parameters.put("portName", "CurrencyConvertorSoap");
        parameters.put("serviceNS", "http://www.webserviceX.NET/");

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testServiceNameParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", "webservice request");
        parameters.put("binding", SOAPBinding.SOAP12HTTP_BINDING);
        parameters.put("endpointAddress", "http://www.webservicex.net/CurrencyConvertor.asmx");
        parameters.put("serviceName", null);
        parameters.put("portName", "CurrencyConvertorSoap");
        parameters.put("serviceNS", "http://www.webserviceX.NET/");

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testPortNameParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", "webservice request");
        parameters.put("binding", SOAPBinding.SOAP12HTTP_BINDING);
        parameters.put("endpointAddress", "http://www.webservicex.net/CurrencyConvertor.asmx");
        parameters.put("serviceName", "CurrencyConvertor");
        parameters.put("portName", null);
        parameters.put("serviceNS", "http://www.webserviceX.NET/");

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    @Cover(classes = { SecureWSConnector.class }, concept = BPMNConcept.CONNECTOR, keywords = { "webservice" },
            story = "Test input parameters.")
    @Test(expected = ConnectorValidationException.class)
    public void testServiceNSParameter() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", "webservice request");
        parameters.put("binding", SOAPBinding.SOAP12HTTP_BINDING);
        parameters.put("endpointAddress", "http://www.webservicex.net/CurrencyConvertor.asmx");
        parameters.put("serviceName", "CurrencyConvertor");
        parameters.put("portName", "CurrencyConvertorSoap");
        parameters.put("serviceNS", null);

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
    }

    private String execute(final String request, final String binding, final String endpoint, final String service, final String port, final String ns,
            final String soapAction, final String username, final String password) throws Exception {
        return execute(request, binding, endpoint, service, port, ns, soapAction, username, password, null);
    }

    private String execute(final String request, final String binding, final String endpoint, final String service, final String port, final String ns,
            final String soapAction, final String username, final String password, final Map<String, List<String>> requestHeaders) throws Exception {
        return execute(request, binding, endpoint, service, port, ns, soapAction, username, password, requestHeaders, null);
    }

    private String execute(final String request, final String binding, final String endpoint, final String service, final String port, final String ns,
            final String soapAction, final String username, final String password, final Map<String, List<String>> requestHeaders,
            final List<List<Object>> requestHeadersAsList) throws Exception {

        if (requestHeadersAsList != null && requestHeaders != null) {
            throw new RuntimeException("only one of requestHeaders and requestHeadersAsList can be specified");
        }

        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", request);
        parameters.put("binding", binding);
        parameters.put("endpointAddress", endpoint);
        parameters.put("serviceName", service);
        parameters.put("portName", port);
        parameters.put("serviceNS", ns);
        parameters.put("soapAction", soapAction);
        parameters.put("userName", username);
        parameters.put("password", password);
        parameters.put("buildResponseDocumentEnvelope", true);
        parameters.put("buildResponseDocumentBody", true);
        parameters.put("printRequestAndResponse", true);

        if (requestHeaders != null) {
            final List<List<Object>> requestHeadersList = new ArrayList<List<Object>>();
            for (String key : requestHeaders.keySet()) {
                List<Object> row = new ArrayList<Object>();
                row.add(key);
                row.add(requestHeaders.get(key));
                requestHeadersList.add(row);
            }

            parameters.put("httpHeaders", requestHeadersList);
        } else {
            parameters.put("httpHeaders", null);
        }

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
        final Map<String, Object> outputs = webservice.execute();

        final Source response = (Source) outputs.get("sourceResponse");
        final String resultAsString = parse(response);
        printResponse(resultAsString);
        return resultAsString;
    }

    private String executeWithBooleanParametersNotSet(final String request, final String binding, final String endpoint, final String service,
            final String port, final String ns,
            final String soapAction, final String username, final String password, final Map<String, List<String>> requestHeaders) throws Exception {
        return executeWithBooleanParametersNotSet(request, binding, endpoint, service, port, ns, soapAction, username, password, requestHeaders, null);
    }

    private String executeWithBooleanParametersNotSet(final String request, final String binding, final String endpoint, final String service,
            final String port, final String ns,
            final String soapAction, final String username, final String password, final Map<String, List<String>> requestHeaders,
            final List<List<Object>> requestHeadersAsList) throws Exception {

        if (requestHeadersAsList != null && requestHeaders != null) {
            throw new RuntimeException("only one of requestHeaders and requestHeadersAsList can be specified");
        }

        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("envelope", request);
        parameters.put("binding", binding);
        parameters.put("endpointAddress", endpoint);
        parameters.put("serviceName", service);
        parameters.put("portName", port);
        parameters.put("serviceNS", ns);
        parameters.put("soapAction", soapAction);
        parameters.put("userName", username);
        parameters.put("password", password);

        if (requestHeaders != null) {
            final List<List<Object>> requestHeadersList = new ArrayList<List<Object>>();
            for (String key : requestHeaders.keySet()) {
                List<Object> row = new ArrayList<Object>();
                row.add(key);
                row.add(requestHeaders.get(key));
                requestHeadersList.add(row);
            }

            parameters.put("httpHeaders", requestHeadersList);
        } else {
            parameters.put("httpHeaders", null);
        }

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
        final Map<String, Object> outputs = webservice.execute();

        final Source response = (Source) outputs.get("sourceResponse");
        final String resultAsString = parse(response);
        printResponse(resultAsString);
        return resultAsString;
    }

    private String parse(final Source response) throws TransformerFactoryConfigurationError, TransformerException {
        assertNotNull(response);
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        transformer.transform(response, result);
        return writer.toString();
    }

    private void printResponse(final String response) {
        assertNotNull(response);
        System.out.println("response=\n" + response);
    }

}
