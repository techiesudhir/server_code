import java.util.concurrent.TimeUnit;


public class threadexmpl implements Runnable {
	int i;
	public threadexmpl(int j){
		this.i=j;
	}
	public void run() {
		while(true){
				System.out.println("Thread :"+i);
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread myrunnable = new Thread(new threadexmpl(1),"T1");
		Thread myrunnable1 = new Thread(new threadexmpl(2),"T2");
		//myrunnable.setDaemon(true);
		myrunnable.start();
		myrunnable1.start();
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Ending main thread");
	}

}
