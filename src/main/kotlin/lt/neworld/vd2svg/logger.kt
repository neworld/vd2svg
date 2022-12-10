package lt.neworld.vd2svg

import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */

fun logProgress(msg: String) {
    Logger.getGlobal().log(Level.INFO, msg)
}

fun logWarning(msg: String) {
    Logger.getGlobal().log(Level.WARNING, msg)
}