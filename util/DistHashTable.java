/*
 * $Id$
 */

package edu.jas.util;

import java.io.IOException;
import java.util.Iterator;
//import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.unima.ky.parallel.ChannelFactory;
import edu.unima.ky.parallel.SocketChannel;


/**
 * Distributed version of a HashTable.
 * Implemented with a SortedMap / TreeMap to keep the sequence 
 * order of elements.
 * @author Heinz Kredel.
 */

public class DistHashTable /* implements List not jet */ {

    private static Logger logger = Logger.getLogger(DistHashTable.class);

    protected final SortedMap theList;
    protected final ChannelFactory cf;
    protected SocketChannel channel = null;
    protected DHTListener listener = null;


/**
 * Constructs a new DistHashTable
 * @param host Name or IP of server host
 */ 

    public DistHashTable(String host) {
        this(host,DistHashTableServer.DEFAULT_PORT);
    }


    public DistHashTable(String host,int port) {
        this(new ChannelFactory(port+1),host,port);
    }


    public DistHashTable(ChannelFactory cf,String host,int port) {
        this.cf = cf;
        try {
            channel = cf.getChannel(host,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("dl channel = " + channel);
        theList = new TreeMap();
        listener = new DHTListener(channel,theList);
        listener.start();
    }


    public DistHashTable(SocketChannel sc) {
        cf = null;
        channel = sc;
        theList = new TreeMap();
        listener = new DHTListener(channel,theList);
        listener.start();
    }


/**
 * Get the internal list, convert from Collection
 * @fix and @check
 */ 
    public List getList() {
        return new ArrayList( theList.values() );
    }


/**
 * Size of the (local) list
 */ 
    public int size() {
        return theList.size();
    }


/**
 * Put object to the distributed hash table.
 * Blocks until the key value pair is send and received 
 * from the server.
 */ 
    public void putWait(Object key, Object value) {
        put(key,value); // = send
        try {
            synchronized ( theList ) {
               while ( ! value.equals( theList.get(key) ) ) {
                  //System.out.print("#");
                  theList.wait(100);
               }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

/**
 * Put object to the distributed hash table.
 * Returns immediately after sending does not block.
 */ 
    public void put(Object key, Object value) {
        if ( key == null || value == null ) {
           throw new NullPointerException("null keys or values not allowed");
        }
        DHTTransport tc = new DHTTransport(key,value);
        try {
            channel.send(tc);
            //System.out.println("send: "+tc+" @ "+listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/**
 * Get value under key from DHT.
 * Blocks until the object is send and received from the server
 * (actually it blocks until some value under key is received).
 */ 
    public Object getWait(Object key) {
        Object value = null;
        try {
            synchronized ( theList ) {
               value = theList.get(key);
               while ( value == null ) {
                   theList.wait(100);
                   value = theList.get(key);
               }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }


/**
 * Get value under key from DHT.
 * If no value is jet available null is returned. 
 */ 
    public Object get(Object key) {
        synchronized ( theList ) {
           return theList.get(key);
        }
    }


/**
 * Terminate the list thread
 */ 
    public void terminate() {
        if ( cf != null ) {
           cf.terminate();
        }
        if ( channel != null ) {
           channel.close();
        }
        //theList.clear();
        if ( listener == null ) { 
           return;
        }
        logger.debug("terminate " + listener);
        listener.setDone(); 
        try { 
             while ( listener.isAlive() ) {
                     listener.interrupt(); 
                     listener.join(100);
             }
        } catch (InterruptedException unused) { 
        }
        listener = null;
    }


/**
 * Clear the List
 * caveat: must be called on all clients
 */ 
    public synchronized void clear() {
        theList.clear();
    }


/**
 * Is the List empty?
 */ 
    public boolean isEmpty() {
        return theList.isEmpty();
    }


/**
 * List iterator
 */ 
    public Iterator iterator() {
        return theList.keySet().iterator();
        // return theList.values().iterator();
    }

}


/**
 * Thread to comunicate with the list server.
 */

class DHTListener extends Thread {

    private static Logger logger = Logger.getLogger(DHTListener.class);

    private SocketChannel channel;
    private SortedMap theList;
    private boolean goon;


    DHTListener(SocketChannel s, SortedMap list) {
        channel = s;
        theList = list;
    } 


    void setDone() {
        goon = false;
    }


    public void run() {
        Object o;
        DHTTransport tc;
        goon = true;
        while (goon) {
            tc = null;
            o = null;
            try {
                o = channel.receive();
                logger.debug("receive("+o+")");
                if ( this.isInterrupted() ) {
                   goon = false;
                } else {
                   if ( o != null ) {
                      if ( o instanceof DHTTransport ) {
                          tc = (DHTTransport)o;
                          if ( tc.key != null ) {
                             logger.debug("receive, put(" + tc + ")");
                             synchronized ( theList ) {
                                theList.put( tc.key, tc.value );
                                theList.notify();
                             }
                          }
                      }
                   }
                }
            } catch (IOException e) {
                goon = false;
            } catch (ClassNotFoundException e) {
                goon = false;
            }
        }
    }

}
