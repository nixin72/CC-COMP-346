package charStackExceptions;

@SuppressWarnings ("serial")
public class CharStackFullException extends Exception {
	public CharStackFullException () {
		super("Char Stack has reached its capacity of CharStack.MAX_SIZE.");
	}
}