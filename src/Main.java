
public class Main {

    static long BUS_ARRIVAL_INTERVAL_MIN = 5000;
    static long BUS_ARRIVAL_INTERVAL_MAX = 9000;

    static long RIDER_ARRIVAL_INTERVAL_MIN = 700;
    static long RIDER_ARRIVAL_INTERVAL_MAX = 1000;

    static long RIDER_BOARDING_INTERVAL_MIN = 100;
    static long RIDER_BOARDING_INTERVAL_MAX = 1000;

    static long CONSOLE_UPDATE_INTERVAL = 1000;


    public static void main(String[] args) {
        BusStop busStop = new BusStop();

        // Create and start riders threads with random interval
        new Thread(() -> {
            while (true) {
                Main.randomSleep(RIDER_ARRIVAL_INTERVAL_MIN, RIDER_ARRIVAL_INTERVAL_MAX);
                Rider rider = new Rider(busStop);
                new Thread(rider).start();
            }
        }).start();

        // Create and start bus threads with random interval
        new Thread(() -> {
            while (true) {
                Main.randomSleep(BUS_ARRIVAL_INTERVAL_MIN, BUS_ARRIVAL_INTERVAL_MAX);
                SenateBus bus = new SenateBus(busStop);
                new Thread(bus).start();
            }
        }).start();

        while (true) {
            sleep(CONSOLE_UPDATE_INTERVAL);
            busStop.showInfo();
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void randomSleep(long from, long to) {
        sleep((long) (Math.random() * (to - from) + from));
    }

}






