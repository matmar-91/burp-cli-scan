package burp;

import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class Config {

	public static final String URL = "url";
	public static final String ACTIVE = "active";
	public static final String PASSIVE = "passive";
	public static final String NO_SPIDER = "no-spider";
	public static final String DO_NOT_SHUTDOWN = "do-not-shutdown";
	public static final String FORMAT = "format";
	public static final String REPORT = "report";
	
	private Options opts;
	private HelpFormatter formatter = new HelpFormatter();
	
	/**
	 * Stores options provided as command line arguments
	 */
	private CommandLine cmd;
	
	/**
	 * Creates new config based on commandLineArguments
	 * @param commandLineArguments
	 * @throws ParseException
	 */
	public Config(String[] commandLineArguments) throws ParseException {
		opts = createOptions();
		cmd = doParseArgs(opts, commandLineArguments);
	}

	/**
	 * Returns true if requested option was set and false otherwise.
	 * @param option
	 * @return
	 */
	public boolean hasOption(String option) {
		return cmd.hasOption(option);
	}
	
	/**
	 * Returns value of requested option.
	 * @param option
	 * @return
	 */
	public Optional<String> getOption(String option) {
		return Optional.ofNullable(cmd.getOptionValue(option));
	}

	/**
	 * Prints help message.
	 */
	public void printHelp() {
		formatter.printHelp("java -jar <file>", opts);
	}

	/**
	 * Parsing arguments using predefined options.
	 * @param opts
	 * @param commandLineArguments
	 * @return
	 * @throws ParseException
	 */
	private CommandLine doParseArgs(Options opts, String[] commandLineArguments) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		return parser.parse(opts, commandLineArguments);
	}

	/**
	 * Creates command line options.
	 * @return
	 */
	private Options createOptions() {
		Options opts = new Options();
		opts.addOption("u", URL, true, "Starting point for spider and / or scanning.");
		opts.addOption("a", ACTIVE, false, "Run active scan against target. Take precedence over passive.");
		opts.addOption("p", PASSIVE, false, "Run passive scan against target. This is default.");
		opts.addOption("s", NO_SPIDER, false, "Disables spidering so only scan for provided URL will be made.");
		opts.addOption("k", DO_NOT_SHUTDOWN, false, "Disables shutting down at the end of execution");
		opts.addOption("f", FORMAT, true, "Format of report. Available values are HTML and XML.");
		opts.addOption("r", REPORT, true, "Path where report should be stored.");
		return opts;
	}
	
}
