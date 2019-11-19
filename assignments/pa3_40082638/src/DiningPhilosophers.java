public class DiningPhilosophers {
	public static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 4;
	public static final int DINING_STEPS = 10;
	public static Monitor soMonitor = null;

	public static void main(String[] argv) {
		try {
			int iPhilosophers = argv.length > 0 ? 
							Integer.parseInt(argv[0]) : DEFAULT_NUMBER_OF_PHILOSOPHERS;

			soMonitor = new Monitor(iPhilosophers); // initialize monitor
			Philosopher aoPhilosophers[] = new Philosopher[iPhilosophers]; 

			for (int j = 0; j < iPhilosophers; j++) {
				aoPhilosophers[j] = new Philosopher(); // initialize philosophers
				aoPhilosophers[j].start(); // Start their thread
			}

			System.out.println(iPhilosophers + " philosopher(s) came in for a dinner.");

			for(int j = 0; j < iPhilosophers; j++)
				aoPhilosophers[j].join(); // wait for thread to terminate 

			System.out.println("All philosophers have left. System terminates normally.");
		}
		catch (NumberFormatException e) {
			System.out.println("\"" + argv[0] + "\" is not a valid positive integer.");
			System.out.println("Usage: java DiningPhilosophers [NUMBER_OF_PHILOSOPHER]");
			System.exit(1);
			return;
		}
		catch(InterruptedException e) {
			System.err.println("main():");
			reportException(e);
			System.exit(1);
		}
	}

	public static void reportException(Exception poException) {
		System.err.println("Caught exception : " + poException.getClass().getName());
		System.err.println("Message          : " + poException.getMessage());
		System.err.println("Stack Trace      : ");
		poException.printStackTrace(System.err);
	}
}
