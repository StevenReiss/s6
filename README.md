S6 is a semantic search engine.  It requires IVY to build.



To build (assume this directory is $ROOT/s6)

1)  be running csh (or tcsh)
2)  ensure ivy is setup and compiled.
3)  source $ROOT/ivy/bin/ivysetupenv if now already done

4)  cd to $ROOT/s6/bin
5)  source s6setupenv
6)  cd to $ROOT/s6 (this directory)
7)  make newmachine
8)  make
