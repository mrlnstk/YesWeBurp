package yesweburp

class EventBus<T> {
    private class Listener<T>(val persistent: Boolean, val action: (T) -> Unit)

    private val listeners = mutableListOf<Listener<T>>()

    fun listen(action: (T) -> Unit) {
        listeners.add(Listener(true, action))
    }

    fun listenOnce(action: (T) -> Unit) {
        listeners.add(Listener(false, action))
    }

    fun fire(data: T) {
        // Snapshot first: a listener may add or remove listeners while it runs.
        val current = listeners.toList()
        listeners.removeAll { !it.persistent }
        current.forEach { it.action(data) }
    }
}
