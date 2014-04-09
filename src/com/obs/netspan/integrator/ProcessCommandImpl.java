package com.obs.netspan.integrator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

public class ProcessCommandImpl {

	private static BufferedReader reader;
	private static PrintWriter printWriter;
	static Logger logger = Logger.getLogger(Consumer.class);
	public static int wait;
	PropertiesConfiguration prop;

	public ProcessCommandImpl(PropertiesConfiguration prop2) {
		// TODO Auto-generated constructor stub
		this.prop=prop2;
	}

	public void processRequest(ProcessRequestData processRequestData) {
		
		try {		
				if (processRequestData.getRequestType().equalsIgnoreCase(
						ComvenienConstants.REQ_ACTIVATION)) {
					
					JSONObject values=new JSONObject(processRequestData.getProduct());
					
					System.out.println(processRequestData.getHardwareId());
					String credentilaData="<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
							"<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"+
							"<soap12:Header>"+	  
							    "<Credentials xmlns=\"http://Airspan.WiMax.WebServices\">"+
							      "<Username>OBS</Username>"+
							      "<Password>0b5@spice</Password>"+
							    "</Credentials>"+
							  "</soap12:Header>"+
							  "<soap12:Body>"+
							  "<ProvisionCustomMS xmlns=\"http://Airspan.WiMax.WebServices\">";
					String endtag="</ProvisionCustomMS>"+
							  "</soap12:Body>"+
							  "</soap12:Envelope>";
					
					StringBuilder data=new StringBuilder();
					data.append(credentilaData);
					data.append("<macAddress>" + values.getString("macId") + "</macAddress>");
					data.append("<serviceProductName>" + values.getString("SERVICE") +"</serviceProductName>");
					//data.append("<homeSectorBsId>" + values.get("Channel") + "</homeSectorBsId>");
					data.append("<customConfigName>ETH_CS</customConfigName>");
					data.append("<vlanPortProfileName>" + values.getString("VLAN_ID") +"</vlanPortProfileName>");
					data.append(endtag);
					logger.info("Netspan processing Data is :"+data);
					String NetSpanResult=NetSpanProcess(ComvenienConstants.NETSPAN_ACTIVATION_URL, data.toString(), ComvenienConstants.PROVISIONCUSTOMMS_ACTIVATION); 

					//for packeteer
					Long id=values.getLong("clientId");
					StringBuilder packteerData=new StringBuilder();
					packteerData.append(values.getString("GROUP_NAME"))
					.append(" ")
					.append(id)
					.append(" inside ")
					.append(values.getString("IP_ADDRESS"));
					
					String PacketeerResult=PacketeerProcess(packteerData.toString(),id.toString());
					String Result;
					if(PacketeerResult.equalsIgnoreCase("Success") && NetSpanResult.equalsIgnoreCase("Success")){
						Result="Success";
					}else{
						Result="Netspan-Result="+NetSpanResult+" && "+" Packeteer_Result=failure";
					}
					
					ProcessCommandImpl.process(Result, processRequestData.getId(),processRequestData.getPrdetailsId());	
					} else if (processRequestData.getRequestType().equalsIgnoreCase(
						ComvenienConstants.REQ_DISCONNECTION)) {
						
						String credentilaData="<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
								"<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"+
								"<soap12:Header>"+	  
								    "<Credentials xmlns=\"http://Airspan.WiMax.WebServices\">"+
								      "<Username>OBS</Username>"+
								      "<Password>0b5@spice</Password>"+
								    "</Credentials>"+
								  "</soap12:Header>"+
								  "<soap12:Body>"+
								  "<DisableService xmlns=\"http://Airspan.WiMax.WebServices\">";
						String endtag="</DisableService>"+
								  "</soap12:Body>"+
								  "</soap12:Envelope>";
						
						StringBuilder data=new StringBuilder();
						data.append(credentilaData);
						data.append("<macAddress>" + processRequestData.getHardwareId() + "</macAddress>");
						data.append(endtag);
						logger.info(data);
						
						String Result=NetSpanProcess(ComvenienConstants.NETSPAN_DISABLESERVICE_URL, data.toString(), ComvenienConstants.DISABLESERVICE_DISCONNECTION); 
						ProcessCommandImpl.process(Result, processRequestData.getId(),processRequestData.getPrdetailsId());	
						
					}	
		}  catch (Exception e) {
			logger.error("(ConfigurationException) Properties file loading error.... : " + e.getMessage());
		} 

	}
	
	private String PacketeerProcess(String packteerData, String clientId) {
		String result="";
		String uname = prop.getString("packeteerhostname");
		String pass = prop.getString("packeteerpassword");
		TelnetData telnetData=new TelnetData(uname, pass);
		
		String data="Traffic class \" "+clientId+"\" created.";
		
		String Command1= "class new "+ ComvenienConstants.INBOUND_MSG + "/" +packteerData;
		String PacketeerResult1=telnetData.sendCommand(Command1);
		if(PacketeerResult1.contains(data)){
			result="Success";
		}else{
			result="inbound msg:"+PacketeerResult1;
		}
		
		String Command2= "class new "+ ComvenienConstants.OUTBOUND_MSG + "/" +packteerData;
		String PacketeerResult2=telnetData.sendCommand2(Command2);
		
		if(PacketeerResult2.contains(data)){
			if(result.equalsIgnoreCase("Success")){
				result="Success";
			}
			else{
				result=result+"---->outbound msg output : "+PacketeerResult2;
			}
		}else{
			result=result+"---->outbound msg output :"+PacketeerResult2;
		}
		
		telnetData.disconnect();
		return result;
		
	}

	public static void process(String value, Long id, Long prdetailsId){
		
		try{		
			logger.info("output from Packetspan Server is :" +value);
			if(value==null){
				throw new NullPointerException();
			}else{		
				Consumer.sendResponse(value,id,prdetailsId);
			}		
		} catch(NullPointerException e){
			logger.error("NullPointerException : Output from the Oss System Server is : " + value);
		} catch (Exception e) {
		    logger.error("Exception : " + e.getMessage());
	    }
		
	}
	
	public String NetSpanProcess(String url,String data,String tagName){
		try{
			URL oURL = new URL(url);
	        HttpURLConnection soapConnection = (HttpURLConnection) oURL.openConnection();
	        System.out.println("connect to server...");
	        //prop= new PropertiesConfiguration("NetSpanIntegrator.properties");
	        // Send SOAP Message to SOAP Server
	        soapConnection.setRequestMethod("POST");
	        soapConnection.setRequestProperty("Host", prop.getString("host_address"));
	        soapConnection.setRequestProperty("Content-Length", String.valueOf(data.length()));
	        soapConnection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
	        soapConnection.setRequestProperty("SoapAction", "");
	        soapConnection.setDoOutput(true);
	        
	        OutputStream reqStream = soapConnection.getOutputStream();
	        System.out.println("Sending Soap Request is: "+data);
	        System.out.println("sending data to server");
	        reqStream.write(data.getBytes());
	        System.out.println("output receive from server");
	        BufferedReader br = new BufferedReader(new InputStreamReader(soapConnection.getInputStream()));
	        StringBuilder responseSB = new StringBuilder();
	        String line;
	        String statusResult = null;
	        while ( (line = br.readLine()) != null){
	        	responseSB.append(line);
	        	
	       	    MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
	 		
			    SOAPMessage message = mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(responseSB.toString().getBytes(Charset.forName("UTF-8"))));
 
			    NodeList returnList=message.getSOAPBody().getElementsByTagName(tagName);
			    
			        NodeList innerResultList = returnList.item(0).getChildNodes();
			        
			        
	        	if (innerResultList.item(0).getNodeName()
	                    .equalsIgnoreCase("ReturnCode")) {
	              int isSucces = Integer.valueOf(innerResultList.item(0)
	                        .getTextContent().trim());
	              
	              if(isSucces==0){
	            	   statusResult="Success";
	            	   System.out.println(statusResult);
	              }else{
	            	   
	            	  if (innerResultList.item(1).getNodeName()
			                    .equalsIgnoreCase("ReturnString")) {
			              String resulterror = innerResultList.item(1).getTextContent().trim();
			              statusResult="failure :"+resulterror;
			              System.out.println(statusResult);
			              
	            	  }
	              }
	            }
	        	
	        }
	        // Close streams
	        br.close();
	        reqStream.close();
	        return statusResult;
	                 
	       
		}catch(MalformedURLException e){
			return null;
		}catch(IOException e){
			return null;
		}catch(Exception e){
			return null;
		}
		
	}

	
	

	
}
