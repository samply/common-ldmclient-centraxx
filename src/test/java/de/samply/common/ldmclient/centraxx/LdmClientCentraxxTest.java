

package de.samply.common.ldmclient.centraxx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import com.google.common.base.Stopwatch;
import de.samply.common.ldmclient.AbstractLdmClient;
import de.samply.common.ldmclient.LdmClientUtil;
import de.samply.common.ldmclient.model.LdmQueryResult;
import de.samply.share.model.ccp.QueryResult;
import de.samply.share.model.common.View;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Test Class for the LDM Connector for Centraxx Backends
 */
public class LdmClientCentraxxTest {

  private static final Logger logger = LoggerFactory.getLogger(LdmClientCentraxxTest.class);
  private static final boolean CACHING = true;

  private static final String centraxxBaseUrl = "http://localhost:9090/centraxx/";

  // In order to avoid unnecessary creation of too many requests...use a pre-existing result id from centraxx. Check the queryresults folder on the centraxx machine
  private static final String predefinedResultId = "99bdb78a-4cd4-418b-b4de-95dcc331e83f";
  private static final String predefinedResultLocation =
      centraxxBaseUrl + AbstractLdmClient.REST_PATH_REQUESTS + "/" + predefinedResultId;

  // Same for errors
  private static final String predefinedErrorResultId = "0ebf8ebb-0132-46f6-aaf6-f682682279dd";
  private static final String predefinedErrorResultLocation =
      centraxxBaseUrl + AbstractLdmClient.REST_PATH_REQUESTS + "/" + predefinedErrorResultId;

  private static final String VIEW_FILENAME_OK = "exampleView.xml";

  private LdmClientCentraxx ldmClientCentraxx;
  private CloseableHttpClient httpClient;
  /*

  @Before
  public void setUp() throws Exception {
    this.httpClient = HttpClients.createDefault();
    this.ldmClientCentraxx = new LdmClientCentraxx(httpClient, centraxxBaseUrl, CACHING);
  }

  @After
  public void tearDown() throws Exception {
    httpClient.close();
    ldmClientCentraxx.cleanQueryResultsCache();
  }

  // Since postView calls postViewString, just run the test once
  @Test
  public void testPostView() throws Exception {
    String viewString = readXmlFromFile(VIEW_FILENAME_OK);
    View view = xmlToView(viewString);
    String location = ldmClientCentraxx.postView(view);
    assertFalse(LdmClientUtil.isNullOrEmpty(location));
  }

  @Test
  public void testGetResult() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedResultId));
    assertTrue(ldmClientCentraxx.getResult(predefinedResultLocation) instanceof QueryResult);
  }

  @Test
  public void testGetResultPageThreeTimes() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedResultId));
    Stopwatch stopwatch = Stopwatch.createStarted();
    assertTrue(ldmClientCentraxx.getResultPage(predefinedResultLocation, 0) instanceof QueryResult);
    logger.info("Getting page 0 took " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");
    stopwatch.reset().start();
    assertTrue(ldmClientCentraxx.getResultPage(predefinedResultLocation, 1) instanceof QueryResult);
    logger.info("Getting page 1 took " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");
    stopwatch.reset().start();
    assertTrue(ldmClientCentraxx.getResultPage(predefinedResultLocation, 0) instanceof QueryResult);
    logger.info("Getting page 0 took " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");
  }

  @Test
  public void testGetResultPage() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedResultId));
    assertTrue(ldmClientCentraxx.getResultPage(predefinedResultLocation, 0) instanceof QueryResult);
  }

  @Test
  public void testGetStatsOrError() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedResultId) && LdmClientUtil
        .isNullOrEmpty(predefinedErrorResultId));
    LdmQueryResult ldmQueryResult = ldmClientCentraxx.getStatsOrError(predefinedResultLocation);
    assertTrue(ldmQueryResult.hasResult() || ldmQueryResult.hasError());
  }

  @Test
  public void testGetQueryResultStatistic() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedResultId));
    assertNotNull(ldmClientCentraxx.getQueryResultStatistic(predefinedResultLocation));
  }

  @Test
  public void testGetError() throws Exception {
    assumeFalse(LdmClientUtil.isNullOrEmpty(predefinedErrorResultId));
    assertNotNull(ldmClientCentraxx.getError(predefinedErrorResultLocation));
  }

  @Test
  public void testGetVersionString() throws Exception {
    assertFalse(LdmClientUtil.isNullOrEmpty(ldmClientCentraxx.getVersionString()));
  }

  private String readXmlFromFile(String filename) {
    String xml = "";

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
      xml = IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException ioEx) {
      ioEx.printStackTrace();
    }

    return xml;
  }

  private View xmlToView(String xml) throws JAXBException {
    InputSource inputSource = new InputSource(new StringReader(xml));

    JAXBContext jaxbContext = JAXBContext.newInstance(View.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (View) jaxbUnmarshaller.unmarshal(inputSource);
  }
  */

}
