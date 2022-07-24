package models.client

import io.circe.Decoder.Result
import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder, HCursor, Json}

import java.time.{Instant, LocalDateTime, ZoneId}

case class Core(limit: Int, remaining: Int, reset: LocalDateTime, used: Int)

object Core {
  implicit val resetCodec: Codec[LocalDateTime] = new Codec[LocalDateTime] {
    override def apply(a: LocalDateTime): Json =
      Encoder.encodeString.contramap[LocalDateTime](_.toString).apply(a)

    override def apply(c: HCursor): Result[LocalDateTime] =
      Decoder.decodeLong.map(s => LocalDateTime.ofInstant(Instant.ofEpochSecond(s), ZoneId.of("UTC"))).apply(c)
  }

  implicit val coreCodec: Codec.AsObject[Core] = deriveCodec[Core]
}
