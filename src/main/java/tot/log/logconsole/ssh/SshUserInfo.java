package tot.log.logconsole.ssh;

import com.jcraft.jsch.UserInfo;

public class SshUserInfo implements UserInfo {

	private String password;

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
		System.out.println(message);
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
