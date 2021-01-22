package de.samply.common.ldmclient.centraxx;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.samply.common.ldmclient.LdmClientException;
import de.samply.common.ldmclient.LdmClientUtil;
import de.samply.common.ldmclient.LdmClientView;
import de.samply.common.ldmclient.centraxx.model.CentraxxInfo;
import de.samply.common.ldmclient.centraxx.utils.PercentageLogger;
import de.samply.common.ldmclient.model.LdmQueryResult;
import de.samply.share.model.ccp.QueryResult;
import de.samply.share.model.common.Error;
import de.samply.share.model.common.QueryResultStatistic;
import de.samply.share.model.common.View;
import de.samply.share.utils.QueryConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to communicate with the local datamanagement implementation "Centraxx".
 */
public class LdmClientCentraxx extends
    LdmClientView<QueryResult, QueryResultStatistic, de.samply.share.model.ccp.Error,
        de.samply.share.model.ccp.View> {

  private static final String REST_PATH_TEILER = "teiler/";
  private static final String REST_PATH_INFO = "info/";
  private static final boolean CACHING_DEFAULT_VALUE = false;
  private final Logger logger = LoggerFactory.getLogger(LdmClientCentraxx.class);

  public LdmClientCentraxx(CloseableHttpClient httpClient, String centraxxBaseUrl)
      throws LdmClientException {
    this(httpClient, centraxxBaseUrl, CACHING_DEFAULT_VALUE);
  }

  public LdmClientCentraxx(CloseableHttpClient httpClient, String centraxxBaseUrl,
      boolean useCaching) throws LdmClientException {
    this(httpClient, centraxxBaseUrl, useCaching, CACHE_DEFAULT_SIZE);
  }

  public LdmClientCentraxx(CloseableHttpClient httpClient, String centraxxBaseUrl,
      boolean useCaching, int cacheSize) throws LdmClientException {
    super(httpClient, centraxxBaseUrl, useCaching, cacheSize);
  }

  public boolean isLdmCentraxx() {
    return true;
  }

  @Override
  protected String getFullPath(boolean statisticsOnly) {
    String path = REST_PATH_REQUESTS;
    if (statisticsOnly) {
      path = path + REST_RESULTS_ONLY_SUFFIX;
    }

    //return LdmClientUtil.addTrailingSlash(getLdmBaseUrl()) + REST_PATH_TEILER + path;
    return LdmClientUtil.addTrailingSlash(getLdmBaseUrl()) + path;
  }

  @Override
  protected Class<de.samply.share.model.ccp.View> getSpecificViewClass() {
    return de.samply.share.model.ccp.View.class;
  }

  @Override
  protected Class<QueryResult> getResultClass() {
    return QueryResult.class;
  }

  @Override
  protected Class<QueryResultStatistic> getStatisticsClass() {
    return QueryResultStatistic.class;
  }

  private Class getStatisticsClass(String response) {
    return (response.contains("http://de.kairos.centraxx/ccp/QueryResultStatistic"))
        ? de.samply.common.ldmclient.centraxx.model.QueryResultStatistic.class
        : getStatisticsClass();
  }

  @Override
  protected Class<de.samply.share.model.ccp.Error> getErrorClass() {
    return de.samply.share.model.ccp.Error.class;
  }

  @Override
  protected Class<?> getObjectFactoryClassForPostView() {
    return de.samply.share.model.ccp.ObjectFactory.class;
  }

  @Override
  protected Class<?> getObjectFactoryClassForResult() {
    return de.samply.share.model.ccp.ObjectFactory.class;
  }

  @Override
  protected de.samply.share.model.ccp.View convertCommonViewToSpecificView(View view)
      throws JAXBException {
    return QueryConverter.convertCommonViewToCcpView(view);
  }

  @Override
  protected View convertSpecificViewToCommonView(de.samply.share.model.ccp.View ccpView)
      throws JAXBException {
    return QueryConverter.convertCcpViewToCommonView(ccpView);
  }

  /**
   * Transforms a CCP QueryResultStatistic to a common QueryResultStatistic.
   *
   * @param queryResultStatistic the QueryResultStatistic in the centraxx namespace
   * @return the QueryResultStatistic in the common namespace
   */
  public LdmQueryResult convertQueryResultStatisticToCommonQueryResultStatistic(
      de.samply.common.ldmclient.centraxx.model.QueryResultStatistic queryResultStatistic) {
    QueryResultStatistic commonQueryResultStatistic = new QueryResultStatistic();
    commonQueryResultStatistic.setNumberOfPages(queryResultStatistic.getNumberOfPages());
    commonQueryResultStatistic.setRequestId(queryResultStatistic.getRequestId());
    commonQueryResultStatistic.setTotalSize(queryResultStatistic.getTotalSize());

    return new LdmQueryResult(commonQueryResultStatistic);
  }

  @Override
  protected LdmQueryResult convertQueryResultStatisticToCommonQueryResultStatistic(
      QueryResultStatistic qrs) throws JAXBException {
    return new LdmQueryResult(qrs);
  }


  @Override
  protected LdmQueryResult convertSpecificErrorToCommonError(de.samply.share.model.ccp.Error error)
      throws JAXBException {
    Error convertedError = QueryConverter.convertCcpErrorToCommonError(error);

    return new LdmQueryResult(convertedError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QueryResult getResult(String location) throws LdmClientException {
    QueryResult queryResult = new QueryResult();
    QueryResultStatistic queryResultStatistic = getQueryResultStatistic(location);

    if (queryResultStatistic != null && queryResultStatistic.getTotalSize() > 0) {

      int numberOfPages = queryResultStatistic.getNumberOfPages();
      PercentageLogger percentageLogger = new PercentageLogger(logger, numberOfPages,
          "getting results from centraxx...");

      for (int i = 0; i < numberOfPages; i++) {
        QueryResult queryResultPage = getResultPage(location, i);
        queryResult.getPatient().addAll(queryResultPage.getPatient());
        percentageLogger.incrementCounter();
      }
      queryResult.setId(queryResultStatistic.getRequestId());


    }
    return queryResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getVersionString() throws LdmClientException {
    CentraxxInfo centraxxInfo = getInfo();
    return (centraxxInfo != null) ? centraxxInfo.getCentraxxVersion() : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserAgentInfo() throws LdmClientException {

    String centraxxVersion = getVersionString();
    return (centraxxVersion != null) ? "Centraxx/" + centraxxVersion : "No CentraXX info provided";

  }

  // This class is intended for the compatibility with previous versions of centraXX 3.14
  // (and with the new ones)
  // This class is very similar to getStatsOrError defined in the library ldmclient
  // TODO: Delete this method after all DKTK-Sites use CentraXX 3.14
  @Override
  public LdmQueryResult getStatsOrError(String location) throws LdmClientException {
    HttpGet httpGet = new HttpGet(LdmClientUtil.addTrailingSlash(location) + REST_PATH_STATS);
    addHttpHeaders(httpGet);

    if (isLdmCentraxx()) {
      // Apparently, it may take a bit longer to reply when a new user session has to be created...
      // so use an extensive timeout of 1 minute
      RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000)
          .setConnectTimeout(60000).setConnectionRequestTimeout(60000).build();
      httpGet.setConfig(requestConfig);
    }

    try {
      CloseableHttpResponse response = getHttpClient().execute(httpGet);
      int statusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      String entityOutput = EntityUtils.toString(entity, Consts.UTF_8);
      if (statusCode == HttpStatus.SC_OK) {

        Class statisticsClass = getStatisticsClass(entityOutput);

        JAXBContext jaxbContext = JAXBContext.newInstance(statisticsClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Object qrs = jaxbUnmarshaller.unmarshal(new StringReader(entityOutput));
        EntityUtils.consume(entity);
        response.close();

        return (qrs instanceof de.samply.common.ldmclient.centraxx.model.QueryResultStatistic)
            ? convertQueryResultStatisticToCommonQueryResultStatistic(
            (de.samply.common.ldmclient.centraxx.model.QueryResultStatistic) qrs) :
            convertQueryResultStatisticToCommonQueryResultStatistic((QueryResultStatistic) qrs);

      } else if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
        JAXBContext jaxbContext = JAXBContext.newInstance(getErrorClass());
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        de.samply.share.model.ccp.Error error = (de.samply.share.model.ccp.Error) jaxbUnmarshaller
            .unmarshal(new StringReader(entityOutput));
        EntityUtils.consume(entity);
        response.close();
        return convertSpecificErrorToCommonError(error);
      } else if (statusCode == HttpStatus.SC_ACCEPTED) {
        response.close();
        logger
            .debug("Statistics not written yet. LDM client is probably busy with another request.");
        return LdmQueryResult.EMPTY;
      } else {
        response.close();
        throw new LdmClientException("Unexpected response code: " + statusCode);
      }
    } catch (IOException | JAXBException e) {
      throw new LdmClientException("While trying to read stats/error", e);
    }
  }

  private CentraxxInfo getInfo() throws LdmClientException {
    ResponseHandler<CentraxxInfo> responseHandler = response -> {
      StatusLine statusLine = response.getStatusLine();
      HttpEntity entity = response.getEntity();
      if (statusLine.getStatusCode() >= 300) {
        throw new HttpResponseException(
            statusLine.getStatusCode(),
            statusLine.getReasonPhrase());
      }
      if (entity == null) {
        throw new ClientProtocolException("Response contains no content");
      }
      try (InputStream instream = entity.getContent(); Reader reader = new InputStreamReader(
          instream, Consts.UTF_8)) {
        return new Gson().fromJson(reader, CentraxxInfo.class);
      } catch (JsonSyntaxException | JsonIOException e) {
        logger.warn("JSON Exception caught while trying to unmarshal centraxx info...");
        return null;
      }
    };

    String infoUrl = getInfoUrl();
    try {
      return getHttpClient().execute(new HttpGet(infoUrl), responseHandler);
    } catch (IOException e) {
      throw new LdmClientException("Could not read centraxx info", e);
    }
  }

  private String getInfoUrl() {

    //TODO : FIX URL
    String baseUrl = LdmClientUtil.addTrailingSlash(getLdmBaseUrl());
    if (baseUrl.contains(REST_PATH_TEILER)) {
      int index = baseUrl.indexOf(REST_PATH_TEILER);
      baseUrl = baseUrl.substring(0, index);
    }
    return baseUrl + REST_PATH_INFO;

  }

  /**
   * Todo.
   *
   * @param queryResult Todo.
   * @return Todo.
   * @throws JAXBException        Todo.
   * @throws LdmClientException   Todo.
   * @throws InterruptedException Todo.
   * @throws IOException          Todo.
   */
  public QueryResult exportQuery(QueryResult queryResult)
      throws JAXBException, LdmClientException, InterruptedException, IOException {
    ArrayList<String> patientids = new ArrayList<>();
    for (int i = 0; i < queryResult.getPatient().size(); i++) {
      patientids.add(queryResult.getPatient().get(i).getId());
    }
    StringBuilder builder = new StringBuilder();
    builder.append(
        "<ns5:View xmlns:ns2=\"http://schema.samply.de/common/MdrKey\" "
            + "xmlns:ns3=\"http://schema.samply.de/common/Value\" "
            + "xmlns:ns4=\"http://schema.samply.de/common/Attribute\" "
            + "xmlns:ns5=\"http://schema.samply.de/common/Query\">\n"
            + "<ns5:Query>\n"
            + "<ns5:Where>\n"
            + "<ns5:Or>\n");
    for (String patientid : patientids) {
      builder.append("<ns5:Eq>\n"
          + "<ns4:Attribute>\n"
          + "<ns2:MdrKey>urn:dktk:dataelement:91:1</ns2:MdrKey>\n"
          + "<ns3:Value>" + patientid + "</ns3:Value>\n"
          + "</ns4:Attribute>\n"
          + "</ns5:Eq>\n");
    }
    builder.append("</ns5:Or>\n"
        + "</ns5:Where>\n"
        + "</ns5:Query>\n"
        + "<ns5:ViewFields>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:54:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:26:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:1:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:24:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:25:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:46:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:43:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:45:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:74:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:72:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:73:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:80:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:99:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:79:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:101:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:78:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:100:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:2:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:10:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:81:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:18:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:82:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:28:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:21:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:29:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:77:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:9:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:3:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:8:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:5:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:19:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:4:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:98:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:7:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:6:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:83:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:89:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:39:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:38:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:69:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:23:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:67:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:40:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:36:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:33:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:34:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:48:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:53:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:49:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:90:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:50:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:97:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:dktk:dataelement:95:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:77:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:78:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:90:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:93:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:89:*</ns2:MdrKey>\n"
        + "<ns2:MdrKey>urn:adt:dataelement:91:*</ns2:MdrKey>\n"
        + "</ns5:ViewFields>\n"
        + "</ns5:View>");

    View v = QueryConverter.xmlToView(builder.toString());
    String location = this.postView(v);
    HttpGet httpGet = new HttpGet(location + "/" + REST_PATH_RESULT + REST_PARAM_PAGE + 0);
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000)
        .setConnectTimeout(60000).setConnectionRequestTimeout(60000).build();
    httpGet.setConfig(requestConfig);
    int statusCode = 202;
    while (statusCode == 202) {
      CloseableHttpResponse response = getHttpClient().execute(httpGet);
      statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 202) {
        TimeUnit.SECONDS.sleep(3);
      }
    }
    queryResult = this.getResult(location);
    System.out.println(builder.toString());
    return queryResult;
  }
}
