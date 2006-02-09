#
# jython examples for jas.

from jas import SolvableRing
from jas import SolvableIdeal

from edu.jas.ring   import SolvableGroebnerBaseSeq;
from edu.jas.poly   import OrderedPolynomialList;
from edu.jas.module import ModuleList;
from edu.jas.module import SolvableSyzygy;

# WA_32 example

rs = """
# solvable polynomials, Weyl algebra A_3,2:
Rat(a,b,e1,e2,e3) L
RelationTable
(
 ( e3 ), ( e1 ), ( e1 e3 - e1 ),
 ( e3 ), ( e2 ), ( e2 e3 - e2 )
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( e1 e3^3 + e2^10 - a ),
 ( e1^3 e2^2 + e3 ),
 ( e3^3 + e3^2 - b )
)
""";

f = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(f);
print;


Z = SolvableSyzygy().leftZeroRelationsArbitrary( f.list );
#Z = SolvableSyzygy().leftZeroRelations( g );
Zp = ModuleList( r.ring, Z );
print "seq left syz Output:", Zp;
print;
if SolvableSyzygy().isLeftZeroRelation( Zp.list, f.list ):
   print "is left syzygy";
else:
   print "is not left syzygy";


Zr = SolvableSyzygy().rightZeroRelationsArbitrary( f.list );
#Z = SolvableSyzygy().rightZeroRelations( g );
Zpr = ModuleList( r.ring, Zr );
print "seq right syz Output:", Zpr;
print;
if SolvableSyzygy().isRightZeroRelation( Zpr.list, f.list ):
   print "is right syzygy";
else:
   print "is not right syzygy";




rg = f.leftGB();
print "seq left Output:", rg;
print;
if SolvableGroebnerBaseSeq().isLeftGB( rg.list ):
   print "is left GB";
else:
   print "is not left GB";
g = rg.list;


rg = f.twosidedGB();
print "seq twosided Output:", rg;
print;
if SolvableGroebnerBaseSeq().isTwosidedGB( rg.list ):
   print "is twosided GB";
else:
   print "is not twosided GB";


rgb = SolvableGroebnerBaseSeq().rightGB( f.list );
rp = OrderedPolynomialList( r.ring, rgb );
print "seq right Output:", rp;
print;
if SolvableGroebnerBaseSeq().isRightGB( rgb ):
   print "is right GB";
else:
   print "is not right GB";

