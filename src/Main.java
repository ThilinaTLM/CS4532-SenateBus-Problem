import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BoardingArea boardingArea = new BoardingArea();

        // rider arrival scheduler
        new Thread(() -> {
            while (true) {
                AtomicLong generator = new AtomicLong(1000);
                sleep((long) (Math.random() * 1000 + 100));
                Rider rider = new Rider(boardingArea);
                new Thread(rider).start();
            }
        }).start();

        // senate bus scheduler
        new Thread(() -> {
            while (true) {
                AtomicLong generator = new AtomicLong(100);
                sleep(5000);
                SenateBus bus = new SenateBus(boardingArea);
                new Thread(bus).start();
            }
        }).start();

        while (true) {
            sleep(1000);
            boardingArea.showInfo();
        }
    }


}

class BoardingArea {

    private final Queue<Rider> waiting = new LinkedList<>();
    private final ReentrantLock waitingInLock = new ReentrantLock();
    private final ReentrantLock waitingOutLock = new ReentrantLock();


    private SenateBus bus;
    private final Semaphore busArrived = new Semaphore(0);


    void arriveBus(SenateBus bus) {
        waitingInLock.lock();
        busArrived.release();
        this.bus = bus;
        waiting.forEach(Rider::signal);
    }

    void departureBus() throws InterruptedException {
        this.bus = null;
        busArrived.acquire();
        waitingInLock.unlock();
    }

    void getToQueue(Rider rider) {
        waitingInLock.lock();
        waiting.add(rider);
        waitingInLock.unlock();
    }

    void getToBus(Rider rider) throws InterruptedException {
        waitingOutLock.lock();
        waiting.remove(rider);
        waitingOutLock.unlock();

        Main.sleep(500);

        busArrived.acquire();
        bus.board();
        busArrived.release();

        bus.signal();
    }

    public boolean isQueueEmpty() {
        waitingInLock.lock();
        boolean empty = waiting.isEmpty();
        waitingInLock.unlock();
        return empty;
    }

    void showInfo() {
        System.out.printf(
                "[Riders: %d] [Bus: %b %d]\n",
                waiting.size(),
                bus != null,
                bus != null ? bus.getEmptySeats() : 0
        );
    }
}

class SenateBus implements Runnable {

    private static final int MAX_CAPACITY = 50;
    private final Semaphore emptySeats = new Semaphore(MAX_CAPACITY);
    private final BoardingArea boardingArea;

    private final Semaphore waitSignal = new Semaphore(0);

    SenateBus(BoardingArea boardingArea) {
        this.boardingArea = boardingArea;
    }



    public void signal() {
        waitSignal.release();
    }

    void board() {
        try {
            emptySeats.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    int getEmptySeats() {
        return emptySeats.availablePermits();
    }

    @Override
    public void run() {
        try {
            boardingArea.arriveBus(this);
            while (emptySeats.availablePermits() > 0 && !boardingArea.isQueueEmpty()) {
                waitSignal.acquire();
            }
            boardingArea.departureBus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


class Rider implements Runnable {

    private final BoardingArea boardingArea;

    private final Semaphore waitSignal = new Semaphore(0);

    Rider(BoardingArea ba) {
        this.boardingArea = ba;
    }

    public void signal() {
        waitSignal.release();
    }

    @Override
    public void run() {
        try {
            this.boardingArea.getToQueue(this);
            this.waitSignal.acquire();
            this.boardingArea.getToBus(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}