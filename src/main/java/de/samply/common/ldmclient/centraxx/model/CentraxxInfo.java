package de.samply.common.ldmclient.centraxx.model;

/**
 * Created by michael on 22.02.17.
 */
public class CentraxxInfo {

  private String centraxxVersion;
  private String currentSessionCount;
  private String status;

  /**
   * Get the Centraxx version.
   * @return the centraxxVersion
   */
  public String getCentraxxVersion() {
    return centraxxVersion;
  }

  /**
   * Set the Centraxx version.
   * @param centraxxVersion the centraxxVersion to set
   */
  public void setCentraxxVersion(String centraxxVersion) {
    this.centraxxVersion = centraxxVersion;
  }

  /**
   * Get the current session count.
   * @return the currentSessionCount
   */
  public String getCurrentSessionCount() {
    return currentSessionCount;
  }

  /**
   * Set the current session count.
   * @param currentSessionCount the currentSessionCount to set
   */
  public void setCurrentSessionCount(String currentSessionCount) {
    this.currentSessionCount = currentSessionCount;
  }

  /**
   * Get the status.
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the status.
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

}
