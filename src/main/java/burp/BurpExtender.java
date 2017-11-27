package burp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.ParseException;

public class BurpExtender implements IBurpExtender, IHttpListener {

	private static final long TIMEOUT = 10;
	private static final String FORMAT_HTML = "HTML";
	private static final String FORMAT_XML = "XML";

	private IBurpExtenderCallbacks callbacks;
	private IExtensionHelpers helpers;
	
	private Config config;
	private ScannerListenerImpl scannerListener = new ScannerListenerImpl();
	private Temporal lastActivityTime = Instant.now();

	/**
	 * {@inheritDoc}
	 */
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		callbacks.setExtensionName("Burp CLI");
		helpers = callbacks.getHelpers();
		createConfig(); 
		
		// if user doesn't provide url, for example if this is just regular BurpSuite start, abort further execution 
		Optional<String> urlOpt = config.getOption(Config.URL); 
		if(!urlOpt.isPresent()) {
			return;
		}

		scan(urlOpt.get());
		
		while(Duration.between(lastActivityTime, Instant.now()).getSeconds() < TIMEOUT) {
			pause(500);
		}
		
		report();
		
		if(!config.hasOption(Config.DO_NOT_SHUTDOWN)) {
			callbacks.exitSuite(false);
		}
	}

	/**
	 * Pause thread for specified amount of time.
	 * @param timeout in millis
	 */
	private void pause(long timeout) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} catch (InterruptedException e) {}
	}

	/**
	 * Writes report to a file.
	 */
	private void report() {
		System.out.println("Generating report...");
		IScanIssue[] issues = scannerListener.getIssues();
		String format = config.getOption(Config.FORMAT).orElse("");
		format = format.toUpperCase();
		
		if(!format.equals(FORMAT_HTML) && !format.equals(FORMAT_XML)) {
			format = FORMAT_HTML;
		}
		
		// default filename
		String filename = String.format("%d_burp_report.%s", System.currentTimeMillis(), format.toLowerCase());
		filename = config.getOption(Config.REPORT).orElse(filename);
		
		callbacks.generateScanReport(format, issues, new File(filename));
	}

	/**
	 * Creates config instance.
	 */
	private void createConfig() {
		try {
			config = new Config(callbacks.getCommandLineArguments());
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			config.printHelp();
			callbacks.exitSuite(false);
		}
	}
	
	/**
	 * Choose appropriate scanning method and starts it.
	 * @param url
	 */
	private void scan(String url) {
		try {
			URL startURL = new URL(url);
			
			// if no spider just scan provided url
			callbacks.registerScannerListener(scannerListener);
			if(config.hasOption(Config.NO_SPIDER)) {
				doScan(startURL);
			} else {
				callbacks.registerHttpListener(this);
				doSpider(startURL);
			}
		} catch (MalformedURLException e) {
			System.err.println("Provided Url is invalid.");
			System.err.println(e.getMessage());
			callbacks.exitSuite(false);
		}
	}

	/**
	 * Make passive or active scan based on {@link #scanType}.
	 * @param url
	 */
	private void doScan(URL url) {
		System.out.println("Scanning URL: " + url);
		String host = url.getHost();
		int port = url.getPort() > 0 ? url.getPort() : 80;
		String protocol = url.getProtocol();
		byte[] request = helpers.buildHttpRequest(url);
		doScan(host, port, protocol, request, null);
	}
	
	/**
	 * Make passive or active scan based on {@link #scanType}
	 * @param host
	 * @param port
	 * @param protocol
	 * @param request
	 * @param response
	 */
	private void doScan(String host, int port, String protocol, byte[] request, byte[] response) {
		boolean isHttps = protocol.equalsIgnoreCase("https");
		if(config.hasOption(Config.ACTIVE)) {
			doActiveScan(host, port, isHttps, request);
		} else {
			doPassiveScan(host, port, isHttps, request, response);
		}
	}
	
	/**
	 * Performs active scan
	 * @param host
	 * @param port
	 * @param isHttps
	 * @param request
	 */
	private void doActiveScan(String host, int port, boolean isHttps, byte[] request) {
		callbacks.doActiveScan(host, port, isHttps, request);
	}
	
	/**
	 * Performs passive scan
	 * @param host
	 * @param port
	 * @param isHttps
	 * @param request
	 * @param response
	 */
	private void doPassiveScan(String host, int port, boolean isHttps, byte[] request, byte[] response) {
		if(response == null) {
			response = callbacks.makeHttpRequest(host, port, isHttps, request);
		}
		callbacks.doPassiveScan(host, port, isHttps, request, response);
	}
	
	/**
	 * Spider site using given url as a starting point.
	 * @param url
	 */
	private void doSpider(URL url) {
		if(!callbacks.isInScope(url)) {
			callbacks.includeInScope(getBaseUrl(url));
		}
		callbacks.sendToSpider(url);
	}

	/**
	 * Strip path out of url, so for example for given URL:
	 * http://example.com/this/is/the/example
	 * it will return 
	 * http://example.com/
	 * 
	 * @param url
	 * @return
	 */
	private URL getBaseUrl(URL url){
		try {
			return new URL(url, "/");
		} catch (MalformedURLException e) {
			// will not happen
		}
		return url;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation sends urls found by spider to scanner.
	 * </p>
	 */
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		lastActivityTime = Instant.now();
		if(toolFlag == IBurpExtenderCallbacks.TOOL_SPIDER && !messageIsRequest) {
			String host = messageInfo.getHttpService().getHost();
			int port = messageInfo.getHttpService().getPort();
			String protocol = messageInfo.getHttpService().getProtocol();
			doScan(host, port, protocol, messageInfo.getRequest(), messageInfo.getResponse());
		}
	}

}
