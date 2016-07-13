#
# S6
#
# Copyright 2006 Steven P. Reiss, Brown University - All rights reserved.
#
#

SYSTEM= s6
HOME = $(PRO)/$(SYSTEM)
GMAKE= make

VERSION = 1
SUBVER = 0
RCSVERSION = 0.0
SVER= $(VERSION).$(SUBVER)

CS_PRO = /cs/src
CS_PRO_DISTRIB= /cs

INSTALL_TOP= $(PRO)

#
#	Distribution information
#

DISTRIB_MACH= valerie
DISTRIB_TOP= /pro/spr_distrib/files
DISTRIB_DIR= $(DISTRIB_TOP)/$(SYSTEM)
DISTRIB_BIN= /pro/spr_distrib/binary/$(SYSTEM)
DISTRIB= $(DISTRIB_MACH):$(DISTRIB_DIR)
DMAKE= $(MAKE) DISTRIB_BIN=$(DISTRIB_BIN)

SHARED_LIB = /pro/lib

ACTIVE= Makefile


#
#	Component definitions
#

FILES= Makefile

SETUPCOMPS= lib bin data
OTHERCOMPS=

C++_COMPONENTS = runctx
JAVA_COMPONENTS= common request license solution keysearch slim language context runner \
	uiautomator engine search suise tgen
LC++_COMPONENTS = $(C++_COMPONENTS)
APPLE_COMPONENTS = $(C++_COMPONENTS)
UI_COMPONENTS= sviweb


COMPONENTS= $(C++_COMPONENTS) $(JAVA_COMPONENTS)

ALL_COMPONENTS= $(SETUPCOMPS) $(COMPONENTS) $(UI_COMPONENTS) $(OTHERCOMPS)
DIST_COMPONENTS=

#
#	Command definitions
#

GENERIC_COMMANDS= print pribm prps clean newlib create prim rcssetup realclean cleanall
ACTIVE_COMMANDS= all links checkin copyright binshare shareclean count
FULL_COMMANDS= full
DISTRIB_COMMANDS= distrib_dir
C++_COMMANDS= opt prof makedep
JAVA_COMMANDS = xjavadep

#
#	Build rules
#

COPYVALUES= PRO=$(PRO)


default: update

$(ACTIVE_COMMANDS):
	touch .DUMMY
	$(MAKE) $(COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(FULL_COMMANDS):
	touch .DUMMY
	$(MAKE) $(COMPONENTS) $(UI_COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(GENERIC_COMMANDS):
	touch .DUMMY
	$(MAKE) $(ALL_COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(DISTRIB_COMMANDS):
	touch .DUMMY
	$(GMAKE) $(ALL_COMPONENTS) $(DIST_COMPONENTS) 'COMMAND=$@' PRO=$(PRO) \
		'DISTRIB=$(DISTRIB)' 'DISTRIB_MACH=$(DISTRIB_MACH)' \
		'DISTRIB_TOP=$(DISTRIB_TOP)' 'DISTRIB_DIR=$(DISTRIB_DIR)'

$(C++_COMMANDS):
	touch .DUMMY
	$(MAKE) $(C++_COMPONENTS) .DUMMY 'COMMAND=$@' $(COPYVALUES)

$(JAVA_COMMANDS):
	touch .DUMMY
	$(MAKE) $(JAVA_COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

.PHONY: $(GENERIC_COMMANDS) $(ACTIVE_COMMANDS) $(DISTRIB_COMMANDS)

.PHONY: $(ALL_COMPONENTS) $(DIST_COMPONENTS)

$(ALL_COMPONENTS) $(DIST_COMPONENTS): .DUMMY

.DUMMY:

.PRECIOUS: $(ALL_COMPONENTS) $(DIST_COMPONENTS)

$(COMPONENTS) $(DIST_COMPONENTS) $(UI_COMPONENTS):
	(cd $@/src; $(MAKE) $(COMMAND))

$(SETUPCOMPS) $(OTHERCOMPS):
	(cd $@; $(MAKE) $(COMMAND) )

cleanlib:
	(cd lib; $(MAKE) cleanlib)
	$(MAKE) newlib
	(cd lib; $(MAKE) ranlib)

fullclean:
	-rm -rf SB/*
	$(GMAKE) clean
	$(GMAKE) repoclean

fullmake:
	$(GMAKE) fullclean
	$(GMAKE) -k machall

setmachine:
	$(GMAKE) -k machall

wc:
	@rm -rf count.out
	@$(GMAKE) count | tee count.out
	@awk -f ../forest/data/total.awk count.out
	@rm -rf count.out

ui:
	$(GMAKE) machall


distrib:
	rm -rf $(DISTRIB_DIR)
	(cd $(DISTRIB_TOP) ; cvs co -P s6 )
	- (cd $(DISTRIB_DIR) ; find . -name CVS -exec rm -rf {} \; )


olddistrib:
	rm -rf $(DISTRIB_DIR)
	mkdir $(DISTRIB_DIR)
	cp $(ACTIVE) $(DISTRIB_DIR)
	mkdir -p $(DISTRIB_DIR)/java/edu/brown/s6
	mkdir -p $(DISTRIB_DIR)/javasrc/edu/brown/s6
	for x in $(JAVA_COMPONENTS); do \
	   mkdir $(DISTRIB_DIR)/javasrc/edu/brown/s6/$$x; \
	   mkdir $(DISTRIB_DIR)/java/edu/brown/s6/$$x; \
	   mkdir $(DISTRIB_DIR)/$$x ; \
	   ln -s ../javasrc/edu/brown/s6/$$x $(DISTRIB_DIR)/$$x/src ; \
	   ln -s ../java/edu/brown/s6/$$x $(DISTRIB_DIR)/$$x/bin.java ; \
	   ln -s ../../../../../data/Make.pass $(DISTRIB_DIR)/$$x/src/Makefile ; \
	  done
	for x in $(C++_COMPONENTS); do \
	   mkdir $(DISTRIB_DIR)/$$x ; \
	   mkdir $(DISTRIB_DIR)/$$x/src ; \
	   ln -s ../../data/Make.pass $(DISTRIB_DIR)/$$x/src/Makefile ; \
	  done
	$(MAKE) distrib_dir

distribjava:





# REMFLAGS= REMOTE=12
# REMSET= FREEX=$(PRO)/bin/field/freex FIELD_DIR=$(PRO)


#
#	Install scripts
#

INSTALL_BIN= $(INSTALL_TOP)/bin/s6	     # location for s6 binaries
INSTALL_LIB= $(INSTALL_TOP)/lib/s6	       # location for libraries
INSTALL_DAT= $(INSTALL_LIB)/data		# location for s6 data files
INSTALL_USR= $(INSTALL_TOP)/bin

BINFILES=
SCRIPTFILES=
TOPFILES=
UTILBIN=
DATAFILES=
LIBFILES=


shareinstall:
	@echo nothing to do

install:
	$(MAKE) realinstall TOPBIN=$(INSTALL_BIN) TOPLIB=$(INSTALL_LIB) TOPDAT=$(INSTALL_DAT) \
		TOPFRM=$(INSTALL_FRM) TOPBUF=$(INSTALL_BUF) TOPDIR=$(INSTALL_TOP) \
		TOPDATA=$(INSTALL_DATA)

realinstall:
	@echo checking directories
	- if [ ! -d $(TOPBIN) ]; then mkdir $(TOPBIN); else true; fi
	- if [ ! -d $(TOPLIB) ]; then mkdir $(TOPLIB); else true; fi
	- if [ ! -d $(TOPDAT) ]; then mkdir $(TOPDAT); else true; fi
	- if [ ! -d $(TOPBUF) ]; then mkdir $(TOPBUF); else true; fi
	- if [ ! -d $(TOPDATA) ]; then mkdir $(TOPDATA); else true; fi
	@echo copying binaries
	( cd bin/sol; cp $(BINFILES) $(SCRIPTFILES) $(UTILBIN) $(TOPBIN) )
	@echo copying libraries
	( cd lib/$(BROWN_VELD_ARCH); cp $(LIBFILES) $(TOPLIB) )
	@echo copying data files
	( cd lib/sol/data; cp $(DATAFILES) $(TOPDAT) )
	@echo doing configuration
	@echo installation done


jar:
	rm -rf jar.files s6.jar
	(cd java; find s6 -follow -name '*.class' -print | \
		fgrep -v .AppleDouble > ../jar.files )
	(cd java; jar cf ../s6.jar `cat ../jar.files` )
	rm -rf jar.files
	(cd $(PRO)/lib/java; jar uf $(BROWN_WADI_WADI)/s6.jar gnu)
	rcp s6.jar wilma:/u/ftp/u/spr
	rsh wilma '(cd /u/ftp/u/spr; rm -rf s6; jar xf s6.jar)'
	rsh wilma '(chmod -R a+r /u/ftp/u/spr/s6)'


world:
	$(MAKE) cleanall
	$(MAKE) links
	$(MAKE) machall
#	$(MAKE) prof

newmachine:
	$(GMAKE) setup
#	$(GMAKE) $(C++_COMPONENTS) 'COMMAND=createc++' $(COPYVALUES)
	$(GMAKE) $(JAVA_COMPONENTS) $(UI_COMPONENTS) 'COMMAND=createjava' $(COPYVALUES)
	$(GMAKE) create
	$(GMAKE) links
	$(GMAKE) makedep
	$(GMAKE) machall

xxxnewmachine:
#	$(GMAKE) $(C++_COMPONENTS) 'COMMAND=createc++' $(COPYVALUES)
	$(GMAKE) $(JAVA_COMPONENTS) $(UI_COMPONENTS) 'COMMAND=createjava' $(COPYVALUES)
	$(GMAKE) create
	$(GMAKE) links
#	$(GMAKE) makedep
	$(GMAKE) machall

update:
	$(MAKE) machall
#	$(MAKE) prof

bindistrib:
	rm -rf $(DISTRIB_BIN)
	mkdir $(DISTRIB_BIN)
	(cd bin; $(DMAKE) bindistrib)
	(cd data; $(DMAKE) bindistrib)
	`s6basepath`/bin/s6jar
	(cd lib; $(DMAKE) bindistrib)
	mkdir $(DISTRIB_BIN)/tmp

cvsco:
	$(GMAKE) setup
	$(GMAKE) create
	$(GMAKE) links
	$(GMAKE) makedep


setup:
	bin/s6setup
	for x in $(C++_COMPONENTS); do \
	   rm -rf $$x/src/Makefile; \
	   cp data/Make.pass $$x/src/Makefile; \
	   bin/s6createc++ $$x; \
	 done
	for x in $(JAVA_COMPONENTS); do \
	   bin/s6createjava $$x; \
	 done
	for x in $(UI_COMPONENTS); do \
	   bin/s6createjava $$x; \
	 done

linux:
	$(MAKE) "C++_COMPONENTS=$(LC++_COMPONENTS)" all opt

apple:
	$(MAKE) "C++_COMPONENTS=$(APPLE_COMPONENTS)" all opt

solaris:
	$(MAKE) all opt


ifeq ($(C++_COMPONENTS)X,X)
machall:
	$(GMAKE) all
else
machall:
	- if [ $(BROWN_VELD_ARCH) = i686 ]; then $(GMAKE) linux; else true; fi
	- if [ $(BROWN_VELD_ARCH) = x86_64 ]; then $(GMAKE) linux; else true; fi
	- if [ $(BROWN_VELD_ARCH) = sol ]; then $(GMAKE) solaris; else true; fi
	- if [ $(BROWN_VELD_ARCH) = sol32 ]; then $(GMAKE) solaris; else true; fi
	- if [ $(BROWN_VELD_ARCH) = ppc ]; then $(GMAKE) apple; else true; fi
	- if [ $(BROWN_VELD_ARCH) = i386 ]; then $(GMAKE) apple; else true; fi
endif
