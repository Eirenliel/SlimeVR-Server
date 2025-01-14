package dev.slimevr.ios;

import dev.slimevr.VRServer;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.apple.uniformtypeid.UTType;
import org.robovm.apple.webkit.*;

import java.util.List;


public class WebviewController extends UIViewController {
	private WKWebView webView;

	public WebviewController() {
		UIView view = getView();

		view.setBackgroundColor(UIColor.purple());
	}

	@Override
	public void loadView() {
		super.loadView();
		UIView view = getView();

		if (!VRServer.Companion.getInstanceInitialized()) {
			Main.runServer();
		}

		var config = new WKWebViewConfiguration();
		config.setURLSchemeHandler(new WKURLSchemeHandler() {
			@Override
			public void startURLSchemeTask(WKWebView webView, WKURLSchemeTask urlSchemeTask) {
				var url = urlSchemeTask.getRequest().getURL();
				var fileUrl = fileUrlFromUrl(url);
				if (fileUrl == null)
					return;
				var mimeType = mimeType(fileUrl);
				var data = NSData.read(fileUrl);
				if (data == null)
					return;

				var response = new NSHTTPURLResponse(url, mimeType, data.getLength(), null);

				urlSchemeTask.didReceiveResponse(response);
				urlSchemeTask.didReceiveData(data);
				urlSchemeTask.didFinish();
			}

			@Override
			public void stopURLSchemeTask(WKWebView webView, WKURLSchemeTask urlSchemeTask) {

			}

			private NSURL fileUrlFromUrl(NSURL url) {
				List<String> paths = url.getPathComponents();
				if (paths.size() == 1) {
					return NSBundle.getMainBundle().findResourceURL("index.html", "", "dist");
				}
				String last = paths.remove(paths.size() - 1);
				paths.remove(0); // Remove "/"
				StringBuilder joining = new StringBuilder();
				for (String path : paths) {
					joining.append("/").append(path);
				}
				return NSBundle.getMainBundle().findResourceURL(last, "", "dist" + joining);
			}

			private String mimeType(NSURL url) {
				var type = UTType.createUsingFilenameExtension(url.getPathExtension());
				if (type == null)
					return null;
				return type.getPreferredMIMEType();
			}
		}, "slimevr");
		webView = new WKWebView(view.getFrame(), config);
		if (webView != null) {
			view.addSubview(webView);
		}
	}

	@Override
	public void viewDidLoad() {
		super.viewDidLoad();

		var req = new NSURLRequest(new NSURL("slimevr:///"));
		webView.loadRequest(req);
	}
}
