import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class BusStop {

    private static final int MAX_CAPACITY = 50;

    private final Queue<Rider> riders = new LinkedList<>();
    private final ReentrantLock inMutex = new ReentrantLock(); // use to lock new riders entering the bus stop
    private final ReentrantLock outMutex = new ReentrantLock(); // use to lock riders leaving the bus stop
    private final Semaphore queueLimit = new Semaphore(MAX_CAPACITY); // maintain the queue size
    private SenateBus bus;

    void waitForBus(Rider rider) throws InterruptedException {
        queueLimit.acquire(); // update queue limiting semaphore
        inMutex.lock(); // lock other riders from entering the bus stop
        riders.add(rider); // add rider to the queue
        inMutex.unlock(); // let other riders enter the bus stop
    }

    void arriveBus(SenateBus bus) throws InterruptedException {
        inMutex.lock(); // lock the queue
        outMutex.lock(); // lock the queue
        this.showInfo(1);
        if (riders.isEmpty()) {
            this.bus.signal();
        }
        this.bus = bus;
        this.riders.forEach(Rider::signal);
        outMutex.unlock();
    }

    void boardBus(Rider rider) {
        this.queueLimit.release(); // get out from the queue
        this.outMutex.lock();
        this.riders.remove(rider);
        Main.randomSleep(Main.RIDER_BOARDING_INTERVAL_MIN, Main.RIDER_BOARDING_INTERVAL_MAX);
        if (riders.isEmpty()) {
            this.bus.signal();
        }
        this.outMutex.unlock();
    }

    void departureBus() throws InterruptedException {
        this.bus = null;
        this.showInfo(-1);
        inMutex.unlock(); // lock the queue
    }


    void showInfo() {
        this.showInfo(0);
    }
    synchronized void showInfo(int status) {
        if (status == -1) {
            System.out.println("---------------- Bus departed -----------------");
        } else if (status == 1) {
            System.out.println("---------------- Bus arrived -----------------");
        } else {
            if (bus == null) {
                System.out.printf(
                        "RIDERS ARE QUEUEING (%d)\n",
                        riders.size()
                );
            } else {
                System.out.printf(
                        "GETTING TO BUS (%d)\n",
                        riders.size()
                );
            }
        }
    }
}