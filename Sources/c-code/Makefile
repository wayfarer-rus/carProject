CC=gcc -std=c99 -pthread -O3
CFLAGS=-I. -I/opt/vc/include/interface/mmal -I/opt/vc/include -lbluetooth
DEPS=bluetoothServer.h
OBJ=main.o bluetoothServer.o
BIN=car-driver
LDIR=/opt/vc/lib/
LDFLAGS+=-lmmal -lmmal_core -lmmal_util -lvcos -lpthread -lbcm_host -pthread -lbluetooth

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

all: $(OBJ)
	$(CC) -o $(BIN) $^ $(CFLAGS) -L$(LDIR) $(LSFLAGS)

.PHONY: clean

clean:
	rm -f *.o *~ $(BIN)
