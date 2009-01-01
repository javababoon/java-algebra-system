/*
 * $Id$
 */

package edu.jas.ufd;


import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.PolyUtil;


/**
 * Greatest common divisor algorithms.
 * @author Heinz Kredel
 */

public abstract class GreatestCommonDivisorAbstract<C extends GcdRingElem<C>> implements
        GreatestCommonDivisor<C> {


    private static final Logger logger = Logger
            .getLogger(GreatestCommonDivisorAbstract.class);


    private final boolean debug = logger.isDebugEnabled();


    /**
     * GenPolynomial base coefficient content.
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public C baseContent(GenPolynomial<C> P) {
        if ( P == null ) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if ( P.isZERO() ) {
            return P.ring.getZEROCoefficient();
        }
        C d = null;
        for ( C c : P.getMap().values() ) {
            if ( d == null ) {
                d = c;
            } else {
                d = d.gcd(c);
            }
            if ( d.isONE() ) {
                return d;
            }
        }
        return d.abs();
    }


    /**
     * GenPolynomial base coefficient primitive part.
     * @param P GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<C> basePrimitivePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        C d = baseContent(P);
        if (d.isONE()) {
            return P;
        }
        GenPolynomial<C> pp = P.divide(d);
        if (debug) {
            GenPolynomial<C> p = pp.multiply(d);
            if (!p.equals(P)) {
                throw new RuntimeException("pp(p)*cont(p) != p: ");
            }
        }
        return pp;
    }


    /**
     * Univariate GenPolynomial greatest common divisor. Uses sparse
     * pseudoRemainder for remainder.
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P,S).
     */
    public abstract GenPolynomial<C> baseGcd(GenPolynomial<C> P, GenPolynomial<C> S);


    /**
     * GenPolynomial polynomial greatest squarefee divisor.
     * @param P GenPolynomial.
     * @return squarefree(pp(P)).
     */
    public GenPolynomial<C> baseSquarefreePart(GenPolynomial<C> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        GenPolynomial<C> pp = basePrimitivePart(P);
        if ( pp.isConstant() ) {
            return pp;
        }
        GenPolynomial<C> d;
        long k = pp.degree(0);
        long j = 1;
        while ( true ) { 
            d = PolyUtil.<C> baseDeriviative(pp);
            //System.out.println("d = " + d);
            if ( !d.isZERO() ) { // || pp.isConstant()
                break;
            }
            long mp = pfac.characteristic().longValue(); // assert != 0
            pp = PolyUtil.<C> baseModRoot(pp,mp);
            j = j * mp;
            if ( j > k ) {
               throw new RuntimeException("polynomial mod " + mp + ", pp = " + pp + ", d = " + d);
            }
        } 
        GenPolynomial<C> g = baseGcd(pp, d);
        GenPolynomial<C> q = PolyUtil.<C> basePseudoDivide(pp, g);
        return q;
    }


    /**
     * GenPolynomial polynomial squarefee factorization.
     * @param A primitive! GenPolynomial (and monic if computing mod p) .
     * @return [e_1 -> p_1, ..., e_k -> p_k ] with P = prod_{i=1,...,k} p_k^{e_k} and p_i squarefree.
     */
    public SortedMap<Integer, GenPolynomial<C>> baseSquarefreeFactors(GenPolynomial<C> A) {
        SortedMap<Integer, GenPolynomial<C>> sfactors = new TreeMap<Integer, GenPolynomial<C>>();
        if ( A == null || A.isZERO() ) {
            return sfactors;
        }
        if ( A.isConstant() ) {
            sfactors.put(1,A);
            return sfactors;
        }
        GenPolynomialRing<C> pfac = A.ring;
        if ( pfac.nvar > 1 ) {
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        if ( pfac.characteristic().signum() > 0 && !A.leadingBaseCoefficient().isONE() ) {
            throw new RuntimeException("A mod p not monic");
        }
        GenPolynomial<C> T0 = A;
        long e = 1L;
        GenPolynomial<C> Tp;
        GenPolynomial<C> T = null;
        GenPolynomial<C> V = null;
        long k = 0L;
        long mp = 0L;
        boolean init = true;
        while ( true ) { 
            if ( init ) {
                if ( T0.isConstant() || T0.isZERO() ) {
                     break;
                }
                Tp = PolyUtil.<C> baseDeriviative(T0);
                T = baseGcd(T0,Tp);
                V = PolyUtil.<C> basePseudoDivide(T0,T);
                //System.out.println("Tp = " + Tp);
                //System.out.println("T = " + T);
                System.out.println("V = " + V);
                k = 0L;
                mp = 0L;
                init = false;
            }
            if ( V.isConstant() ) { 
                mp = pfac.characteristic().longValue(); // assert != 0
                T0 = PolyUtil.<C> baseModRoot(T,mp);
                System.out.println("T0 = " + T0);
                e = e * mp;
                init = true;
                continue;
            }
            k++;
            if ( mp != 0L && k % mp == 0L ) {
                T = PolyUtil.<C> basePseudoDivide(T, V);
                System.out.println("k = " + k);
                //System.out.println("T = " + T);
                k++;
            }
            GenPolynomial<C> W = baseGcd(T,V);
            GenPolynomial<C> z = PolyUtil.<C> basePseudoDivide(V, W);
            //System.out.println("W = " + W);
            //System.out.println("z = " + z);
            V = W;
            T = PolyUtil.<C> basePseudoDivide(T, V);
            //System.out.println("V = " + V);
            //System.out.println("T = " + T);
            if ( z.degree(0) > 0 ) {
                sfactors.put((int)(e*k), z);
            }
        }
        return sfactors;
    }


    /**
     * GenPolynomial recursive content.
     * @param P recursive GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> recursiveContent(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P.ring.getZEROCoefficient();
        }
        GenPolynomial<C> d = null;
        for (GenPolynomial<C> c : P.getMap().values()) {
            if (d == null) {
                d = c;
            } else {
                d = gcd(d, c); // go to recursion
            }
            if (d.isONE()) {
                return d;
            }
        }
        return d.abs();
    }


    /**
     * GenPolynomial recursive primitive part.
     * @param P recursive GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<GenPolynomial<C>> recursivePrimitivePart(
            GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomial<C> d = recursiveContent(P);
        if (d.isONE()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> pp = PolyUtil.<C> recursiveDivide(P, d);
        return pp;
    }


    /**
     * GenPolynomial recursive greatest common divisor. Uses
     * pseudoRemainder for remainder.
     * @param P recursive GenPolynomial.
     * @param S recursive GenPolynomial.
     * @return gcd(P,S).
     */
    public GenPolynomial<GenPolynomial<C>> recursiveGcd(
           GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S) {
        if ( S == null || S.isZERO() ) {
            return P;
        }
        if ( P == null || P.isZERO() ) {
            return S;
        }
        if ( P.ring.nvar <= 1 ) {
            return recursiveUnivariateGcd( P, S );
        }
        // distributed polynomials gcd
        GenPolynomialRing<GenPolynomial<C>> rfac = P.ring;
        RingFactory<GenPolynomial<C>> rrfac = rfac.coFac;
        GenPolynomialRing<C> cfac = (GenPolynomialRing<C>)rrfac;
        GenPolynomialRing<C> dfac = cfac.extend( rfac.nvar );
        GenPolynomial<C> Pd = PolyUtil.<C> distribute(dfac, P);
        GenPolynomial<C> Sd = PolyUtil.<C> distribute(dfac, S);
        GenPolynomial<C> Dd = gcd(Pd,Sd);
        // convert to recursive
        GenPolynomial<GenPolynomial<C>> C = PolyUtil.<C> recursive(rfac, Dd);
        return C;
    }


    /**
     * Univariate GenPolynomial recursive greatest common divisor. Uses
     * pseudoRemainder for remainder.
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P,S).
     */
    public abstract GenPolynomial<GenPolynomial<C>> recursiveUnivariateGcd(
            GenPolynomial<GenPolynomial<C>> P, GenPolynomial<GenPolynomial<C>> S);


    /**
     * GenPolynomial recursive polynomial greatest squarefee divisor.
     * @param P recursive GenPolynomial.
     * @return squarefree(pp(P)).
     */
    public GenPolynomial<GenPolynomial<C>> 
       recursiveSquarefreePart( GenPolynomial<GenPolynomial<C>> P ) {
        if ( P == null || P.isZERO() ) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if ( pfac.nvar > 1 ) {
            throw new RuntimeException(this.getClass().getName()
                    + " only for multivariate polynomials");
        }
        // squarefree content
        GenPolynomial<GenPolynomial<C>> pp = P;
        GenPolynomial<C> Pc = recursiveContent(P);
        if ( ! Pc.isONE() ) {
           Pc = squarefreePart(Pc);
           pp = pp.divide(Pc);
        }
        if ( pp.leadingExpVector().getVal(0) < 1 ) {
            return pp.multiply(Pc);
        }
        // mod p case
        GenPolynomial<GenPolynomial<C>> d;
        while ( true ) { 
            d = PolyUtil.<C>recursiveDeriviative(pp);
            //System.out.println("d = " + d);
            if ( !d.isZERO() ) { // || pp.isConstant()
                break;
            }
            int mp = pfac.characteristic().intValue(); // assert != 0
            pp = PolyUtil.<C> recursiveModRoot(pp,mp);
        } 
        // now d != 0
        GenPolynomial<GenPolynomial<C>> g = recursiveUnivariateGcd(pp, d);
        GenPolynomial<GenPolynomial<C>> q = PolyUtil.<C> recursivePseudoDivide(pp, g);
        return q.multiply(Pc);
    }


    /**
     * GenPolynomial recursive polynomial squarefee factorization.
     * @param P primitive recursive GenPolynomial.
     * @return squarefreeFactors(P).
     */
    public SortedMap<Integer, GenPolynomial<GenPolynomial<C>>> 
      recursiveSquarefreeFactors( GenPolynomial<GenPolynomial<C>> P ) {
        SortedMap<Integer, GenPolynomial<GenPolynomial<C>>> sfactors = new TreeMap<Integer, GenPolynomial<GenPolynomial<C>>>();
        if (P == null || P.isZERO()) {
            return sfactors;
        }
        if ( P.isConstant() ) {
            sfactors.put(1,P);
            return sfactors;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // recursiveContent not possible by return type
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }

        // factors of content
        GenPolynomial<C> Pc = recursiveContent(P);
        SortedMap<Integer, GenPolynomial<C>> rsf = squarefreeFactors(Pc);
        //System.out.println("rsf = " + rsf);

        GenPolynomial<GenPolynomial<C>> pp = P;
        GenPolynomial<GenPolynomial<C>> d;
        int j = 1;
        while ( true ) { 
            d = PolyUtil.<C>recursiveDeriviative(pp);
            //System.out.println("d = " + d);
            if ( !d.isZERO() ) { // || pp.isConstant()
                break;
            }
            int mp = pfac.characteristic().intValue(); // assert != 0
            pp = PolyUtil.<C> recursiveModRoot(pp,mp);
            j = j * mp;
        } 
        GenPolynomial<GenPolynomial<C>> g = recursiveUnivariateGcd(pp, d);
        GenPolynomial<GenPolynomial<C>> q = PolyUtil.<C> recursivePseudoDivide(pp, g);
        //GenPolynomial<GenPolynomial<C>> y = PolyUtil.<C>recursivePseudoDivide(d,g);
        while (g.leadingExpVector().getVal(0) >= 1 /*!g.abs().isONE()*/) {
            GenPolynomial<GenPolynomial<C>> c = recursiveUnivariateGcd(g, q);
            GenPolynomial<GenPolynomial<C>> z = PolyUtil.<C> recursivePseudoDivide(q, c);
            if (z.leadingExpVector().getVal(0) > 0 /*! z.isONE()*/) {
                sfactors.put(j, z);
            }
            j++;
            q = c;
            g = PolyUtil.<C> recursivePseudoDivide(g, c);
        }
        sfactors.put(j, q);
        // add factors of content
        for (Integer k : rsf.keySet()) {
            GenPolynomial<GenPolynomial<C>> c = sfactors.get(k);
            if (c == null) {
                c = pfac.getONE();
            }
            c = c.multiply(rsf.get(k));
            sfactors.put(k, c);
        }
        return sfactors;
    }


    /**
     * GenPolynomial content.
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> content(GenPolynomial<C> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            // baseContent not possible by return type
            throw new RuntimeException(this.getClass().getName()
                    + " use baseContent for univariate polynomials");

        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>(
                cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        GenPolynomial<C> D = recursiveContent(Pr);
        return D;
    }


    /**
     * GenPolynomial primitive part.
     * @param P GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<C> primitivePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return basePrimitivePart(P);
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>(
                cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        GenPolynomial<GenPolynomial<C>> PP = recursivePrimitivePart(Pr);

        GenPolynomial<C> D = PolyUtil.<C> distribute(pfac, PP);
        return D;
    }


    /**
     * GenPolynomial division. Indirection to GenPolynomial method.
     * @param a GenPolynomial.
     * @param b coefficient.
     * @return a/b.
     */
    public GenPolynomial<C> divide(GenPolynomial<C> a, C b) {
        if (b == null || b.isZERO()) {
            throw new RuntimeException(this.getClass().getName() + " division by zero");

        }
        if (a == null || a.isZERO()) {
            return a;
        }
        return a.divide(b);
    }


    /**
     * Coefficient greatest common divisor. Indirection to coefficient method.
     * @param a coefficient.
     * @param b coefficient.
     * @return gcd(a,b).
     */
    public C gcd(C a, C b) {
        if (b == null || b.isZERO()) {
            return a;
        }
        if (a == null || a.isZERO()) {
            return b;
        }
        return a.gcd(b);
    }


    /**
     * GenPolynomial greatest common divisor.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P,S).
     */
    public GenPolynomial<C> gcd(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            GenPolynomial<C> T = baseGcd(P, S);
            return T;
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>( cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        GenPolynomial<GenPolynomial<C>> Sr = PolyUtil.<C> recursive(rfac, S);
        GenPolynomial<GenPolynomial<C>> Dr = recursiveUnivariateGcd(Pr, Sr);
        GenPolynomial<C> D = PolyUtil.<C> distribute(pfac, Dr);
        return D;
    }


    /**
     * GenPolynomial least common multiple.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return lcm(P,S).
     */
    public GenPolynomial<C> lcm(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomial<C> C = gcd(P, S);
        GenPolynomial<C> A = P.multiply(S);
        return PolyUtil.<C> basePseudoDivide(A, C);
    }


    /**
     * GenPolynomial greatest squarefree divisor.
     * @param P GenPolynomial.
     * @return squarefree(pp(P)).
     */
    public GenPolynomial<C> squarefreePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseSquarefreePart(P);
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>(
                cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        GenPolynomial<C> Pc = recursiveContent(Pr);
        GenPolynomial<C> Ps = squarefreePart(Pc);
        GenPolynomial<GenPolynomial<C>> PP = recursiveSquarefreePart(Pr);
        GenPolynomial<GenPolynomial<C>> PS = PP.multiply(Ps);
        GenPolynomial<C> D = PolyUtil.<C> distribute(pfac, PS);
        return D;
    }


    /**
     * GenPolynomial squarefree factorization.
     * @param P primitive GenPolynomial.
     * @return squarefreeFactors(P).
     */
    public SortedMap<Integer, GenPolynomial<C>> squarefreeFactors(GenPolynomial<C> P) {
        if (P == null) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseSquarefreeFactors(P);
        }
        SortedMap<Integer, GenPolynomial<C>> sfactors = new TreeMap<Integer, GenPolynomial<C>>();
        if (P.isZERO()) {
            return sfactors;
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>(
                cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        SortedMap<Integer, GenPolynomial<GenPolynomial<C>>> PP = recursiveSquarefreeFactors(Pr);

        for (Map.Entry<Integer, GenPolynomial<GenPolynomial<C>>> m : PP.entrySet()) {
            Integer i = m.getKey();
            GenPolynomial<GenPolynomial<C>> Dr = m.getValue();
            GenPolynomial<C> D = PolyUtil.<C> distribute(pfac, Dr);
            sfactors.put(i, D);
        }
        return sfactors;
    }


    /**
     * GenPolynomial resultant.
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P,S).
     */
    public GenPolynomial<C> resultant(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        // hack
        GreatestCommonDivisorSubres<C> ufd_sr = new GreatestCommonDivisorSubres<C>();
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            GenPolynomial<C> T = ufd_sr.baseResultant(P, S);
            return T;
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<GenPolynomial<C>>(
                cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.<C> recursive(rfac, P);
        GenPolynomial<GenPolynomial<C>> Sr = PolyUtil.<C> recursive(rfac, S);

        GenPolynomial<GenPolynomial<C>> Dr = ufd_sr.recursiveResultant(Pr, Sr);
        GenPolynomial<C> D = PolyUtil.<C> distribute(pfac, Dr);
        return D;
    }

}
