package com.ergodicity.engine.plaza2

import futures.Sessions
import org.slf4j.LoggerFactory
import java.io.File
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import plaza2.RequestType.CombinedDynamic
import com.ergodicity.engine.plaza2.Connection.Connect
import plaza2.{TableSet, Connection => P2Connection, DataStream => P2DataStream}
import scheme.{FutInfo, SessionRecord}
import com.ergodicity.engine.plaza2.DataStream.{JoinTable, SetLifeNumToIni, Open}
import akka.testkit.{TestActorRef, TestFSMRef, TestKit}
import java.util.concurrent.TimeUnit

class DataStreamIntegrationSpec extends TestKit(ActorSystem()) with WordSpec {
  val log = LoggerFactory.getLogger(classOf[ConnectionSpec])

  val Host = "localhost"
  val Port = 4001
  val AppName = "DataStreamIntegrationSpec"

  "DataStream" must {
    "do some stuff" in {
      val underlyingConnection = P2Connection()
      val connection = TestFSMRef(Connection(underlyingConnection), "Connection")
      connection ! Connect(Host, Port, AppName)

      val ini = new File("plaza2/scheme/FuturesSession.ini")
      val tableSet = TableSet(ini)
      val underlyingStream = P2DataStream("FORTS_FUTINFO_REPL", CombinedDynamic, tableSet)

      val dataStream = TestFSMRef(new DataStream(underlyingStream), "FuturesInfo")
      dataStream ! SetLifeNumToIni(ini)
      val sessions = TestActorRef(new Sessions(dataStream), "Sessions")

      /*val repository = TestFSMRef(new Repository[SessionRecord]()(FutInfo.SessionDeserializer), "SessionsRepository")

      dataStream ! JoinTable(repository, "session")*/

      dataStream ! Open(connection.underlyingActor)

      Thread.sleep(TimeUnit.DAYS.toMillis(10))
    }
  }
}