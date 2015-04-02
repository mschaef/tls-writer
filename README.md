# tls-writer
A variant of OutputStream that adds per-line synchronization across multi-thread writes

# Implementation Details

This is a proof of concept of an implementation of `OutputStream` that lets multiple
threads write to the same stream and produce sensible output, without taking any
steps to synchronize access. The way it achieves this goal is by establishing a
thread-local buffer for thread to gather complete lines of text. The lines of text
are then atomically written to the underlying output. This gives the following
guarantees:

* The output lines of text from a thread appear in the order in which that thread wrote them.
* Each line of output text comes from a single source thread.
