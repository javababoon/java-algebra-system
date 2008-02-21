/*
 * $Id$
 */

package edu.jas.ring;

import java.util.List;


import edu.jas.structure.RegularRingElem;

import edu.jas.poly.GenPolynomial;



/**
 * Polynomial R Reduction interface.
 * Defines additionally boolean closure methods.
 * @author Heinz Kredel
 */

public interface RReduction<C extends RegularRingElem<C>> 
                 extends Reduction<C> {


    /**
     * Is strong top reducible.
     * Condition is idempotent(a) == idempotent(b), 
     * for a=ldcf(A) and b=ldcf(B) and lt(B) | lt(A) for some B in F.
     * @typeparam C coefficient type.
     * @param A polynomial.
     * @param P polynomial list.
     * @return true if A is string top reducible with respect to P.
     */
    public boolean isStrongTopReducible(List<GenPolynomial<C>> P, 
                                        GenPolynomial<C> A);


    /**
     * Is boolean closed, 
     * test if A == idempotent(ldcf(A)) A.
     * @typeparam C coefficient type.
     * @param A polynomial.
     * @return true if A is boolean closed, else false.
     */
     public boolean isBooleanClosed(GenPolynomial<C> A);


    /**
     * Is boolean closed, 
     * test if all A in F are boolean closed.
     * @typeparam C coefficient type.
     * @param F polynomial list.
     * @return true if F is boolean closed, else false.
     */
    public boolean isBooleanClosed(List<GenPolynomial<C>> F);


    /**
     * Boolean closure, 
     * compute idempotent(ldcf(A)) A.
     * @typeparam C coefficient type.
     * @param A polynomial.
     * @return bc(A).
     */
    public GenPolynomial<C> booleanClosure(GenPolynomial<C> A);


    /**
     * Boolean remainder, 
     * compute idemComplement(ldcf(A)) A.
     * @typeparam C coefficient type.
     * @param A polynomial.
     * @return br(A) = A - bc(A).
     */
    public GenPolynomial<C> booleanRemainder(GenPolynomial<C> A);


    /**
     * Reduced boolean closure, 
     * compute BC(A) for all A in F.
     * @typeparam C coefficient type.
     * @param F polynomial list.
     * @return red(bc(F)) = bc(red(F)).
     */
    public List<GenPolynomial<C>> reducedBooleanClosure(List<GenPolynomial<C>> F);


    /**
     * Reduced boolean closure, 
     * compute BC(A) modulo F.
     * @typeparam C coefficient type.
     * @param A polynomial.
     * @param F polynomial list.
     * @return red(bc(A)).
     */
    public List<GenPolynomial<C>> reducedBooleanClosure(List<GenPolynomial<C>> F,
                                                        GenPolynomial<C> A);


}
