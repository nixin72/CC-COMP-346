package lab2;

public class Withdrawer extends Thread {
    Account ac;
    public Withdrawer(Account ac){
        this.ac = ac;
    }

    @Override
    public void run() {
        for(int i=0; i<100000;i++){
            ac.withdraw();
        }
        
        System.out.println("Withdrawer finished");
    }
}
