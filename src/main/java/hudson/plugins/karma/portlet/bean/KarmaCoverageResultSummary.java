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
package hudson.plugins.karma.portlet.bean;

import hudson.model.Job;
import hudson.plugins.karma.portlet.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary of the Karma Coverage result.
 */
public class KarmaCoverageResultSummary {

  /**
   * The related job.
   */
  private Job job;

  /**
   * Line coverage percentage.
   */
  private float lineCoverage;

  /**
   * Statement coverage percentage.
   */
  private float statementCoverage;

  /**
   * Function coverage percentage.
   */
  private float functionCoverage;

  /**
   * Branch coverage percentage.
   */
  private float branchCoverage;

  private List<KarmaCoverageResultSummary> coverageResults = new ArrayList<KarmaCoverageResultSummary>();

  /**
   * Default Constructor.
   */
  public KarmaCoverageResultSummary() {
  }

  /**
   * Constructor with parameters.
   *
   * @param job
   *          the related Job
   * @param lineCoverage
   *          line coverage percentage
   * @param statementCoverage
   *          statement coverage percentage
   * @param functionCoverage
   *          function coverage percentage
   * @param branchCoverage
   *          branch coverage percentage
   */
  public KarmaCoverageResultSummary(Job job, float lineCoverage, float statementCoverage, float functionCoverage,
    float branchCoverage) {
    this.job = job;
    this.lineCoverage = lineCoverage;
    this.statementCoverage = statementCoverage;
    this.functionCoverage = functionCoverage;
    this.branchCoverage = branchCoverage;
  }

  /**
   * Add a coverage result.
   *
   * @param coverageResult
   *          a coverage result
   * @return KarmaCoverageResultSummary summary of the Karma coverage
   *         result
   */
  public KarmaCoverageResultSummary addCoverageResult(KarmaCoverageResultSummary coverageResult) {

    this.setLineCoverage(this.getLineCoverage() + coverageResult.getLineCoverage());
    this.setStatementCoverage(this.getStatementCoverage() + coverageResult.getStatementCoverage());
    this.setFunctionCoverage(this.getFunctionCoverage() + coverageResult.getFunctionCoverage());
    this.setBranchCoverage(this.getBranchCoverage() + coverageResult.getBranchCoverage());

    getCoverageResults().add(coverageResult);

    return this;
  }

  /**
   * Get list of KarmaCoverageResult objects.
   *
   * @return List a List of KarmaCoverageResult objects
   */
  public List<KarmaCoverageResultSummary> getKarmaCoverageResults() {
    return this.getCoverageResults();
  }

  /**
   * Getter of the total of branch coverage.
   *
   * @return float the total of branch coverage.
   */
  public float getTotalBranchCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalBranch = this.getBranchCoverage() / this.getCoverageResults().size();
      totalBranch = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalBranch);
      return totalBranch;
    }
  }

  /**
   * Getter of the total of statement coverage.
   *
   * @return float the total of statement coverage.
   */
  public float getTotalStatementCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalStatement = this.getStatementCoverage() / this.getCoverageResults().size();
      totalStatement = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalStatement);
      return totalStatement;
    }
  }

  /**
   * Getter of the total of function coverage.
   *
   * @return float the total of function coverage.
   */
  public float getTotalFunctionCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalFunction = this.getFunctionCoverage() / this.getCoverageResults().size();
      totalFunction = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalFunction);
      return totalFunction;
    }
  }

  /**
   * Getter of the total of line coverage.
   *
   * @return float the total of line coverage.
   */
  public float getTotalLineCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalLine = this.getLineCoverage() / this.getCoverageResults().size();
      totalLine = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalLine);
      return totalLine;
    }
  }

  /**
   * @return Job a job
   */
  public Job getJob() {
    return job;
  }

  /**
   * @return the lineCoverage
   */
  public float getLineCoverage() {
    return lineCoverage;
  }

  /**
   * @return the statementCoverage
   */
  public float getStatementCoverage() {
    return statementCoverage;
  }

  /**
   * @return the functionCoverage
   */
  public float getFunctionCoverage() {
    return functionCoverage;
  }

  /**
   * @return the branchCoverage
   */
  public float getBranchCoverage() {
    return branchCoverage;
  }

  /**
   * @param job
   *          the job to set
   */
  public void setJob(Job job) {
    this.job = job;
  }

  /**
   * @param lineCoverage
   *          the lineCoverage to set
   */
  public void setLineCoverage(float lineCoverage) {
    this.lineCoverage = lineCoverage;
  }

  /**
   * @param statementCoverage
   *          the statementCoverage to set
   */
  public void setStatementCoverage(float statementCoverage) {
    this.statementCoverage = statementCoverage;
  }

  /**
   * @param functionCoverage
   *          the functionCoverage to set
   */
  public void setFunctionCoverage(float functionCoverage) {
    this.functionCoverage = functionCoverage;
  }

  /**
   * @param branchCoverage
   *          the branchCoverage to set
   */
  public void setBranchCoverage(float branchCoverage) {
    this.branchCoverage = branchCoverage;
  }

  /**
   * @return a list of coverage results
   */
  public List<KarmaCoverageResultSummary> getCoverageResults() {
    return coverageResults;
  }

  /**
   * @param coverageResults 
   *          the list of coverage results to set
   */
  public void setCoverageResults(List<KarmaCoverageResultSummary> coverageResults) {
    this.coverageResults = coverageResults;
  }
}
