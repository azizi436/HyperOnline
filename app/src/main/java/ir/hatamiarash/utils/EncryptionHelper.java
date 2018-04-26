package ir.hatamiarash.utils;

import ir.hatamiarash.hyperonline.BuildConfig;
import se.simbio.encryption.Encryption;

public class EncryptionHelper {
	private String KEY;
	private String SALT;
	private byte[] IV;
	private Encryption encryption;
	
	public EncryptionHelper() {
		this.KEY = BuildConfig.ENCRIPTION_KEY;
		this.SALT = BuildConfig.ENCRIPTION_KEY;
		this.IV = new byte[16];
		this.encryption = Encryption.getDefault(this.KEY, this.SALT, this.IV);
	}
	
	public String encrypt(String text) {
		return this.encryption.encryptOrNull(text);
	}
	
	public String decrypt(String text) {
		return this.encryption.decryptOrNull(text);
	}
}
