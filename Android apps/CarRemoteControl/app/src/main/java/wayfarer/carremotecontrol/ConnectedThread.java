package wayfarer.carremotecontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Project BluetoothClientTest
 * Created by wayfarer on 8/21/16.
 */
public class ConnectedThread extends Thread {
    public static final int MESSAGE_READ = 4;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(Handler handler, BluetoothSocket socket) {
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    private void putChunkToQueue(byte[] b, int offset, int endOfTheChunkInd) {
        if (!(endOfTheChunkInd > 0)) {
            return;
        }

        byte[] tmp =  new byte[endOfTheChunkInd];
        int n = 0;

        for (int i = offset; i < endOfTheChunkInd; ++i) {
            tmp[n++] = b[i];
        }

        CarRemoteControl.videoNalQueue.add(tmp);
    }

    public void run() {
        final int bufferSize = 10000;//65536;
        byte[] buffer = new byte[bufferSize];  // buffer store for the stream
        int bytes; // bytes returned from read()
        int cursor = 0;
        boolean streamStarted = false;
//        NALParserStreamData smallNALChunksApproach = new NALParserStreamData(new ByteArrayOutputStream(), false);

        // Keep listening to the InputStream until an exception occurs
        while (mmSocket.isConnected()) {
            try {
                if (mmInStream.available() > 0) {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer, cursor, bufferSize - cursor);
                    cursor += bytes;
                    Log.d("client", "new data read from socket. #bytes = " + bytes);
                    //smallNALChunksApproach.getStreamBuffer().write(buffer, 0, bytes);

                    if (cursor < bufferSize)
                        continue;

                    int i = cursor-1; // last element

                    while (i > 0) {
                        // find last magic sequence
                        if (0x01 == buffer[i]) {
                            if ((i - 3) >= 0) {
                                int sequenceStart = -1;

                                if (0x00 == buffer[i - 1] && 0x00 == buffer[i - 2] && 0x00 == buffer[i-3]) {
                                    sequenceStart = i - 3;
                                } else if (0x00 == buffer[i - 1] && 0x00 == buffer[i - 2]) {
                                    sequenceStart = i -2;
                                }

                                if (sequenceStart > -1) {
                                    putChunkToQueue(buffer,0, sequenceStart);
                                    // copy tail to the beginning of the buffer
                                    {
                                        int n = 0;
                                        for(int m = sequenceStart; m < bufferSize; ++m) {
                                            buffer[n++] = buffer[m];
                                        }
                                        cursor = n;
                                    }

                                    break;
                                }
                            }
                        }

                        --i;
                    }

                    if (!streamStarted) {
                        streamStarted = true;
                        mHandler.obtainMessage(MESSAGE_READ).sendToTarget();
                    }

//                    smallNALChunksApproach.invoke();


                    // section where we find special key-sequence that was send to us
                    // by server and cut stream data based on this sequence
/*                    byte[] b = streamBuffer.toByteArray();
                    int headerStartInd = -1;
                    int headerEndInd = -1;
                    int sizeStartInd = 0;
                    int i = 0;

                    while (i < b.length) {
                        if ('{' == b[i]) {
                            headerStartInd = i;
                            // check for header
                            if ((i+7) < b.length &&
                                    '"' == b[i+1] &&
                                    's' == b[i+2] &&
                                    'i' == b[i+3] &&
                                    'z' == b[i+4] &&
                                    'e' == b[i+5] &&
                                    '"' == b[i+6] &&
                                    ':' == b[i+7]
                                    )
                            {
                                sizeStartInd = i+8;
                                i = i+8;
                                // find end of the header
                                while (i<b.length && '}' != b[i])
                                    ++i;

                                if ('}' != b[i]) {
                                    // header not loaded yet
                                    continue;
                                }

                                byte[] d = new byte[i-sizeStartInd];
                                for (int n = sizeStartInd; n < (sizeStartInd+d.length); ++n)
                                    d[n-sizeStartInd] = b[n];

                                chunkSize = Integer.valueOf(new String(d, "UTF-8"));
                                headerEndInd = i;
                                // we found what we need
                                break;
                            }
                        }

                        ++i;
                    }

                    if (headerEndInd > -1 && headerStartInd > -1) {
                        Log.d("client", "next chunk size: " + chunkSize);
                        if (!nextChunk) {
                            // skip everything before header
                            // save everything after header as new streamBuffer
                            streamBuffer = new ByteArrayOutputStream();

                            if (b.length > (headerEndInd+1))
                                streamBuffer.write(b, headerEndInd+1, b.length-(headerEndInd+1));

                            nextChunk = true;
                            mHandler.obtainMessage(MESSAGE_READ).sendToTarget();
                        } else {
                            // we are at the end of first chunk
                            // write everything before header to streamBuffer and send to client

                            byte[] data = new byte[headerStartInd];
                            for (int n = 0; n < headerStartInd; ++n)
                                data[n] = b[n];

                            CarRemoteControl.videoNalQueue.add(data);

                            // reset buffer and write rest the data to it
                            streamBuffer = new ByteArrayOutputStream();
                            if (b.length > (headerEndInd+1))
                                streamBuffer.write(b, headerEndInd+1, b.length - (headerEndInd+1));
                        }

                    }
*/
//                    Log.d("client", "current buffer size: " + streamBuffer.size());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    private class NALParserStreamData {
        private ByteArrayOutputStream streamBuffer;
        private boolean streamStarted;

        public NALParserStreamData(ByteArrayOutputStream streamBuffer, boolean streamStarted) {
            this.streamBuffer = streamBuffer;
            this.streamStarted = streamStarted;
        }

        public ByteArrayOutputStream getStreamBuffer() {
            return streamBuffer;
        }

        public boolean isStreamStarted() {
            return streamStarted;
        }

        public NALParserStreamData invoke() {
            byte[] b = streamBuffer.toByteArray();
            int chunkStartInd = 0;
            int headerStartInd = -1;
            int headerEndInd = -1;
            int i = 0;

            while (i < b.length) {
                if (0x00 == b[i]) {
                    // check for header 0x00 0x00 0x01 is a 'magic sequence'
                    if ((i+3) < b.length) {
                        if (0x00 == b[i + 1] && 0x01 == b[i + 2]) {
                            headerStartInd = i;
                            i = i + 2;
                            headerEndInd = i;
                            // copy chunk to queue and continue
                            putChunkToQueue(b, chunkStartInd, headerStartInd);
                            chunkStartInd = i;
                            //Log.d("StreamParser", "ChunkStartInd = " + chunkStartInd + "; HeaderStartInd = " + headerStartInd + "; HeaderEndInd = " + headerEndInd);
                            continue;
                        } else if (0x00 == b[i + 1] && 0x00 == b[i + 2] && 0x01 == b[i + 3]) {
                            headerStartInd = i;
                            i = i + 3;
                            headerEndInd = i;
                            // copy chunk to queue and continue
                            putChunkToQueue(b, chunkStartInd, headerStartInd);
                            chunkStartInd = i;
                            //Log.d("StreamParser", "ChunkStartInd = " + chunkStartInd + "; HeaderStartInd = " + headerStartInd + "; HeaderEndInd = " + headerEndInd);
                            continue;
                        }
                    }
                }

                ++i;
            }

            Log.d("StreamParser", "HeaderStartInd = " + headerStartInd + "; QueueSize = " + CarRemoteControl.videoNalQueue.size());

            if (headerStartInd > -1) {
                streamBuffer = new ByteArrayOutputStream();

                if (b.length > headerStartInd)
                    streamBuffer.write(b, headerStartInd, b.length-headerStartInd);

                if (!streamStarted && CarRemoteControl.videoNalQueue.size() > 20) {
                    streamStarted = true;
                    // message to init screen
                    mHandler.obtainMessage(MESSAGE_READ).sendToTarget();
                }
            }
            return this;
        }
    }
}
