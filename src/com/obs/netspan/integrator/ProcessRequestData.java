
package com.obs.netspan.integrator;


public class ProcessRequestData {

	private String command;
    private String hardwareId;
    private String product; 
    private String requestType;
    private Long id;
	private Long serviceId;
	private Long prdetailsId;
	private int serialNo;

	
   /* public ProcessRequestData(int id, String command,String hardwareId,String product, String string) {
        this.id = id;
        this.command = command;
        this.smartcardId=hardwareId;
        this.product=product;
    }

   

    public ProcessRequestData(int id, String command, String smsOperatorId) {
		// TODO Auto-generated constructor stub
    	 this.id = id;
         this.command = command;
         this.smsOperatorId=smsOperatorId;
        
	}
*/





	public ProcessRequestData(int serialNo, String product, String hardwareId,String requestType,Long id, Long serviceId,Long prdetailsId) {
		//this.serialNo=serialNo;
		this.product=product;
		this.hardwareId=hardwareId;
		this.requestType=requestType;
		this.id=id;
		this.serviceId=serviceId;
		this.prdetailsId=prdetailsId;
	}



	public Long getId() {
		return id;
	}

	public int getSerialNo() {
		return serialNo;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getHardwareId() {
		return hardwareId;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}

	public String getProduct() {
		return product;
	}
	
	public String getRequestType() {
		return requestType;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public Long getPrdetailsId() {
		return prdetailsId;
	}

	
	
}
