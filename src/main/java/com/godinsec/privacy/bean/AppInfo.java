package com.godinsec.privacy.bean;

public class AppInfo extends ShortCutInfo {
	
	public static final int INSTALL_STATE = 0x01;
	public static final int UNINSTALL_STATE = 0x02;
	

    private String pckName;
    
    private int installState;
    
    public int getInstallState() {
		return installState;
	}


	public void setInstallState(int installState) {
		this.installState = installState;
	}

    public String getPckName() {
		return pckName;
	}

	public void setPckName(String pckame) {
		this.pckName = pckame;
	}

	@Override
	public String toString() {
		StringBuilder sb  = new StringBuilder();
		sb.append("包名为 [ ").append(pckName).append(" ]")
				.append("标题是 [ ").append(getTitle()).append(" ] 的应用:");
		return sb.toString();
	}
}
