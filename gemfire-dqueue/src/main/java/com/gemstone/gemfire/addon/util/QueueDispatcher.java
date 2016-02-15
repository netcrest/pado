package com.gemstone.gemfire.addon.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Dae Song Park
 * @since 1.0
 */
public class QueueDispatcher
{
    private List list = Collections.synchronizedList(new LinkedList());
    private QueueDispatcherListener queueDispatcherListener;
    private ConsumerThread consumerThread;
    private boolean terminated = true;
    private boolean paused = false;

    public QueueDispatcher()
    {
    }

    public synchronized void start()
    {
        if (consumerThread == null) {
            consumerThread = new ConsumerThread();
            consumerThread.setDaemon(true);
            consumerThread.start();
            terminated = false;
        }
    }

    public synchronized void stop()
    {
        if (consumerThread != null) {
            consumerThread.terminate();
            consumerThread = null;
        }
    }
    
    public void pause()
    {
    	paused = true;
    }
    
    public void resume()
    {
    	paused = false;
    }

    public synchronized void enqueue(Object obj)
    {
        list.add(obj);
        if (paused == false) {
        	this.notify();
        }
    }

    public synchronized Object dequeue() throws InterruptedException
    {
        return dequeue(0);
    }

    public synchronized Object dequeue(long timeout) throws InterruptedException
    {
        while (paused || list.size() == 0) {
            this.wait(timeout);
        }
        if (paused || list.size() == 0) {
            return null;
        } else {
            return list.remove(0);
        }
    }
    
    public int size()
    {
        return list.size();
    }

    public boolean isEmpty()
    {
        return list.size() == 0;
    }
    
    /**
     * Clears the queue and returns the last object in the queue
     */
    public synchronized Object clear()
    {
    	Object obj = null;
    	if (list.size() > 0) {
    		obj = list.get(list.size() - 1);
    	}
    	list.clear();
    	return obj;
    }
    
    public boolean isTerminated()
    {
    	return terminated;
    }

    public void setQueueDispatcherListener(QueueDispatcherListener listener)
    {
        this.queueDispatcherListener = listener;
    }

    public QueueDispatcherListener getQueueDispatcherListener()
    {
        return queueDispatcherListener;
    }

    class ConsumerThread extends Thread
    {
        private boolean shouldRun = true;

        ConsumerThread()
        {
        	super("QueueDispatcher.ConsumerThread");
        	setDaemon(true);
        }
        
        public void run()
        {
            while (shouldRun) {
                try {
                    Object obj = dequeue(1000);
                    if (obj != null && queueDispatcherListener != null) {
                        queueDispatcherListener.objectDispatched(obj);
                    }
                } catch (InterruptedException ex) {
                    // ignore for the time being
                }
            }
            terminated = true;
            clear();
        }

        public void terminate()
        {
            shouldRun = false;
        }
    }
}
