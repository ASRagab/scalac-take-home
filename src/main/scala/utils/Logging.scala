package utils

import org.slf4j.{Logger, LoggerFactory}
import zio.ZLayer

case class Logging() extends Function[Class[_], Logger] {
  def apply(clazz: Class[_]): Logger = LoggerFactory.getLogger(clazz)
}

object Logging {
  val live = ZLayer.succeed(Logging())
}
