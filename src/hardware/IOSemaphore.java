package hardware;

/**
 * An IOSemaphore is used for buffered IO.
 * <p/>
 * The special feature of the IOSemaphore is that it can be "closed,"
 * after which no more permits can be released.  When an IOSemaphore is
 * closed, existing permits can be acquired.  Successive attempts
 * to acquire more permits yields an IOSemaphoreClosedException.
 */

public class IOSemaphore {

    private int permits;

    private boolean isClosed;

    public IOSemaphore(int permits) {
        super();
        this.permits = permits;
    }

    public synchronized boolean isClosed() {
        return isClosed;
    }

    public void acquire()  {
        acquire(1);
    }

    /**
     * acquire at least one, and maximum <code>max</code> permits.
     *
     * @param max
     * @return
     * @throws IOSemaphoreClosedException
     * @throws IOSemaphoreClosedException
     */

    public int acquireSome(int max)  {
        return acquireSome(1, max, 0);
    }

    public synchronized int acquireSome(int min, int max, long time)  {

        // the fast track //
        if (permits >= 1) {
            int howmany = Math.min(permits, max);
            permits -= howmany;
            return howmany;
        }

        long now = System.currentTimeMillis();

        long end;
        if (time == 0) {
            end = Long.MAX_VALUE;
        } else {
            end = now + Math.min(time, Long.MAX_VALUE - now);
        }

        while (permits < min) {
            if (isClosed) {
//                throw new IOSemaphoreClosedException(permits);
            }

            now = System.currentTimeMillis();
            long rest = end - now;
            try {
                wait(rest);
            }
            catch (InterruptedException e) {
                // clear interrupted flag
                Thread.interrupted();

            }

        }

        int howmany = Math.min(permits, max);
        permits -= howmany;
        return howmany;
    }

    public void acquire(int i) {
        attempt(i, 0L);
    }

    public synchronized boolean attempt(int i, long time)
           
    {

        // the fast track //
        if (permits >= i) {
            permits -= i;
            return true;
        }

        long now = System.currentTimeMillis();

        long end;
        if (time == 0) {
            end = Long.MAX_VALUE;
        } else {
            end = now + Math.min(time, Long.MAX_VALUE - now);
        }

        while (permits < i) {
            if (isClosed) {
//                throw new IOSemaphoreClosedException(permits);
            }

            now = System.currentTimeMillis();
            long rest = end - now;
            try {
                wait(rest);
            }
            catch (InterruptedException e) {
                // clear interrupted flag
                Thread.interrupted();

            }

        }
        permits -= i;

        return true;
    }

    public void release()  {
        release(1);
    }

    public synchronized void release(int i)  {
        if (isClosed) {
//            throw new IOSemaphoreClosedException(permits);
        }

        permits += i;
        notifyAll();
    }

    public synchronized void close() {
        isClosed = true;
        notifyAll();
    }

    public synchronized int availablePermits() {
        return permits;
    }

    public synchronized void releaseIfNotClosed(int amount) {
        if (!isClosed) {
            permits += amount;
            notifyAll();
        }
    }
}