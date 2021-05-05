package org.aroundthecode.pathfinder.server.crawler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.*;
import org.aroundthecode.pathfinder.server.crawler.handler.JsonResponseHandler;
import org.json.simple.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

public class CrawlerWrapper {

	private static String mvnHome = "/opt/apache-maven";
	private static final Invoker invoker = new DefaultInvoker();
	private static File filePom = null;
	private static final Logger log = LogManager.getLogger(CrawlerWrapper.class.getName());

	
	static{
		String mHome = System.getenv("M2_HOME");
		if(mHome==null){
			mHome = getMvnHome();
			log.warn("No M2_HOME set, using [{}]",new Throwable(mHome) );
		}

		invoker.setMavenHome( new File(mHome) );

		URL pomFile = CrawlerWrapper.class.getClassLoader().getResource("embedder/pom.xml");
		log.info("Dummy pom file [{}]", new Throwable(pomFile.getFile()) );
		try {
			filePom = new File(pomFile.toURI());
		} catch(URISyntaxException e) {
			filePom = new File(pomFile.getPath());
		}

	}

	public static JSONObject crawl(String groupId,String artifactId,String type,String classifier, String version){

		JsonResponseHandler jHandler = new JsonResponseHandler();
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile( filePom );
		request.setGoals(  Collections.singletonList( "org.aroundthecode.pathfinder:pathfinder-maven-plugin:0.1.0-SNAPSHOT:crawler" ) );
		request.setOutputHandler(jHandler);

		Properties params = new Properties();
		params.setProperty("crawler.groupId", 		groupId);
		params.setProperty("crawler.artifactId", 	artifactId);
		params.setProperty("crawler.type", 			type);
		params.setProperty("crawler.classifier", 	classifier);
		params.setProperty("crawler.version", 		version);

		request.setProperties(params);
		log.info("Request [{}]", new Throwable(String.valueOf(params)) );
		InvocationResult result;
		try {
			result = invoker.execute( request );
			log.info("Completed!");
			jHandler.setReturnStatus( result.getExitCode() );
			jHandler.setException( result.getExecutionException() );
		} 
		catch (Exception e) {
			jHandler.setReturnStatus( -1 );
			jHandler.setException( e );
		}

		return jHandler.getJson();

	}

	public static String getMvnHome() {
		return mvnHome;
	}

    public static Invoker getInvoker() {
        return invoker;
    }

    public static File getFilePom() {
        return filePom;
    }

	public void setMvnHome(String mvnHome) {
		CrawlerWrapper.mvnHome = mvnHome;
	}


}
