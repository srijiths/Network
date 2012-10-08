package com.sree.textbytes.network;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
/**
 * Canonicalize the url and escape illegal special characters in a URL
 * @author sree
 *
 */
public class CanonicalizeURL {
	public static String escapeIllegalURLCharacters(String url) throws Exception{
		String decodeUrl = URLDecoder.decode(url,"UTF-8");
		URL urlString = new URL(decodeUrl);
	  URI uri = new URI(urlString.getProtocol(), urlString.getUserInfo(), urlString.getHost(), urlString.getPort(), urlString.getPath(), urlString.getQuery(), urlString.getRef());
		return uri.toString();
	}
	
	}
