package com.anatawa12.mataram.util


fun <T> Sequence<T>.dropLast(n: Int): Sequence<T> {
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return this
    return DropLastSequence(this, n)
}

private class DropLastSequence<T>(val sequence: Sequence<T>, val count: Int) : Sequence<T> {
    override fun iterator(): Iterator<T> = DropLastSequenceIterator(sequence.iterator(), count)
}

private class DropLastSequenceIterator<T>(val iterator: Iterator<T>, count: Int) : Iterator<T> {
    /**
     * null means reached end of sequence
     */
    @Suppress("UNCHECKED_CAST")
    private var queue: Array<T>? = arrayOfNulls<Any?>(count) as Array<T>

    /**
     * if negative, the inverted index last returned.
     * if positive, the index should return next time.
     */
    private var index: Int

    init {
        for (i in 0 until count) {
            if (!iterator.hasNext()) {
                queue = null
                break
            }
            queue!![i] = iterator.next()
        }
        index = if (queue != null) {
            queue!!.lastIndex.inv()
        } else {
            0
        }
    }

    override fun hasNext(): Boolean {
        val queue = queue ?: return false
        if (index < 0) {
            // compute
            var newIndex = index.inv() + 1
            if (newIndex == queue.size) newIndex = 0
            if (!iterator.hasNext()) {
                this.queue = null
                return false
            }
            index = newIndex
        }
        return true
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        val queue = queue!!
        val result = queue[index]
        queue[index] = iterator.next()
        index = index.inv()
        return result
    }
}



