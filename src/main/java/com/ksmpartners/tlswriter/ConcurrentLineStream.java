package com.ksmpartners.tlswriter;

import java.util.List;
import java.util.LinkedList;

import java.io.IOException;
import java.io.OutputStream;

/**
 * ConcurrentLineStream is a wrapper around OutputStream that
 * automatically adds line based synchronization of writes to the
 * underlying output stream. PUt another way, multiple threads can
 * write to a ConcurrentLineStream, and the concurrent stream ensures
 * that each line of text in the final output will only contain text
 * from a single thread.
 */
public class ConcurrentLineStream
    extends OutputStream
{
    /** The underlying output stream. All output ultimately gets
     * written to this stream. */
    protected OutputStream underlying = null;

    public ConcurrentLineStream(OutputStream underlying)
    {
        this.underlying = underlying;
    }

    private List<StringBuffer> threadLineBuffers =
        new LinkedList<StringBuffer>();

    ThreadLocal<StringBuffer> threadBuf = new ThreadLocal<StringBuffer>() {
        protected StringBuffer initialValue() {
            StringBuffer buf = new StringBuffer();

            synchronized(threadLineBuffers) {
                threadLineBuffers.add(buf);
            }

            return buf;
        }
    };

    protected void flushBuffer(StringBuffer buf)
        throws IOException
    {
        synchronized(underlying) {
            underlying.write(buf.toString().getBytes());
        }
    }

    public void flush()
        throws IOException
    {
        flushBuffer(threadBuf.get());

        threadBuf.get().setLength(0);
    }

    public void write(int b)
        throws IOException
    {
        threadBuf.get().append((char)b);

        if (b == (int)'\n')
            flush();
    }

    public void close()
        throws IOException
    {
        for(StringBuffer lbuf : threadLineBuffers)
            flushBuffer(lbuf);

        underlying.close();
    }
}
