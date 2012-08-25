/*
 * $Id$
 */

package edu.jas.poly;


import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.math.BigInteger;

import edu.jas.kern.StringUtil;
import edu.jas.structure.MonoidElem;
import edu.jas.structure.MonoidFactory;


/**
 * WordFactory implements alphabet related methods. 
 * @author Heinz Kredel
 */

public class WordFactory implements MonoidFactory<Word> {


    /**
     * The data structure is a String of characters which defines the alphabet.
     */
    /*package*/ final String alphabet;


    /**
     * The empty word for this monoid.
     */
    public final Word ONE;


    /**
     * Random number generator.
     */
    private final static Random random = new Random();


    /**
     * Constructor for WordFactory.
     */
    public WordFactory() {
        this("");
    }


    /**
     * Constructor for WordFactory.
     * @param s String for alphabet
     */
    public WordFactory(String s) {
        if (s == null) {
            throw new IllegalArgumentException("null string not allowed");
        }
        alphabet = clean(s);
        ONE = new Word(this);
    }


    /**
     * Is this structure finite or infinite.
     * @return true if this structure is finite, else false.
     * @see edu.jas.structure.ElemFactory#isFinite() <b>Note: </b> returns true
     *      because of finite String value.
     */
    public boolean isFinite() {
        return false;
    }


    /**
     * Query if this monoid is commutative.
     * @return true if this monoid is commutative, else false.
     */
    public boolean isCommutative() {
        return false;
    }


    /**
     * Query if this monoid is associative.
     * @return true if this monoid is associative, else false.
     */
    public boolean isAssociative() {
        return true;
    }


    /**
     * Get the one element, the empty word.
     * @return 1 as Word.
     */
    public Word getONE() {
        return ONE;
    }


    /**
     * Copy word.
     */
    @Override
    public Word copy(Word w) {
        return new Word(this,w.getVal()); 
    }


    /**
     * Get the alphabet String.
     * @return alphabet.
     */
    /*package*/ String getVal() {
        return alphabet;
    }


    /**
     * Get the alphabet letter at position i.
     * @param i position.
     * @return val[i].
     */
    public char getVal(int i) {
        return alphabet.charAt(i);
    }


    /**
     * Get the string representation.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer("WordFactory(");
        for (int i = 0; i < alphabet.length(); i++) {
            if (i != 0) {
                s.append("*");
            }
            s.append(getVal(i));
        }
        s.append(")");
        return s.toString();
    }


    /**
     * Get a scripting compatible string representation.
     * @return script compatible representation for this Element.
     * @see edu.jas.structure.Element#toScript()
     */
    //JAVA6only: @Override
    public String toScript() {
        return toString();
    }


    /**
     * Comparison with any other object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof WordFactory)) {
            return false;
        }
        WordFactory b = (WordFactory) B;
        return alphabet.equals(b.alphabet);
    }


    /**
     * hashCode. 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return alphabet.hashCode();
    }


    /**
     * Get a list of the generating elements.
     * @return list of generators for the algebraic structure.
     */
    public List<Word> generators() {
        int len = alphabet.length();
        List<Word> gens = new ArrayList<Word>(len+1);
        gens.add(ONE);
        for ( int i = 0; i < len; i++ ) {
	    Word w = new Word(this, String.valueOf(alphabet.charAt(i)) );
            gens.add(w);
        }
        return gens;
    }


    /**
     * Get the Element for a.
     * @param a long
     * @return element corresponding to a.
     */
    public Word fromInteger(long a) {
        throw new UnsupportedOperationException("not implemented for WordFactory");
    }


    /**
     * Get the Element for a.
     * @param a java.math.BigInteger.
     * @return element corresponding to a.
     */
    public Word fromInteger(BigInteger a) {
        throw new UnsupportedOperationException("not implemented for WordFactory");
    }


    /**
     * Generate a random Element with size less equal to n.
     * @param n
     * @return a random element.
     */
    public Word random(int n) {
        return random(n,random);
    }


    /**
     * Generate a random Element with size less equal to n.
     * @param n
     * @param random is a source for random bits.
     * @return a random element.
     */
    public Word random(int n, Random random) {
        StringBuffer sb = new StringBuffer();
        int len = alphabet.length();
        for ( int i = 0; i < n; i++ ) {
            int r = random.nextInt();
            if ( r < 0 ) {
                r = -r;
            }
            r = r % len;
	    sb.append( alphabet.charAt(r) );
        }
        return new Word(this,sb.toString());
    }


    /**
     * Parse from String.
     * @param s String.
     * @return a Element corresponding to s.
     */
    public static String clean(String s) {
        String st = s.trim();
        st = st.replaceAll("\\*","");
        st = st.replaceAll("\\s","");
        st = st.replaceAll("\\(","");
        st = st.replaceAll("\\)","");
        return st;
    }


    /**
     * Parse from String.
     * @param s String.
     * @return a Element corresponding to s.
     */
    public Word parse(String s) {
        String st = clean(s);
        String regex = "[" + alphabet + "]*";
        if ( ! st.matches( regex ) ) {
            throw new IllegalArgumentException("word '" + st + "' contains letters not from: " + alphabet);
	}
        return new Word(this,st);
    }


    /**
     * Parse from Reader.
     * White space is delimiter for word.
     * @param r Reader.
     * @return the next Element found on r.
     */
    public Word parse(Reader r) {
        return parse( StringUtil.nextString(r) );
    }

}