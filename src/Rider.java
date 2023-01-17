import java.util.concurrent.Semaphore;

public class Rider implements Runnable {

    private final BusStop busStop;

    private final Semaphore arrivalSignal = new Semaphore(0); // use to signal arrival of bus to rider

    Rider(BusStop busStop) {
        this.busStop = busStop;
    }

    public void signal() {
        arrivalSignal.release();
    }

    @Override
    public void run() {
        try {
            this.busStop.waitForBus(this);
            this.arrivalSignal.acquire(); // wait for bus to arrival
            this.busStop.boardBus(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}