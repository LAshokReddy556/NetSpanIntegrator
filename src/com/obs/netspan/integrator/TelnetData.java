package com.obs.netspan.integrator;

import org.apache.commons.net.telnet.*;
import java.io.*;
import java.util.Properties;

public class TelnetData {
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	private String prompt = "Access denied";

	public TelnetData(String server, String password) {
		try {
			// Connect to the specified server
			telnet.connect(server, 23);

			// Get input and output stream references
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());

			// Log the user on
			readUntil("Password: ");
			write(password);
			/*String da=readUntil(prompt);
			if(da!=null){
			System.out.println("\n");
			System.out.println("output is: "+da.trim() +" ,Reddy.");
			}*/
			// Advance to a prompt
			//readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			char ch = (char) in.read();
			while (true) {
				System.out.print(ch);
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public String readData(String pattern) {
		try {
			int i=0;
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			char ch = (char) in.read();
			while (true) {
				System.out.print(ch);
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						
						if(i==1){
						return sb.toString();
						}
						i=i+1;
					}
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
			System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String sendCommand2(String command) {
		try {
			write(command);
			return readUntil("# ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String sendCommand(String command) {
		try {
			write(command);
			return readData("# ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			System.out.println("DisConnection called");
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*	public static void main(String[] args) {
		try {
			Properties prop = new Properties();
			InputStream inputstream = new FileInputStream("PacketeerIntegrator.properties");
			prop.load(inputstream);		
			TelnetSample telnet = new TelnetSample(prop.getProperty("hostname"),prop.getProperty("password"));
			String data=telnet.sendCommand(prop.getProperty("command"));
			System.out.println("data is:"+data.trim());
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
