package lab2;

public class Dipositor extends Thread {
    Account ac;
    public Dipositor(Account ac){
        this.ac = ac;
    }

    @Override
    public void run() {
        for(int i=0; i<100000;i++){
            ac.diposit();
        }
        
        System.out.println("Depositor finished");
    }
}
