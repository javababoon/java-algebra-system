/*
 * $Id$
 */

package edu.jas.root;


import java.util.List;

import edu.jas.arith.BigRational;
import edu.jas.poly.GenPolynomial;
import edu.jas.structure.Complex;
import edu.jas.structure.RingElem;


/**
 * Complex roots interface.
 * @param <C> coefficient type.
 * @author Heinz Kredel
 */
public interface ComplexRoots<C extends RingElem<C>> {


    /**
     * Root bound. With f(M) * f(-M) != 0.
     * @param f univariate polynomial.
     * @return M such that -M &lt; root(f) &lt; M.
     */
    public Complex<C> rootBound(GenPolynomial<Complex<C>> f);


    /**
     * Complex root count of complex polynomial on rectangle.
     * @param rect rectangle.
     * @param a univariate complex polynomial.
     * @return root count of a in rectangle.
     */
    public long complexRootCount(Rectangle<C> rect, GenPolynomial<Complex<C>> a);


    /**
     * List of complex roots of complex polynomial a on rectangle.
     * @param rect rectangle.
     * @param a univariate squarefree complex polynomial.
     * @return list of complex roots.
     */
    public List<Rectangle<C>> complexRoots(Rectangle<C> rect, GenPolynomial<Complex<C>> a);


    /**
     * List of complex roots of complex polynomial.
     * @param a univariate complex polynomial.
     * @return list of complex roots.
     */
    public List<Rectangle<C>> complexRoots(GenPolynomial<Complex<C>> a);


    /**
     * Complex root refinement of complex polynomial a on rectangle.
     * @param rect rectangle containing exactly one complex root.
     * @param a univariate squarefree complex polynomial.
     * @param len rational length for refinement.
     * @return refined complex root.
     */
    public Rectangle<C> complexRootRefinement(Rectangle<C> rect, GenPolynomial<Complex<C>> a, BigRational len);

}
