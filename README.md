## 更细日志-by HiJack
2017.7.19 

 	 1.自动识别网页编码
  
 	 2.添加httpRequestHeaders,以通过部分网站访问过滤	 
	 
------------------------------------------------------------------
## 原始说明
This is a bundle of utilities , that can be used for Java Network calls.

This bundle includes 
	
	* Get the html source of any url
	* Get the re directed links from the url
	* Get the absolute path
	* Canonicalize the url
	
	
Sample Usage

	HtmlFetcher htmlFetcher = new HtmlFetcher();
	String html = htmlFetcher.getHtml("url",socketTimeOut);
	
	By default the socket time out parameter is 10000 , if you dont provide it.
	
	or in command prompt
	
	MAVEN_OPTS="-Xms256m -Xmx1026m" mvn exec:java -Dexec.mainClass=com.sree.textbytes.network.SampleFetcher
