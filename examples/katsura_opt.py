#
# jython examples for jas.
# $Id$
#

import sys;

from jas import Ring, PolyRing, QQ, GF
from jas import startLog

# example katsura, optimize term order 
# optimal is (u1, u2, u3, u4, u5, u6, u0, u7)
# better is (u7,u6,u5,u4,u3,u2,u1,u0)

#r = Ring( "Mod 13 (u0,u1,u2,u3,u4,u5,u6,u7) G" );
#r = Ring( "Rat (u7,u6,u5,u4,u3,u2,u1,u0) G" );
r = Ring( "Mod 5 (u7,u6,u5,u4,u3,u2,u1,u0) G" );
print "Ring: " + str(r);
print;

ps = """
(
u7*u7 + u6*u6 + u5*u5 + u4*u4 + u3*u3 + u2*u2 + u1*u1 + u0*u0 + u1*u1 + u2*u2 + u3*u3 + u4*u4 + u5*u5 + u6*u6 + u7*u7 - u0,
u7*0 + u6*u7 + u5*u6 + u4*u5 + u3*u4 + u2*u3 + u1*u2 + u0*u1 + u1*u0 + u2*u1 + u3*u2 + u4*u3 + u5*u4 + u6*u5 + u7*u6 - u1,
u7*0 + u6*0 + u5*u7 + u4*u6 + u3*u5 + u2*u4 + u1*u3 + u0*u2 + u1*u1 + u2*u0 + u3*u1 + u4*u2 + u5*u3 + u6*u4 + u7*u5 - u2,
u7*0 + u6*0 + u5*0 + u4*u7 + u3*u6 + u2*u5 + u1*u4 + u0*u3 + u1*u2 + u2*u1 + u3*u0 + u4*u1 + u5*u2 + u6*u3 + u7*u4 - u3,
u7*0 + u6*0 + u5*0 + u4*0 + u3*u7 + u2*u6 + u1*u5 + u0*u4 + u1*u3 + u2*u2 + u3*u1 + u4*u0 + u5*u1 + u6*u2 + u7*u3 - u4,
u7*0 + u6*0 + u5*0 + u4*0 + u3*0 + u2*u7 + u1*u6 + u0*u5 + u1*u4 + u2*u3 + u3*u2 + u4*u1 + u5*u0 + u6*u1 + u7*u2 - u5,
u7*0 + u6*0 + u5*0 + u4*0 + u3*0 + u2*0 + u1*u7 + u0*u6 + u1*u5 + u2*u4 + u3*u3 + u4*u2 + u5*u1 + u6*u0 + u7*u1 - u6,
u7 + u6 + u5 + u4 + u3 + u2 + u1 + u0 + u1 + u2 + u3 + u4 + u5 + u6 + u7 - 1
)
""";

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

startLog();

o = f.optimize();
print "optimized Ideal: " + str(o);
print;

rg = f.GB();
print "Output:", rg;
print;

#org = o.GB();
#print "opt Output:", org;
#print;

