CC=gcc -pthread -O3
CFLAGS=-I. -I/opt/vc/include/interface/mmal -I/opt/vc/include -lbluetooth
DEPS=bluetoothServer.h cameramodule.h RaspiCLI.h RaspiCamControl.h
OBJ=main.o bluetoothServer.o cameramodule.o RaspiCamControl.o RaspiCLI.o
BIN=car-driver
LDIR=/opt/vc/lib/
LDFLAGS+=-lmmal -lmmal_core -lmmal_util -lvcos -lpthread -lbcm_host

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

all: $(OBJ)
	$(CC) -o $(BIN) $^ $(CFLAGS) -L$(LDIR) $(LDFLAGS)

.PHONY: clean

clean:
	rm -f *.o *~ $(BIN)
