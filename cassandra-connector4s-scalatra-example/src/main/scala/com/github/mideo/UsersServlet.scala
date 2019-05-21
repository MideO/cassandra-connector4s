package com.github.mideo

import java.util.UUID

import akka.actor.ActorSystem
import com.datastax.driver.mapping.Mapper
import com.github.mideo.repository.{CassandraKeyspace, User, UserAccessor}
import javax.inject.Singleton
import org.codehaus.jackson.map.ObjectMapper
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{FutureSupport, ScalatraServlet}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UsersServlet
  extends ScalatraServlet
    with FutureSupport
    with JacksonJsonSupport {

  before() {
    contentType = formats("json")
  }

  private implicit val system: ActorSystem = ActorSystem("ScalatraAppSystem")

  case class DbTable(mapper: Mapper[User], accessor: UserAccessor)

  val futureDbTable: Future[DbTable] = for {
    c <- CassandraKeyspace
    mapper <- c.materialise[User]
    accessor <- c.materialiseAccessor[UserAccessor]
  } yield DbTable(mapper, accessor)

  private final val objectMapper: ObjectMapper = new ObjectMapper()

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  override protected implicit def executor: ExecutionContext = system.dispatcher

  get("/user") {
    futureDbTable map {
      dbTable =>
        dbTable.accessor.truncate
        val uid = UUID.randomUUID()
        val user = new User(uid, "scalatraUser")
        dbTable.mapper.save(user)
        objectMapper.writeValueAsString(dbTable.mapper.get(uid))
    }
  }


  get("/users") {
    futureDbTable map {
      dbTable =>
        dbTable.accessor.truncate
        dbTable.mapper.save(new User(UUID.randomUUID(), "scalatraUser1"))
        dbTable.mapper.save(new User(UUID.randomUUID(), "scalatraUser2"))
        val result = dbTable.accessor.getAll.all()
        result.sort((o1: User, o2: User) => o1.name.compareTo(o2.name))
        objectMapper.writeValueAsString(result)
    }

  }

}
