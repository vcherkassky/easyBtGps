package cherkassky.victor.easybtgps.nmea;

import java.util.StringTokenizer;


/**
 * @author victor
 *
 * Created Aug 31, 2010
 *
 */
public class Message {
	
	private final String message;
	
	private final Type type;
	
	public Message(String message) {

		this.message = message;
		
		if(message == null || message.length() < 6) {
			this.type = Type.UNKNOWN;
		} else {
			this.type = Type.valueOf(message.substring(1, 6));
		}
	}
	
	/**
	 * Gets the message 
	 *
	 * @return the message
	 */
	public String getMessage() {
	
		return this.message;
	}
	
	/**
	 * Gets the type 
	 *
	 * @return the type
	 */
	public Type getType() {
	
		return this.type;
	}
	
	public boolean validate() {
		
		String countedChecksum = Message.getNmeaChecksum(this.message);
		StringTokenizer st = new StringTokenizer(this.message, "*");
		st.nextToken();
		String receivedChecksum = st.nextToken();
		
		return countedChecksum.equalsIgnoreCase(receivedChecksum);
	}
	
	public static String getNmeaChecksum(String sentence) {
		
		char character;
		Integer checksum = null;
		int length = sentence.length();
		
		for(int i=0; i<length; i++) {
			
			character = sentence.charAt(i);
			if(character == '$') {
				continue;
			}
			if(character == '*') {
				break;
			}
			if(checksum == null) {
				// if this is the first checksum assignment, just assign a character
				checksum = new Integer((byte) character);
			} else {
				// XOR checksum with character
				checksum = checksum ^ ((byte) character);
			}
		}
		
		return Integer.toHexString(checksum);
	}

	public static enum Type {
		
		GPGGA, GPGSA, GPGSV, GPRMC, GPVTG, UNKNOWN;
	}
}
