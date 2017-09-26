prefix = /apps/adf/pado/prod-iot

all: 

install: 
	mkdir -p $(DESTDIR)$(prefix)
	cp -rf pado-0.4.1b4/* $(DESTDIR)$(prefix)/

clean:
	

distclean: clean

uninstall:
	-rm -f $(DESTDIR)$(prefix)

.PHONY: all install clean distclean uninstall
