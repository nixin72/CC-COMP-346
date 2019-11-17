class Semaphore {
	private int value;

	public Semaphore (int value) {
		this.value = Math.abs(value);
	}

	public Semaphore () {
		this.value = 0;
	}

	public synchronized void Wait () {
		this.value--;

		if (this.value < 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Semaphore::Wait() - caught InterruptedException: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public synchronized void Signal () {
		this.value++;
		notify();
	}

	public synchronized void P () {
		this.Wait();
	}

	public synchronized void V () {
		this.Signal();
	}
}
