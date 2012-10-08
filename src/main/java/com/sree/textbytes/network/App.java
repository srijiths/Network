package com.sree.textbytes.network;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );
        
        HtmlFetcher htmlFetcher = new HtmlFetcher();
        String html = htmlFetcher.getHtml("http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=1&utm_medium=RSS&utm_source=RSS_Feed", 0);
        System.out.println("Html : "+html);
    }
}
