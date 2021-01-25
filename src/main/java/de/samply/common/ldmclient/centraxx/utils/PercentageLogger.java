package de.samply.common.ldmclient.centraxx.utils;

import com.cookingfox.guava_preconditions.Preconditions;
import org.slf4j.Logger;

public class PercentageLogger {

  private Logger logger;
  private int numberOfElements;
  private int counter = 0;
  private int lastPercentage = 0;

  /**
   * Calculates percentage of remaining steps and logs it.
   *
   * @param logger logger
   * @param numberOfElements number of elements
   * @param description description of the operation
   */
  public PercentageLogger(Logger logger, int numberOfElements, String description) {

    this.logger = logger;
    this.numberOfElements = numberOfElements;
    Preconditions.checkArgument(numberOfElements > 0, "numberOfElements is negative");
    logger.debug(description);

  }

  /**
   * Increments one step.
   */
  public void incrementCounter() {

      counter++;
      int percentage = 100 * counter / numberOfElements;

      if (lastPercentage != percentage) {

        lastPercentage = percentage;

        if (percentage % 10 == 0) {
          logger.debug(percentage + " %");
        }
      }

  }

}
