package hudson.plugins.karma;

import java.io.Serializable;

/**
 * Holds the configuration details for {@link hudson.model.HealthReport} generation
 *
 * @author Stephen Connolly
 * @since 1.7
 */
public class KarmaHealthReportThresholds implements Serializable {
    private int minLine;
    private int maxLine;
    private int minStatement;
    private int maxStatement;
    private int minFunction;
    private int maxFunction;
    private int minBranch;
    private int maxBranch;

    public KarmaHealthReportThresholds() {
    }

    public KarmaHealthReportThresholds(int minLine, int maxLine, int minStatement, int maxStatement, int minFunction, int maxFunction, int minBranch, int maxBranch) {
        this.minLine = minLine;
        this.maxLine = maxLine;
        this.minStatement = minStatement;
        this.maxStatement = maxStatement;
        this.minFunction = minFunction;
        this.maxFunction = maxFunction;
        this.minBranch = minBranch;
        this.maxBranch = maxBranch;
        ensureValid();
    }

    private int applyRange(int min , int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public void ensureValid() {
        maxLine = applyRange(0, maxLine, 100);
        minLine = applyRange(0, minLine, maxLine);
        maxStatement = applyRange(0, maxStatement, 100);
        minStatement = applyRange(0, minStatement, maxStatement);
        maxFunction = applyRange(0, maxFunction, 100);
        minFunction = applyRange(0, minFunction, maxFunction);
        maxBranch = applyRange(0, maxBranch, 100);
        minBranch = applyRange(0, minBranch, maxBranch);
    }

    public int getMinLine() {
        return minLine;
    }

    public void setMinLine(int minLine) {
        this.minLine = minLine;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public int getMinStatement() {
        return minStatement;
    }

    public void setMinStatement(int minStatement) {
        this.minStatement = minStatement;
    }

    public int getMaxStatement() {
        return maxStatement;
    }

    public void setMaxStatement(int maxStatement) {
        this.maxStatement = maxStatement;
    }

    public int getMinFunction() {
        return minFunction;
    }

    public void setMinFunction(int minFunction) {
        this.minFunction = minFunction;
    }

    public int getMaxFunction() {
        return maxFunction;
    }

    public void setMaxFunction(int maxFunction) {
        this.maxFunction = maxFunction;
    }

    public int getMinBranch() {
        return minBranch;
    }

    public void setMinBranch(int minBranch) {
        this.minBranch = minBranch;
    }

    public int getMaxBranch() {
        return maxBranch;
    }

    public void setMaxBranch(int maxBranch) {
        this.maxBranch = maxBranch;
    }
}
