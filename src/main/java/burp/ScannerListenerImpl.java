package burp;

import java.util.ArrayList;
import java.util.List;

public class ScannerListenerImpl implements IScannerListener {

	List<IScanIssue> issues = new ArrayList<>();
	
	public void newScanIssue(IScanIssue issue) {
		issues.add(issue);
	}
	
	public IScanIssue[] getIssues() {
		return issues.toArray(new IScanIssue[0]);
	}

}
