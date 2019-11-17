import charStackExceptions.*;

public class StackManager {
	private static int iThreadSteps = 3;
	private static CharStack stack = new CharStack();
	private static Semaphore task2Sem = new Semaphore(1);
	private static Semaphore task4Sem = new Semaphore();
	private static int pushCount = 0;

	public static void main (String argv[]) {
		try {
			System.out.println("----------------------------------------------------");
			System.out.println("Main thread starts executing.");
			System.out.println("----------------------------------------------------");
			System.out.println("Initial value of top = " + stack.getTop() + ".");
			System.out.println("Initial value of stack top = " + CharStack.peek() + ".");
			System.out.println("\nMain thread will now fork several threads.");
			System.out.println("----------------------------------------------------");
		}
		catch (CharStackEmptyException e) {
			System.out.println("Caught exception: StackCharEmptyException");
			System.out.println("Message : " + e.getMessage());
			System.out.println("Stack Trace : ");
			e.printStackTrace();
		}

		var ab1 = new Consumer();
		var ab2 = new Consumer();
		System.out.println("Two Consumer threads have been created.");
		var rb1 = new Producer();
		var rb2 = new Producer();
		System.out.println("Two Producer threads have been created.");
		var csp = new CharStackProber();
		System.out.println("One CharStackProber thread has been created.");
		System.out.println();

		ab1.start();
		rb1.start();
		ab2.start();
		rb2.start();
		csp.start();

		try {
			ab1.join();
			ab2.join();
			rb1.join();
			rb2.join();
			csp.join();

			System.out.println("\nSystem terminates normally.");
			System.out.println("----------------------------------------------------");
			System.out.println("Final value of top = " + stack.getTop() + ".");
			System.out.println("Final value of stack top = " + CharStack.peek() + ".");
			System.out.println("Final value of stack top-1 = " + CharStack.getAt(stack.getTop() - 1) + ".");
			System.out.println("Stack access count = " + stack.getAccessCounter());
		}
		catch (InterruptedException e) {
			System.out.println("Caught InterruptedException: " + e.getMessage());
			System.exit(1);
		}
		catch (Exception e) {
			System.out.println("Caught exception: " + e.getClass().getName());
			System.out.println("Message : " + e.getMessage());
			System.out.println("Stack Trace : ");
			e.printStackTrace();
		}
	}

	static class Consumer extends BaseThread {
		private char copy;

		public void run () {
			task4Sem.Wait();
			System.out.println("Consumer thread [TID=" + this.iTID + "] starts executing.");
			for (var i = 0; i < StackManager.iThreadSteps; i++) {
				task2Sem.Wait();
				try {
					this.copy = CharStack.pop();
				}
				catch (CharStackEmptyException e) {
					e.printStackTrace();
				}

				System.out.println("Consumer thread [TID=" + this.iTID + "] pops character = " + this.copy);
				task2Sem.Signal();
			}

			System.out.println("Consumer thread [TID=" + this.iTID + "] terminates.");
			task4Sem.Signal();
		}
	}

	static class Producer extends BaseThread {
		private char block;

		public void run () {
			System.out.println("Producer thread [TID=" + this.iTID + "] starts executing.");
			for (var i = 0; i < StackManager.iThreadSteps; i++) {
				task2Sem.Wait();
				try {
					this.block = (char) (CharStack.peek() + 1);
					CharStack.push(this.block);
				}
				catch (CharStackEmptyException e) {
					e.printStackTrace();
				}
				catch (CharStackFullException e) {
					e.printStackTrace();
				}

				System.out.println("Producer thread [TID=" + this.iTID + "] pushes character = " + this.block);
				task2Sem.Signal();
			}

			System.out.println("Producer thread [TID=" + this.iTID + "] terminates.");
			pushCount++;
			if (pushCount == 2) {
				task4Sem.Signal();
			}
		}
	}

	static class CharStackProber extends BaseThread {
		public void run () {
			System.out.println("CharStackProber thread [TID=" + this.iTID + "] starts executing.");

			for (var i = 0; i < 2 * StackManager.iThreadSteps; i++) {
				task2Sem.Wait();

				var stack = new StringBuilder("Stack S = (");
				for (var j = 0; j < 10; j++) {
					char top = '$';

					try {
						top = CharStack.getAt(j);
					}
					catch (CharStackInvalidAccessException e) {}

					stack.append("[" + top + "], ");
				}

				System.out.println(stack.substring(0, stack.length() - 2) + ")");
				task2Sem.Signal();
			}

			System.out.println("CharStackProber thread [TID=" + this.iTID + "] terminates.");
		}
	}
}
