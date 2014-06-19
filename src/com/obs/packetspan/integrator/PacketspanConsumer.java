package com.obs.packetspan.integrator;

import java.io.IOException;
import java.util.Queue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class PacketspanConsumer implements Runnable {

	private static Queue<ProcessRequestData> queue;
	private static PropertiesConfiguration prop;
	private static HttpPost post;
	private static byte[] encoded;
	private static String tenantIdentifier;
	private static HttpClient httpClient;
	static Logger logger = Logger.getLogger("");
	private ProcessCommandImpl processCommand;
	public static int wait;

	public static HttpClient wrapClient(HttpClient base) {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public PacketspanConsumer(Queue<ProcessRequestData> queue1,
			PropertiesConfiguration prop2) {
		try {
			this.queue = queue1;
			this.prop=prop2;
			httpClient = new DefaultHttpClient();
			httpClient = wrapClient(httpClient);
			String username = prop.getString("username");
			String password = prop.getString("password");
			tenantIdentifier = prop.getString("tenantIdentfier");
			String encodedpassword = username.trim() + ":" + password.trim();
			encoded = Base64.encodeBase64(encodedpassword.getBytes());
			processCommand=new ProcessCommandImpl(prop2);
		} catch (Exception e) {
			logger.error("Exception:" + e.getStackTrace());
		}

	}

	@Override
	public void run() {

		while (true) {
			logger.info("Consumer() class calling ...");
			try {
				synchronized (queue) {
					consume();
					queue.wait();
				}
			} catch (InterruptedException ex) {
				logger.error("thread is Interrupted for the : " + ex.getCause().getLocalizedMessage());
			}
		}
	}

	private void consume() {
		try {
				while (!queue.isEmpty()) {
					for (ProcessRequestData processRequestData : queue) {
						queue.poll();		
						processCommand.processRequest(processRequestData);					
					}
					queue.notifyAll();
				}
			 
		} catch (Exception e) {
			logger.error("Exception is : " + e.getCause().getLocalizedMessage());
		}

	}

	public static void sendResponse(String output, Long id, Long prdetailsId) {
		
		try {
			post = new HttpPost(prop.getString("BSSServerQuery").trim() + "/" +id);
			post.setHeader("Authorization", "Basic " + new String(encoded));
			post.setHeader("Content-Type", "application/json");
			post.addHeader("X-Mifos-Platform-TenantId", tenantIdentifier);

			
			JSONObject object = new JSONObject();

				object.put("receiveMessage", output);
				object.put("receivedStatus", "1");
				object.put("prdetailsId", prdetailsId);
			
			//logger.info("The json data sending to BSS System is :"+object.toString());
			StringEntity se = new StringEntity(object.toString());
			post.setEntity(se);
			HttpResponse response = httpClient.execute(post);
			response.getEntity().consumeContent();
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());	
				Thread.sleep(wait);
				return;
			}
			else{
				logger.info("record is Updated Successfully in Bss System");
			}
		  

		} catch (IOException e) {
			logger.error("IOException : " + e.getMessage() + ". verify the BSS system server running or not");
		} catch (InterruptedException e) {
			logger.error("thread is Interrupted for the : " + e.getMessage());
		} catch (Exception e) {
		logger.error("Exception : " + e.getMessage());
	    }
	  
	}
}
