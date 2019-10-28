package lab2;

public class Main {
	public static void main (String[] args) {
		Account ac = new Account();
		new Dipositor(ac).start();
		new Withdrawer(ac).start();

		System.out.println("Balance is: " + ac.bal);
	}
}
