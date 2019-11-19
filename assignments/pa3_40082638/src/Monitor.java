public class Monitor {
	// Help keep track of who's used the chopsticks
	class Chopstick {
		public boolean available = true;
		public int lastUser;
		
		public Chopstick() {
			available = true;
			lastUser = -1; // no last user
		}
		
		// If it's not available and you're the last user, you must have the chopstick currently
		public boolean iHave(final int piTID) {
			return !available && lastUser == piTID;
		}
		
		// If it's available and you don't have it, it's clearly not available
		public boolean elseHas(final int piTID) {
			return !available && piTID != lastUser;
		}
		
		public void pickUp(final int piTID) {
			available = false;
			lastUser = piTID;
		}
		
		public void putDown() {
			available = true;
		}
	}

	private static int len; // just a helped to prevent chopsticks.length, which is verbose
	private static Chopstick[] chopsticks;
	private static boolean talking;
	
	public Monitor(int piNumberOfPhilosophers) {
		len = piNumberOfPhilosophers;
		chopsticks = new Chopstick[len];
		talking = false;
		
		// initialize all the chopstick
		for (var i = 0 ; i < chopsticks.length ; i++) {
			chopsticks[i] = new Chopstick();
		}
	}

	public synchronized void pickUp(final int piTID) throws InterruptedException {
		var left = chopsticks[piTID - 1];
		var right = chopsticks[piTID % len];

		for (;;) {
			// if either chopstick is in use
			if (left.elseHas(piTID) || right.elseHas(piTID)) {
				// if it's available and someone else just finished using it
				if (left.available && left.lastUser != piTID) left.pickUp(piTID);
				if (right.available && right.lastUser != piTID) right.pickUp(piTID);

				wait();
			}
			// if they're both free, pick up both
			else {
				left.pickUp(piTID);
				right.pickUp(piTID);
				break;
			}
		}
	}

	// Release both chopsticks and notify that they're available
	public synchronized void putDown(final int piTID) {
		chopsticks[piTID-1].putDown();
		chopsticks[piTID % len].putDown();
		notifyAll();
	}

	// Wait until they're able to talk, then set talking to true when it's their turn
	public synchronized void requestTalk() throws InterruptedException {
		for (;;) {
			if (talking) {
				wait();
			}
			else {
				talking = true;
				break;
			}
		}
	}

	// Notify other philosophers they can talk
	public synchronized void endTalk() {
		talking = false;
		notifyAll();
	}
}
