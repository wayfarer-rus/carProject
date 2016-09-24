import bluetooth

nearby_devices = bluetooth.discover_devices()

for bdaddr in nearby_devices:
    print (bluetooth.lookup_name( bdaddr ))
