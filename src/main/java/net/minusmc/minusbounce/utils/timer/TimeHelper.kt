package net.minusmc.minusbounce.utils.timer

class TimeHelper {
    private var time: Long = System.currentTimeMillis()

    fun reached(currentTime: Long): Boolean {
        return maxOf(0L, System.currentTimeMillis() - time) >= currentTime
    }

    fun reached(lastTime: Long, currentTime: Long): Boolean {
        return maxOf(0L, System.currentTimeMillis() - time + lastTime) >= currentTime
    }

    fun reset() {
        time = System.currentTimeMillis()
    }

    fun getTime(): Long {
        return maxOf(0L, System.currentTimeMillis() - time)
    }

}