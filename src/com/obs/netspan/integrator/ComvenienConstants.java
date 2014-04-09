package com.obs.netspan.integrator;

public class ComvenienConstants {
	
	
	//For Netspan Commands
	public static final String PROVISIONCUSTOMMS_ACTIVATION="ProvisionCustomMSResult";
	public static final String DISABLESERVICE_DISCONNECTION="DisableServiceResult";
	public static final String MESSAGE_CMD="INDOSM";
	public static final String HOLD_MESSAGE="HOLDTL";
	
	//For Packeteer Commands
	public static final String INBOUND_MSG="inbound";
	public static final String OUTBOUND_MSG="outbound";
	
	//For request Type
	public static final String REQ_ACTIVATION="ACTIVATION";
	public static final String REQ_DISCONNECTION="DISCONNECTION";
	public static final String REQ_RECONNECT="RECONNECTION";
    public static final String REQ_MESSAGE="OSDMESSAGE";
    
    //For Netspan urls
    public static final String NETSPAN_ACTIVATION_URL="http://172.30.1.50/WiMaxWS1/SSProvisioning.asmx";
     // DisableService or Disconnect Service
    public static final String NETSPAN_DISABLESERVICE_URL="http://172.30.1.50/WiMaxWS1/SSProvisioning.asmx";
}
