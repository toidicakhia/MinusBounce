package net.minusmc.minusbounce.utils

object StringUtils {

	fun getFormatTime(deltaTime: Long): String {
		val realTime = deltaTime / 1000

		val hours = realTime / 3600
		val minutes = (realTime % 3600) / 60
		val seconds = (realTime % 3600) % 60

		return "${hours}h ${minutes}m ${seconds}s"
    }

}