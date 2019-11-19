import common.BaseThread;

public class Philosopher extends BaseThread {
	public static final long MAX_TIME_TO_WASTE = 1000;

	// Print that they're eating, wait, print that they're done eating 
	public void eat() {
		try {
			System.out.println("Philosopher " + getTID() + " has started eating.");
      sleep((long)(Math.random() * MAX_TIME_TO_WASTE));
			System.out.println("Philosopher " + getTID() + " is done eating.");
    }
		catch(InterruptedException e) {
      System.err.println("Philosopher.eat():");
      DiningPhilosophers.reportException(e);
      System.exit(1);
    }
	}

	// Print that they're thinking, wait, print that they're done thinking 
	public void think() {
		try {
			System.out.println("Philosopher " + getTID() + " has started thinking.");
      sleep((long)(Math.random() * MAX_TIME_TO_WASTE));
			System.out.println("Philosopher " + getTID() + " is done thinking.");
    }
		catch(InterruptedException e) {
      System.err.println("Philosopher.think():");
      DiningPhilosophers.reportException(e);
      System.exit(1);
    }
	}

	// Print that they're talking, say something, print that they're done talking 
	public void talk() {
		System.out.println("Philosopher " + getTID() + " has started talking.");
		saySomething();
		System.out.println("Philosopher " + getTID() + " is done talking.");
	}

	public void run() {
		for (int i = 0; i < DiningPhilosophers.DINING_STEPS; i++) {
      try {
				DiningPhilosophers.soMonitor.pickUp(getTID());
				eat();
				DiningPhilosophers.soMonitor.putDown(getTID());
				think();

				// Decide whether or not to talk
				if(Math.random() > 0.5) {
      		DiningPhilosophers.soMonitor.requestTalk();
          talk();
          DiningPhilosophers.soMonitor.endTalk();
        }
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
    }
	} 

	public void saySomething() {
		String[] astrPhrases = {
      "Eh, it's not easy to be a philosopher: eat, think, talk, eat...",
      "You know, true is false and false is true if you think of it",
      "2 + 2 = 5 for extremely large values of 2...",
      "If thee cannot speak, thee must be silent",
      "My number is " + getTID() + ""
    };

		System.out.println("Philosopher " + getTID() + " says: " + 
						astrPhrases[(int)(Math.random() * astrPhrases.length)]);
	}
}
