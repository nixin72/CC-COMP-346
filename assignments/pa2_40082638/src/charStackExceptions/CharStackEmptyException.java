package charStackExceptions;

@SuppressWarnings ("serial")
public class CharStackEmptyException extends Exception {
	public CharStackEmptyException () {
		super("Char Stack is empty.");
	}
}