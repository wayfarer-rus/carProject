CC=gcc
CFLAGS=-I. -I/opt/vc/include/interface/mmal -I/opt/vc/include
DEPS=RaspiCLI.h RaspiCamControl.h RaspiPreview.h
OBJ=cameraUnit.o RaspiCamControl.o RaspiCLI.o RaspiPreview.o
BIN=camera-unit
LDIR=/opt/vc/lib/
LDFLAGS+=-lmmal -lmmal_core -lmmal_util -lvcos -lpthread -lbcm_host

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

all: $(OBJ)
	gcc -o $(BIN) $^ $(CFLAGS) -L$(LDIR) $(LDFLAGS)

.PHONY: clean

clean:
	rm -f *.o *~ $(BIN)
