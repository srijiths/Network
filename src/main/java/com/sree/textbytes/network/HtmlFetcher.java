package com.sree.textbytes.network;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * User: sree
 */

public class HtmlFetcher {
	private static final Logger logger = Logger.getLogger(HtmlFetcher.class
			.getName());

	static int connectionTimeOut = 100000;
	static int socketTimeOut = 100000;
	private static Header[] headers;
	private static int statusCode = 0;
	private static String modTime = null;
	boolean absSet = false;
	/**
	 * holds the HttpClient object for making requests
	 */
	private static HttpClient httpClient;
	public String htmlContent = null;

	public static HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * makes an http fetch to go retreive the HTML from a url, store it to disk
	 * and pass it off
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */

	public String getLastModifiedDate() {
		if (headers.length > 0) {
			return headers[0].getValue();
		} else {
			logger.info("header is zero ");
		}
		return null;
	}

	public static int getStatus() {
		return statusCode;
	}

	public void setModTime(String modifiedTime) {
		modTime = modifiedTime;
	}

	public String getHtml(String url, int sokTimeout) throws Exception {
		logger.info("url  " + url);
		HttpResponse response = null;
		initClient(sokTimeout);
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpget = null;
		String htmlResult = null;
		HttpEntity entity = null;
		try {
			url = CanonicalizeURL.escapeIllegalURLCharacters(url);
			httpget = new HttpGet(url);

			if (modTime != null) {
				httpget.setHeader("If-Modified-Since", modTime);
			}

			response = httpClient.execute(httpget, localContext);
			headers = response.getHeaders("Last-Modified");
			statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 200 || statusCode == 201 || statusCode == 202) {
				logger.debug("HTTP status code  " + statusCode
						+ " OK,Proceeding... ");
				htmlResult = getHtmlSource(response);

			} else if (statusCode == 304) {

				logger.warn("HTTP Status Code is " + statusCode
						+ " link is already uptodate ");
				return null;
			} else if (statusCode == 503) {
				logger.warn("HTTP Status Code NOT OK " + statusCode
						+ " Skipping the url... ");
				throw new Exception(" service  unavailable status code is "
						+ statusCode + " Skipping the url... ");

			} else {
				logger.warn("HTTP Status Code NOT OK " + statusCode
						+ " Skipping the url... ");
				throw new Exception("status code is " + statusCode);

			}

		} catch (NullPointerException e) {
			logger.warn(e.toString() + " " + e.getMessage() + "for url " + url);
			throw e;

		} catch (Exception e) {
			logger.error("GRVBIGFAIL: " + url + " Reached max bytes size");
			throw e;

		} finally {
			closeConnection(entity);
			closeHttpRequest(httpget);
			if (modTime != null)
				modTime = null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("starting...");
		}
		if (htmlResult == null || htmlResult.length() < 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("HTMLRESULT is empty or null");
			}
			// throw new NotHtmlException();
		}

		return htmlResult;
	}

	static void closeConnection(HttpEntity entity) throws IOException {
		if (entity != null)
			EntityUtils.consume(entity);

	}

	static void closeInptStream(InputStream instream) throws Exception {
		if (instream != null) {
			try {
				instream.close();
			} catch (Exception e) {
				logger.warn(e.getMessage());
				throw e;
			}
		}
	}

	static void closeHttpRequest(HttpGet httpGet) throws Exception {
		if (httpGet != null) {
			try {
				httpGet.abort();

			} catch (Exception e) {
				throw e;
			}
		}

	}

	public static String getHtmlSource(HttpResponse response) throws Exception {

		String htmlResult = null;
		HttpEntity entity = null;
		InputStream instream = null;
		entity = response.getEntity();
		if (entity != null) {
			instream = entity.getContent();
			String encodingType = "UTF-8";
			try {
				encodingType = EntityUtils.getContentCharSet(entity);
				if (encodingType == null) {
					encodingType = "UTF-8";
				}

			} catch (Exception e) {

				if (logger.isDebugEnabled()) {
					logger.debug("Unable to get charset for: ");
					logger.debug("Encoding Type is: " + encodingType);
				}
				throw e;
			}

			try {
				htmlResult = HtmlFetcher.convertStreamToString(instream,
						15728640, encodingType);
				if (htmlResult != null) {
					htmlResult = htmlResult.trim();
				}
			} finally {
				closeConnection(entity);
				closeInptStream(instream);

			}

		} else {
			logger.error("Unable to fetch URL Properly: ");
		}

		return htmlResult;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public String getAbsoluteUrl(String url, int socTimeOut) throws Exception {

		logger.info("url for getting final link " + url);
		String redirectedUrl = null;
		String htmlContent = null;
		initClient(socTimeOut);
		HttpGet httpget = null;
		HttpResponse response = null;
		int statusCode = 0;
		try {
			HTTPRedirect httpRedirect = new HTTPRedirect();
			HttpContext localContext = new BasicHttpContext();
			url = CanonicalizeURL.escapeIllegalURLCharacters(url);
			httpget = new HttpGet(url);
			response = httpClient.execute(httpget, localContext);
			HttpUriRequest currentReq = (HttpUriRequest) localContext
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) localContext
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			redirectedUrl = currentHost.toURI() + currentReq.getURI();
			statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == 404) {

				redirectedUrl = null;
				throw new Exception("status Code " + statusCode);

			} else if (statusCode >= 200 && statusCode <= 202) {

				htmlContent = getHtmlSource(response);
				if (htmlContent != null) {
					// check whether meta tag is present or not
					String finalUrl = httpRedirect.doGet(redirectedUrl,
							htmlContent);
					if (finalUrl != null) {
						// try to get the status OK for url
						redirectedUrl = finalUrl;
						getAbsoluteUrl(finalUrl, socTimeOut);
					} else {
						this.htmlContent = htmlContent;
					}
				}
				if (statusCode == 404) {
					redirectedUrl = null;
					throw new Exception("status Code " + statusCode);

				}

				return redirectedUrl;
			} else if (statusCode == 303 || statusCode == 301
					|| statusCode == 302) {

				getAbsoluteUrl(redirectedUrl, socTimeOut);

			} else if (statusCode == 503) {

				throw new Exception("status Code " + statusCode);

			} else {
				throw new Exception("status Code " + statusCode);
			}

		} catch (Exception e) {

			/*
			 * closeConnection(response.getEntity()); closeHttpRequest(httpget);
			 */
			throw e;
		}

		finally {
			if (response != null)
				closeConnection(response.getEntity());
			closeHttpRequest(httpget);
			logger.info("status code is  " + statusCode);
		}

		return redirectedUrl;

	}

	/**
	 * Initialize HttpClient
	 * 
	 * */

	private void initClient(int sokTimeout) {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing HttpClient");
		}
		HttpParams httpParams = new BasicHttpParams();

		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

		// set request params
		httpParams.setParameter("http.language.Accept-Language", "en-us");
		httpParams.setParameter("http.protocol.content-charset", "UTF-8");
		httpParams
				.setParameter(
						"Accept",
						"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		httpParams.setParameter("http.connection.stalecheck", false); // turn
																		// off
																		// stale
																		// check
																		// checking
																		// for
																		// performance
																		// reasons

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
				.getSocketFactory()));

		final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				schemeRegistry);
		httpClient = new DefaultHttpClient(cm, httpParams);

		if (sokTimeout > 0) {
			logger.debug("Socket time out property found : " + sokTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, sokTimeout);

		} else {
			logger.warn("Socket time out property not found , using default"
					+ socketTimeOut);
			HttpConnectionParams.setSoTimeout(httpParams, socketTimeOut);

		}

		httpClient.getParams().setParameter("http.connection-manager.timeout",
				1000000L);
		httpClient.getParams().setParameter("http.protocol.wait-for-continue",
				1000000L);
		httpClient.getParams().setParameter("http.tcp.nodelay", true);

	}

	/**
	 * reads bytes off the string and returns a string
	 * 
	 * @param is
	 * @param maxBytes
	 *            The max bytes that we want to read from the input stream
	 * @return String
	 * @throws Exception
	 */

	public static String convertStreamToString(InputStream is, int maxBytes,
			String encodingType) throws Exception {

		char[] buf = new char[2048];
		Reader r = null;
		StringBuilder s = new StringBuilder();
		try {
			r = new InputStreamReader(is, encodingType);

			int bytesRead = 2048;
			while (true) {
				if (bytesRead >= maxBytes) {
					throw new Exception();
				}

				int n = r.read(buf);
				bytesRead += 2048;
				if (n < 0)
					break;

				s.append(buf, 0, n);
			}

			// return s.toString();

		} catch (Exception e) {
			logger.warn(e.toString() + " " + e.getMessage());
			if (r != null) {
				try {
					r.close();
				} catch (Exception e1) {
					throw e1;
				}
			}
			throw e;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
					throw e;
				}
			}
		}
		if (s.length() == 0)
			return null;
		else
			return s.toString();

	}
}