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
 * that originated from a single thread.
 * <br>
 * <br>Note:</b> This stream immediately honors <tt>close</tt> and
 * <tt>flush</tt> operations without automatically introducing new
 * EOLN's. This can break the line-per-thread guarantee provided by
 * this stream. The easiest way to avoid this problem is to avoid
 * manually flushing the stream and ensure that all threads have written
 * a complete line when the stream is closed.
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

    /** A <tt>StringBuffer</tt> managed on a per-thread basis. Each
     * thread that calls into the <tt>ConcurrentLineStream</tt> gets a
     * local buffer that it uses to accumulate a line of text. Once a
     * full line has been accumulated in the thread local buffer, it is
     * atomically written to the underlying output.
     */
    ThreadLocal<StringBuffer> threadBuf = new ThreadLocal<StringBuffer>() {
        protected StringBuffer initialValue() {
            StringBuffer buf = new StringBuffer();

            synchronized(threadLineBuffers) {
                threadLineBuffers.add(buf);
            }

            return buf;
        }
    };

    /** A list of all the thread local buffers created for this
     * instance of <tt>ConcurrentLineStream</tt>. This is used when
     * the stream is closed to ensure that all text waiting in thread
     * buffers gets flushed to the underlying output.
     */
    private List<StringBuffer> threadLineBuffers =
        new LinkedList<StringBuffer>();

    /** Atomically write the contents of a buffer to the underlying
     * output.
     */
    protected void flushBuffer(StringBuffer buf)
        throws IOException
    {
        synchronized(underlying) {
            underlying.write(buf.toString().getBytes());

            threadBuf.get().setLength(0);
        }
    }

    /** Flush the current thread buffer to the underlying output.
     * <br>
     * <b>Note:</b> This results in a write to the underlying stream
     * that is performed regardless if the thread buffer contains a complete
     * line of text or not. This can result in output from two threads
     * occurring on the underlying output stream.
     */
    public void flush()
        throws IOException
    {
        flushBuffer(threadBuf.get());
    }


    /** Extend the current line buffer, flushing it if a EOLN has
     * been reached.
     */
    public void write(int b)
        throws IOException
    {
        threadBuf.get().append((char)b);

        if (b == (int)'\n')
            flush();
    }

    /** Close the stream, flushing any residual output remaining on
     * the thread buffers. No additional newlines are introduced into
     * the output stream, so the final output from each thread will
     * all be written to the same output line. */
    public void close()
        throws IOException
    {
        for(StringBuffer lbuf : threadLineBuffers)
            flushBuffer(lbuf);

        underlying.close();
    }
}
