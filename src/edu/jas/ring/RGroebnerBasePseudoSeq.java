/*
 * $Id$
 */

package edu.jas.ring;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.RingFactory;
import edu.jas.structure.RegularRingElem;

import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;

import edu.jas.ring.OrderedRPairlist;

import edu.jas.ufd.GreatestCommonDivisor;
import edu.jas.ufd.GreatestCommonDivisorAbstract;
import edu.jas.ufd.GCDFactory;


/**
 * Regular ring Groebner Base with pseudo reduction sequential algorithm.
 * Implements R-Groebner bases and GB test.
 * @author Heinz Kredel
 */

public class RGroebnerBasePseudoSeq<C extends RegularRingElem<C>> 
       extends RGroebnerBaseSeq<C>  {


    private static final Logger logger = Logger.getLogger(RGroebnerBasePseudoSeq.class);
    private final boolean debug = logger.isDebugEnabled();


    /**
     * Greatest common divisor engine for coefficient content and primitive parts.
     */
    protected final GreatestCommonDivisorAbstract<C> engine;


    /**
     * Coefficient ring factory.
     */
    protected final RingFactory<C> cofac;


    /**
     * Constructor.
     * @param rf coefficient ring factory.
     */
    public RGroebnerBasePseudoSeq(RingFactory<C> rf) {
        this( new RPseudoReductionSeq<C>(), rf );
    }


    /**
     * Constructor.
     * @param red R-pseuso-Reduction engine
     * @param rf coefficient ring factory.
     * <b>Note:</b> red must be an instance of PseudoReductionSeq.
     */
    public RGroebnerBasePseudoSeq(RReduction<C> red, RingFactory<C> rf) {
        super(red);
        if ( ! (red instanceof RPseudoReductionSeq) ) {
           throw new IllegalArgumentException("red must be a RPseudoReductionSeq");
        }
        cofac = rf;
        engine = (GreatestCommonDivisorAbstract<C>)GCDFactory.getImplementation( rf );
    }


    /**
     * R-Groebner base using pairlist class.
     * @typeparam C coefficient type.
     * @param modv module variable number.
     * @param F polynomial list.
     * @return GB(F) a R-Groebner base of F.
     */
    public List<GenPolynomial<C>> 
             GB( int modv, 
                 List<GenPolynomial<C>> F ) {  
        if ( F == null ) {
           return F;
        }
        /* boolean closure */
        List<GenPolynomial<C>> bcF = red.reducedBooleanClosure(F);
           logger.info("#bcF-#F = " + (bcF.size()-F.size()));
        F = bcF;
        /* normalize input */
        List<GenPolynomial<C>> G = new ArrayList<GenPolynomial<C>>();
        OrderedRPairlist<C> pairlist = null; 
        for ( GenPolynomial<C> p : F ) { 
            if ( !p.isZERO() ) {
               System.out.println("cont(p) = " + engine.baseContent(p));
               p = engine.basePrimitivePart(p); // not monic, no field
               p = p.abs();
               if ( p.isConstant() && p.leadingBaseCoefficient().isFull() ) { 
                  G.clear(); G.add( p );
                  return G; // since boolean closed and no threads are activated
               }
               G.add( p ); //G.add( 0, p ); //reverse list
               if ( pairlist == null ) {
                  pairlist = new OrderedRPairlist<C>( modv, p.ring );
               }
               // putOne not required
               pairlist.put( p );
            }
        }
        if ( G.size() <= 1 ) {
           return G; // since boolean closed and no threads are activated
        }
        /* loop on critical pairs */
        Pair<C> pair;
        GenPolynomial<C> pi;
        GenPolynomial<C> pj;
        GenPolynomial<C> S;
        GenPolynomial<C> D;
        GenPolynomial<C> H;
        List<GenPolynomial<C>> bcH;
        //int len = G.size();
        //System.out.println("len = " + len);
        while ( pairlist.hasNext() ) {
              pair = pairlist.removeNext();
              //System.out.println("pair = " + pair);
              if ( pair == null ) continue; 

              pi = pair.pi; 
              pj = pair.pj; 
              if ( logger.isDebugEnabled() ) {
                 logger.debug("pi    = " + pi );
                 logger.debug("pj    = " + pj );
              }

              // S-polynomial -----------------------
              if ( true ) {
              //if ( pair.getUseCriterion3() ) { // correct ?
              //if ( pair.getUseCriterion4() ) { // correct ? no, not applicable
                  S = red.SPolynomial( pi, pj );
                  //System.out.println("S_d = " + S);
                  if ( S.isZERO() ) {
                      pair.setZero();
                      continue;
                  }
                  if ( logger.isDebugEnabled() ) {
                      logger.debug("ht(S) = " + S.leadingExpVector() );
                  }

                  H = red.normalform( G, S );
                  if ( H.isZERO() ) {
                      pair.setZero();
                      continue;
                  }
                  if ( logger.isDebugEnabled() ) {
                      logger.debug("ht(H) = " + H.leadingExpVector() );
                  }
                  //System.out.println("cont(H) = " + engine.baseContent(H));
                  // must be bc: H = engine.basePrimitivePart(H); 
                  H = H.abs(); // not monic, no field
                  if ( H.isConstant() && H.leadingBaseCoefficient().isFull() ) { 
                     // mostly useless
                     G.clear(); G.add( H );
                     return G; // not boolean closed ok, no threads are activated
                  }
                  if ( logger.isDebugEnabled() ) {
                      logger.debug("H = " + H );
                  }
                  if ( !H.isZERO() ) {
                      logger.info("Sred = " + H);
                      //len = G.size();
                      bcH = red.reducedBooleanClosure(G,H);
                      logger.info("#bcH = " + bcH.size());
                      //G.addAll( bcH );
                      for ( GenPolynomial<C> h: bcH ) {
                          System.out.println("cont(h) = " + engine.baseContent(h));
                          h = engine.basePrimitivePart(h); 
                          h = h.abs(); // monic() not ok, since no field
                          G.add( h );
                          pairlist.put( h );
                      }
                      if ( debug ) {
                         if ( !pair.getUseCriterion3() || !pair.getUseCriterion4() ) {
                            logger.info("H != 0 but: " + pair);
                         }
                      }
                  }
              }
        }
        logger.debug("#sequential list = " + G.size());
        System.out.println("isGB() = " + isGB(G));
        G = minimalGB(G);
        //G = red.irreducibleSet(G);
        logger.info("pairlist #put = " + pairlist.putCount() 
                  + " #rem = " + pairlist.remCount()
                    // + " #total = " + pairlist.pairCount()
                   );
        return G;
    }


    /**
     * Minimal ordered Groebner basis.
     * @typeparam C coefficient type.
     * @param Gp a Groebner base.
     * @return a reduced Groebner base of Gp.
     * @todo use primitivePart
     */
    public List<GenPolynomial<C>> 
                minimalGB(List<GenPolynomial<C>> Gp) {  
        if ( Gp == null || Gp.size() <= 1 ) {
            return Gp;
        }
        // remove zero polynomials
        List<GenPolynomial<C>> G
            = new ArrayList<GenPolynomial<C>>( Gp.size() );
        for ( GenPolynomial<C> a : Gp ) { 
            if ( a != null && !a.isZERO() ) { // always true in GB()
               // already positive a = a.abs();
               G.add( a );
            }
        }
        if ( G.size() <= 1 ) {
           //wg monic do not return G;
        }
        // remove top reducible polynomials
        GenPolynomial<C> a, b;
        List<GenPolynomial<C>> F;
        List<GenPolynomial<C>> bcH;
        F = new ArrayList<GenPolynomial<C>>( G.size() );
        while ( G.size() > 0 ) {
            a = G.remove(0); b = a;
            if ( red.isTopReducible(G,a) || red.isTopReducible(F,a) ) {
               // drop polynomial 
               if ( true || debug ) {
                  List<GenPolynomial<C>> ff;
                  ff = new ArrayList<GenPolynomial<C>>( G );
                  ff.addAll(F);
                  a = red.normalform( ff, a );
                  if ( !a.isZERO() ) {
                     System.out.println("minGB nf(a) != 0 " + a);
                     bcH = red.reducedBooleanClosure(G,a);
                     if ( bcH.size() > 1 ) { // never happend so far
                        System.out.println("minGB not bc: bcH size = " + bcH.size());
                        F.add(b); // do not replace, stay with b
                     } else {
                        //System.out.println("minGB add bc(a): a = " + a + ", bc(a) = " + bcH.get(0));
                        F.add(b); // do not replace, stay with b
                        //F.addAll( bcH );
                     }
                  } else {
                     System.out.println("minGB dropped " + b);
                     F.add(b);
                  }
               }
            } else {
                F.add(a);
            }
        }
        G = F;
        if ( G.size() <= 1 ) {
           // wg monic return G;
        }
        // reduce remaining polynomials
        int len = G.size();
        int el = 0;
        while ( el < len ) {
            a = G.remove(0); b = a;
            //System.out.println("doing " + a.length());
            a = red.normalform( G, a );
            //System.out.println("cont(a) = " + engine.baseContent(a));
            //not bc: a = engine.basePrimitivePart(a); // not a.monic() since no field
            if ( ! red.isBooleanClosed(a) ) {
                System.out.println("minGB not bc: a = " + a + "\n BC(a) = " + red.booleanClosure(a) + ", BR(a) = " + red.booleanRemainder(a) );
            }
            bcH = red.reducedBooleanClosure(G,a);
            if ( bcH.size() > 1 ) {
               System.out.println("minGB not bc: bcH size = " + bcH.size());
               G.add( b ); // do not reduce
            } else {
               //G.addAll( bcH );
               G.add( b ); // do not reduce
               for ( GenPolynomial<C> h: bcH ) {
                   System.out.println("cont(h) = " + engine.baseContent(h));
                   h = engine.basePrimitivePart(h); 
                   h = h.abs(); // monic() not ok, since no field
                   //G.add( h );
               }
            }
            el++;
        }
        // make abs if possible
        F = new ArrayList<GenPolynomial<C>>( G.size() );
        for ( GenPolynomial<C> p : G ) {
            a = p.abs();
            F.add( a );
        }
        G = F;

        if ( true ) {
           return G;
        }

        /* stratify: collect polynomials with equal leading terms */
        ExpVector e, f;
        F = new ArrayList<GenPolynomial<C>>( G.size() );
        for ( int i = 0; i < G.size(); i++ ) {
            a = G.get(i);
            if ( a == null || a.isZERO() ) {
               continue;
            }
            e = a.leadingExpVector();
            for ( int j = i+1; j < G.size(); j++ ) {
                b = G.get(j);
                if ( b == null || b.isZERO() ) {
                   continue;
                }
                f = b.leadingExpVector();
                if ( e.equals(f) ) {
                   //System.out.println("minGB e == f: " + a + ", " + b);
                   a = a.sum(b);
                   G.set(j,null);
                }
            }
            F.add( a );
        }
        G = F;

        /* info on boolean algebra element blocks 
        Map<C,List<GenPolynomial<C>>> bd = new TreeMap<C,List<GenPolynomial<C>>>();
        for ( GenPolynomial<C> p : G ) { 
            C cf = p.leadingBaseCoefficient();
            cf = cf.idempotent();
            List<GenPolynomial<C>> block = bd.get( cf );
            if ( block == null ) {
               block = new ArrayList<GenPolynomial<C>>();
            }
            block.add( p ); 
            bd.put( cf, block );
        }
        System.out.println("\nminGB bd:");
        for( C k: bd.keySet() ) {
           System.out.println("\nkey = " + k + ":");
           System.out.println("val = " + bd.get(k));
        }
        System.out.println();
        */
        return G;
    }

}
