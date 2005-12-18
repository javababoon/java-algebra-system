/*
 * $Id$
 */

package edu.jas.util;

/**
 * StrategyEnumeration.
 * This class names possible / implemented strategies.
 * @author Heinz Kredel.
 */

public final class StrategyEnumeration {

    public static final StrategyEnumeration FIFO = new StrategyEnumeration();

    public static final StrategyEnumeration LIFO = new StrategyEnumeration();

    private StrategyEnumeration() { }

/**
 * toString.
 */    
    public String toString() {
        if (this == FIFO) {
           return "FIFO strategy";
        } else {
           return "LIFO strategy";
        }
    }

}
