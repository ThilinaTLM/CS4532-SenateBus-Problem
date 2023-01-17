import java.util.concurrent.Semaphore;

public class SenateBus implements Runnable {

    private final BusStop busStop;
    private final Semaphore allBoard = new Semaphore(0); // use to signal all riders have boarded

    SenateBus(BusStop busStop) {
        this.busStop = busStop;
    }

    void signal() {
        allBoard.release();
    }


    @Override
    public void run() {
        try {
            busStop.arriveBus(this);
            this.allBoard.acquire(); // wait for all riders to board
            busStop.departureBus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}