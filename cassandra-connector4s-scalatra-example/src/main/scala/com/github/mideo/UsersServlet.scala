package com.github.mideo

import java.util.{Comparator, UUID}

import akka.actor.ActorSystem
import com.datastax.driver.mapping.Mapper
import com.github.mideo.repository.{RepositoryMapper, User, UserAccessor}
import javax.inject.Singleton
import org.codehaus.jackson.map.ObjectMapper
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{FutureSupport, ScalatraServlet}
import org.json4s.{DefaultFormats, Formats}

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
  private val futureMapper: Future[Mapper[User]] = RepositoryMapper.materialise(classOf[User])
 private val futureAccessor: Future[UserAccessor] = RepositoryMapper.materialiseAccessor(classOf[UserAccessor])
  private val objectMapper: ObjectMapper = new ObjectMapper()

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  override protected implicit def executor: ExecutionContext = system.dispatcher

  get("/user") {
    futureMapper map {
      mapper =>
        val uid = UUID.randomUUID()
        val user = new User(uid, "scalatraUser")
        mapper.save(user)
        objectMapper.writeValueAsString(mapper.get(uid))
    }
  }


  get("/users") {
    futureMapper map  {
      mapper =>
        mapper.save(new User(UUID.randomUUID(), "scalatraUser1"))
        mapper.save(new User(UUID.randomUUID(), "scalatraUser2"))
    } flatMap (
      _ => futureAccessor map { accessor => objectMapper
        .writeValueAsString({
          val result = accessor.getAll.all()
          result.sort((o1: User, o2: User) => o1.name.compareTo(o2.name))
          result
        })
      }
    )
  }

}
