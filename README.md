# burp-cli-scan
BurpSuite Extension for performing scan via CLI.

This little extension gives you ability to perform active or passive scan of provided URL. 
It can spider host prior scan, or check only the provided link. You can get report in two formats
supported by burp suite: HTML or XML. 

### Build
In order to build this extension you need to have java and maven installed. Then just clone the repo and run `mvn clean compile assembly:single`. After running this successfully `./target` folder should appear with `burp-cli-scan.jar` file within.

### Usage
In order to run this extension you should first run Burp Suite manually and add it in the extender tab.
From now on you can shutdown burp and run it from terminal like this:

`java -jar burp.jar --url http://urltoscan.com/`

In order to run burp in headless mode you can run: 

`java -Djava.awt.headless=true -jar burp.jar --url http://urltoscan.com/`

Complete list of options:

* `-u --url <url>` Starting point for spider and / or scanning. If not provided this extension abort execution, so burp running just like usual.
* `-a --active` Run active scan against target. Take precedence over passive.
* `-p --passive` Run passive scan against target. This is default.
* `-s --no-spider` Disables spidering so only scan for provided URL will be performed.
* `-k --do-not-shutdown` Disables shutting down at the end of execution.
* `-f --format <HTML|XML>` Format of report. Available values are HTML and XML.
* `-r --report <filename>` Path where report should be stored. By default report will be stored in current directory as `<timestamp>_burp_report.<html|xml>`
