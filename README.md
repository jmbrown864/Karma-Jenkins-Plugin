# Karma-Jenkins-Plugin
Jenkins CI plugin to generate code coverage trend graphs based on Karma HTML coverage reports.
Only reads output of PhantomJS coverage.

##To Use:
Add post-build action: Record Karma coverage report
Set 'File containing the Karma HTML report' to be the path to index.html for PhantomJS generated by Karma-coverage
Save Jenkins configuration and build
