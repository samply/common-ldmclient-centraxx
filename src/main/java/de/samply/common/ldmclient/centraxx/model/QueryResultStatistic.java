package de.samply.common.ldmclient.centraxx.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for anonymous complex type.
 * The following schema fragment specifies the expected content contained within this class.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "requestId",
    "numberOfPages",
    "totalSize"
})
@XmlRootElement(name = "queryResultStatistic", namespace = "http://de.kairos.centraxx/ccp/QueryResultStatistic")
public class QueryResultStatistic implements Serializable {

  @XmlElement(required = true)
  private String requestId;
  private int numberOfPages;
  private int totalSize;

  /**
   * Gets the value of the requestId property.
   *
   * @return possible object is {@link String }
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * Sets the value of the requestId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRequestId(String value) {
    this.requestId = value;
  }

  /**
   * Gets the value of the numberOfPages property.
   * @return the number of pages
   */
  public int getNumberOfPages() {
    return numberOfPages;
  }

  /**
   * Sets the value of the numberOfPages property.
   * @param value the number of pages to set
   */
  public void setNumberOfPages(int value) {
    this.numberOfPages = value;
  }

  /**
   * Gets the value of the totalSize property.
   * @return the total size of the result
   */
  public int getTotalSize() {
    return totalSize;
  }

  /**
   * Sets the value of the totalSize property.
   * @param value the total count size
   */
  public void setTotalSize(int value) {
    this.totalSize = value;
  }

}
