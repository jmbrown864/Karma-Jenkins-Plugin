/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following branchs:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

/**
 * @author Allyn Pierre (Allyn.GreyDeAlmeidaLimaPierre@sonyericsson.com)
 * @author Eduardo Palazzo (Eduardo.Palazzo@sonyericsson.com)
 * @author Mauro Durante (Mauro.DuranteJunior@sonyericsson.com)
 */
package hudson.plugins.karma.portlet;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.karma.KarmaBuildAction;
import hudson.plugins.karma.portlet.bean.KarmaCoverageResultSummary;
import hudson.plugins.karma.portlet.utils.Utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.LocalDate;

/**
 * Load data of Karma coverage results used by chart or grid.
 */
public final class KarmaLoadData {

  /**
   * Private constructor avoiding this class to be used in a non-static way.
   */
  private KarmaLoadData() {
  }

  /**
   * Get Karma coverage results of all jobs and store into a sorted
   * HashMap by date.
   *
   * @param jobs
   *        jobs of Dashboard view
   * @param daysNumber
   *          number of days
   * @return Map The sorted summaries
   */
  public static Map<LocalDate, KarmaCoverageResultSummary> loadChartDataWithinRange(List<Job> jobs, int daysNumber) {

    Map<LocalDate, KarmaCoverageResultSummary> summaries = new HashMap<LocalDate, KarmaCoverageResultSummary>();

    // Get the last build (last date) of the all jobs
    LocalDate lastDate = Utils.getLastDate(jobs);

    // No builds
    if (lastDate == null) {
      return null;
    }

    // Get the first date from last build date minus number of days
    LocalDate firstDate = lastDate.minusDays(daysNumber);

    // For each job, get Karma coverage results according with
    // date range (last build date minus number of days)
    for (Job job : jobs) {

      Run run = job.getLastBuild();

      if (null != run) {
        LocalDate runDate = new LocalDate(run.getTimestamp());

        while (runDate.isAfter(firstDate)) {

          summarize(summaries, run, runDate, job);

          run = run.getPreviousBuild();

          if (null == run) {
            break;
          }

          runDate = new LocalDate(run.getTimestamp());

        }
      }
    }

    // Sorting by date, ascending order
    Map<LocalDate, KarmaCoverageResultSummary> sortedSummaries = new TreeMap(summaries);

    return sortedSummaries;

  }

  /**
   * Summarize Karma converage results.
   *
   * @param summaries
   *          a Map of KarmaCoverageResultSummary objects indexed by
   *          dates
   * @param run
   *          the build which will provide information about the
   *          coverage result
   * @param runDate
   *          the date on which the build was performed
   * @param job
   *          job from the DashBoard Portlet view
   */
  private static void summarize(Map<LocalDate, KarmaCoverageResultSummary> summaries, Run run, LocalDate runDate, Job job) {

    KarmaCoverageResultSummary karmaCoverageResult = getResult(run);

    // Retrieve Karma information for informed date
    KarmaCoverageResultSummary karmaCoverageResultSummary = summaries.get(runDate);

    // Consider the last result of each
    // job date (if there are many builds for the same date). If not
    // exists, the Karma coverage data must be added. If exists
    // Karma coverage data for the same date but it belongs to other
    // job, sum the values.
    if (karmaCoverageResultSummary == null) {
      karmaCoverageResultSummary = new KarmaCoverageResultSummary();
      karmaCoverageResultSummary.addCoverageResult(karmaCoverageResult);
      karmaCoverageResultSummary.setJob(job);
    } else {

      // Check if exists Karma data for same date and job
      List<KarmaCoverageResultSummary> listResults = karmaCoverageResultSummary.getKarmaCoverageResults();
      boolean found = false;

      for (KarmaCoverageResultSummary item : listResults) {
        if ((null != item.getJob()) && (null != item.getJob().getName()) && (null != job)) {
          if (item.getJob().getName().equals(job.getName())) {
            found = true;
            break;
          }
        }
      }

      if (!found) {
        karmaCoverageResultSummary.addCoverageResult(karmaCoverageResult);
        karmaCoverageResultSummary.setJob(job);
      }
    }

    summaries.put(runDate, karmaCoverageResultSummary);
  }

  /**
   * Get the Karma coverage result for a specific run.
   *
   * @param run
   *          a job execution
   * @return KarmaCoverageTestResult the coverage result
   */
  private static KarmaCoverageResultSummary getResult(Run run) {
    KarmaBuildAction karmaAction = run.getAction(KarmaBuildAction.class);

    float lineCoverage = 0.0f;
    float statementCoverage = 0.0f;
    float functionCoverage = 0.0f;
    float branchCoverage = 0.0f;

    if (karmaAction != null) {
      if (null != karmaAction.getLineCoverage()) {
        lineCoverage = karmaAction.getLineCoverage().getPercentageFloat();
      }
      if (null != karmaAction.getStatementCoverage()) {
        statementCoverage = karmaAction.getStatementCoverage().getPercentageFloat();
      }
      if (null != karmaAction.getFunctionCoverage()) {
        functionCoverage = karmaAction.getFunctionCoverage().getPercentageFloat();
      }
      if (null != karmaAction.getBranchCoverage()) {
        branchCoverage = karmaAction.getBranchCoverage().getPercentageFloat();
      }
    }
    return new KarmaCoverageResultSummary(run.getParent(), lineCoverage, statementCoverage, functionCoverage, branchCoverage);
  }

  /**
   * Summarize the last coverage results of all jobs. If a job doesn't
   * include any coverage, add zero.
   *
   * @param jobs
   *          a final Collection of Job objects
   * @return KarmaCoverageResultSummary the result summary
   */
  public static KarmaCoverageResultSummary getResultSummary(final Collection<Job> jobs) {
    KarmaCoverageResultSummary summary = new KarmaCoverageResultSummary();

    for (Job job : jobs) {

      float lineCoverage = 0.0f;
      float statementCoverage = 0.0f;
      float functionCoverage = 0.0f;
      float branchCoverage = 0.0f;

      Run run = job.getLastSuccessfulBuild();

      if (run != null) {

        KarmaBuildAction karmaAction = job.getLastSuccessfulBuild().getAction(KarmaBuildAction.class);

        if (null != karmaAction) {
          if (null != karmaAction.getLineCoverage()) {
            lineCoverage = karmaAction.getLineCoverage().getPercentageFloat();
            BigDecimal bigLineCoverage = new BigDecimal(lineCoverage);
            bigLineCoverage = bigLineCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            lineCoverage = bigLineCoverage.floatValue();
          }

          if (null != karmaAction.getStatementCoverage()) {
            statementCoverage = karmaAction.getStatementCoverage().getPercentageFloat();
            BigDecimal bigStatementCoverage = new BigDecimal(statementCoverage);
            bigStatementCoverage = bigStatementCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            statementCoverage = bigStatementCoverage.floatValue();
          }
          if (null != karmaAction.getFunctionCoverage()) {
            functionCoverage = karmaAction.getFunctionCoverage().getPercentageFloat();
            BigDecimal bigFunctionCoverage = new BigDecimal(functionCoverage);
            bigFunctionCoverage = bigFunctionCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            functionCoverage = bigFunctionCoverage.floatValue();
          }

          if (null != karmaAction.getBranchCoverage()) {
            branchCoverage = karmaAction.getBranchCoverage().getPercentageFloat();
            BigDecimal getBranchCoverage = new BigDecimal(branchCoverage);
            getBranchCoverage = getBranchCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            branchCoverage = getBranchCoverage.floatValue();
          }
        }
      }
      summary.addCoverageResult(new KarmaCoverageResultSummary(job, lineCoverage, statementCoverage, functionCoverage,
        branchCoverage));
    }
    return summary;
  }
}
