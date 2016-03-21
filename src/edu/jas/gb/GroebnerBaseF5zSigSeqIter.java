/*
 * $Id$
 */

package edu.jas.gb;


import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.apache.log4j.Logger;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.OrderedPolynomialList;
import edu.jas.poly.PolyUtil;
import edu.jas.structure.RingElem;


/**
 * Groebner Base signatur based sequential iterative
 * algorithm. Implements Groebner bases and GB test.
 * @param <C> coefficient type
 * @author Heinz Kredel
 * 
 * @see edu.jas.application.GBAlgorithmBuilder
 * @see edu.jas.gbufd.GBFactory
 */

public class GroebnerBaseF5zSigSeqIter<C extends RingElem<C>> extends GroebnerBaseSigSeqIter<C> {


    private static final Logger logger = Logger.getLogger(GroebnerBaseF5zSigSeqIter.class);


    private static final boolean debug = logger.isDebugEnabled();


    /**
     * Constructor.
     */
    public GroebnerBaseF5zSigSeqIter() {
        this(new SigReductionSeq<C>());
    }


    /**
     * Constructor.
     * @param red Reduction engine
     */
    public GroebnerBaseF5zSigSeqIter(SigReductionSeq<C> red) {
        super(red);
    }


    /**
     * Top normalform.
     * @param A polynomial.
     * @param F polynomial list.
     * @param G polynomial list.
     * @return nf(A) with respect to F and G.
     */
    public SigPoly<C> sigNormalform(List<GenPolynomial<C>> F, List<SigPoly<C>> G, SigPoly<C> A) {
        return sred.sigSemiNormalform(F, G, A);
    }


    List<SigPair<C>> pruneP(List<SigPair<C>> P, List<ExpVector> syz) {
        List<SigPair<C>> res = new ArrayList<SigPair<C>>(P.size());
        for (SigPair<C> p : P) {
            ExpVector f = p.sigma.leadingExpVector();
            if (f == null) {
                continue;
            }
            boolean div = false;
            for (ExpVector e : syz) {
                if (f.multipleOf(e)) {
                    div = true;
                    break;
                }
            }
            if (div) {
                continue;
            }
            res.add(p);
        }
        return res;
    }

    List<SigPair<C>> pruneS(List<SigPair<C>> S, List<ExpVector> syz, List<SigPoly<C>> done, List<SigPoly<C>> G) {
        List<SigPair<C>> res = new ArrayList<SigPair<C>>(S.size());
        for (SigPair<C> p : S) {
            if (p.sigma.isZERO()) {
                continue;
            }
            ExpVector f = p.sigma.leadingExpVector();
            boolean div = false;
            for (ExpVector e : syz) {
                if (f.multipleOf(e)) {
                    div = true;
                    break;
                }
            }
            if (div) {
                continue;
            }
            ExpVector fi = p.pi.poly.leadingExpVector();
            ExpVector fj = p.pj.poly.leadingExpVector();
            ExpVector fu = fi.lcm(fj).subtract(fi);
            f = p.pi.sigma.leadingExpVector();
            fu = fu.sum(f);
            div = false;
            for (SigPoly<C> q : done) {
                ExpVector e = q.sigma.leadingExpVector();
                if (e == null) {
                    continue;
                }
                if (fu.multipleOf(e)) {
                    if (q.sigma.compareTo(p.pi.sigma) > 0) {
                        div = true;
                        break;
                    }
                }
            }
            if (div) {
                continue;
            }
            res.add(p);
        }
        return res;
    }

    List<ExpVector> initializeSyz(List<GenPolynomial<C>> F, List<SigPoly<C>> G) {
        List<ExpVector> P = new ArrayList<ExpVector>();
        for (GenPolynomial<C> p : F) {
	    if (p.isZERO()) {
                continue;
            }
            P.add( p.leadingExpVector() );
        }
        return P;
    }

    void updateSyz(List<ExpVector> syz, SigPoly<C> r) {
        if (r.poly.isZERO() && !r.sigma.isZERO()) {
	    logger.info("update_syz, sigma = " + r.sigma);   
            syz.add( r.sigma.leadingExpVector() );
        }
        return;
    }

}