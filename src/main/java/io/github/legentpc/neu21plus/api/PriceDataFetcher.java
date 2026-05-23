package io.github.legentpc.neu21plus.api;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceDataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceDataFetcher.class);

    private static final PriceDataFetcher INSTANCE = new PriceDataFetcher();

    public static PriceDataFetcher getInstance() {
        return INSTANCE;
    }

    private Thread fetcherThread;
    private volatile boolean running = false;
    private long lastFetchTime = 0;
    private static final long FETCH_INTERVAL = 60000;

    private PriceDataFetcher() {
    }

    public void start() {
        if (running) return;

        running = true;
        fetcherThread = new Thread(this::fetchLoop, "NEU-PriceFetcher");
        fetcherThread.setDaemon(true);
        fetcherThread.start();

        LOGGER.info("Price data fetcher started");
    }

    public void stop() {
        running = false;
        if (fetcherThread != null) {
            fetcherThread.interrupt();
            fetcherThread = null;
        }
        LOGGER.info("Price data fetcher stopped");
    }

    private void fetchLoop() {
        while (running) {
            try {
                Thread.sleep(FETCH_INTERVAL);

                APIManager apiManager = APIManager.getInstance();
                if (apiManager.getApiKey() == null || apiManager.getApiKey().isEmpty()) {
                    continue;
                }

                apiManager.updateBazaarPrices();

                lastFetchTime = System.currentTimeMillis();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.error("Error in price fetch loop", e);
            }
        }
    }

    public void forceFetch() {
        APIManager apiManager = APIManager.getInstance();
        if (apiManager.getApiKey() == null || apiManager.getApiKey().isEmpty()) return;

        apiManager.updateBazaarPrices();
        lastFetchTime = System.currentTimeMillis();
    }

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public boolean isRunning() {
        return running;
    }
}
