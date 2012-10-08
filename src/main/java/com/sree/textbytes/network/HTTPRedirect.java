package com.sree.textbytes.network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Class to get the http redirected link
 * 
 * @author simi
 *
 */
public class HTTPRedirect {

	private static final Logger logger = Logger.getLogger(HTTPRedirect.class
			.getName());

	public String doGet(String url, String content) {
		String refreshUrl = null;
		String metaRefresh = null;
		logger.info("url for checking meta tag   " + url);
		try {
			String refreshRegex = "REFRESH|refresh";
			Document doc = Jsoup.parse(content);
			metaRefresh = doc.select("meta[HTTP-EQUIV~=" + refreshRegex + "]")
					.attr("content");
			if (metaRefresh != null && metaRefresh.length() > 0) {
				logger.info("Checking for meta equi tag " + metaRefresh);
				refreshUrl = findMatch("URL=(.*)", metaRefresh);
				logger.info("Refreshed url  " + refreshUrl);
				if (refreshUrl != null) {
					refreshUrl = buildUrl(url, refreshUrl);
				}

			}

		}

		catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
		logger.info("Redirected Url  " + refreshUrl);
		return refreshUrl;
	}

	public static String findMatch(final String regex, final String text) {
		final Pattern pattern = Pattern
				.compile(regex, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static String between(final String string, final String prefix,
			final String suffix) {
		return string.substring(prefix.length(),
				string.length() - suffix.length());
	}

	public static String buildUrl(final String url, final String path) {

		String urlPath = null;
		if (path.startsWith("http:/") | path.startsWith("https:/"))
			return path;

		if (url.endsWith("/")) {
			if (path.endsWith("/")) {
				urlPath = path.substring(1);
				return url + urlPath;
			} else {
				return url + path;
			}
		}
		String urlSplit[] = url.split("/");
		if (path.startsWith("/")) {
			return urlSplit[0] + "//" + urlSplit[2] + path;
		} else {
			return urlSplit[0] + "//" + urlSplit[2] + "/" + path;
		}
	}
}
