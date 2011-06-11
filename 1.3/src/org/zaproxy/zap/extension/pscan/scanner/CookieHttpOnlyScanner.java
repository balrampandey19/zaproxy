package org.zaproxy.zap.extension.pscan.scanner;

import java.util.Vector;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScanner;


public class CookieHttpOnlyScanner implements PassiveScanner {

	private PassiveScanThread parent = null;
	//private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@SuppressWarnings("unchecked")
	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		Vector<String> cookies1 = msg.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE);

		if (cookies1 != null) {
			for (String cookie : cookies1) {
				if (cookie.toLowerCase().indexOf("httponly") < 0) {
					this.raiseAlert(msg, id, cookie);
				}
			}
		}

		Vector<String> cookies2 = msg.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE2);
		
		if (cookies2 != null) {
			for (String cookie : cookies2) {
				if (cookie.toLowerCase().indexOf("httponly") < 0) {
					this.raiseAlert(msg, id, cookie);
				}
			}
		}
	}
	
	private void raiseAlert(HttpMessage msg, int id, String cookie) {
	    Alert alert = new Alert(getId(), Alert.RISK_LOW, Alert.WARNING, 
		    	"Cookie set without HttpOnly flag");
		    	alert.setDetail(
		    		"A cookie has been set without the HttpOnly flag, which means that the cookie can be accessed by JavaScript. " +
		    		"If a malicious script can be run on this page then the cookie will be accessible and can be transmitted to another site. " +
		    		"If this is a session cookie then session hijacking may be possible.", 
		    		msg.getRequestHeader().getURI().toString(),
		    		cookie, "", 
		    		"Ensure that the HttpOnly flag is set for all cookies.", 
		            "www.owasp.org/index.php/HttpOnly", 
		            msg);
	
    	parent.raiseAlert(id, alert);

	}

	private int getId() {
		return 40000;
	}

	@Override
	public String getName() {
		return "Cookie no http-only flag";
	}

}
