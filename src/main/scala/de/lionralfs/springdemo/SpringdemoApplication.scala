package de.lionralfs.springdemo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.{
  RequestMapping,
  RequestParam,
  RestController
}
import scala.concurrent.Future
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import co.elastic.apm.attach.ElasticApmAttacher
import co.elastic.apm.api.{ElasticApm, Traced}
import scala.util.Failure
import scala.util.Success

@SpringBootApplication
@RestController
class SpringdemoApplication {
  @RequestMapping(path = Array("/hello"))
  def hello(
      @RequestParam(value = "name", defaultValue = "World") name: String
  ): String = {
    Await.result(asyncWork(name), Duration(2, TimeUnit.SECONDS))
  }

  def asyncWork(name: String): Future[String] = wrap(
    Future {
      Thread.sleep(1000)
      s"Hello $name";
    }
  )

  // see https://github.com/wellcomecollection/scala-libs/blob/f27de0186e5f08409cfb32b253d90dd3c4c79168/typesafe_app/src/main/scala/weco/Tracing.scala#L85
  def wrap[T](block: => Future[T]): Future[T] = {
    val span = ElasticApm.currentTransaction().startSpan().setName("wrapped")
    block.transform(res => {
      res match {
        case Success(_)         =>
        case Failure(exception) => span.captureException(exception)
      }
      span.end()
      res
    })
  }
}

object SpringdemoApplication extends App {
  ElasticApmAttacher.attach();
  SpringApplication.run(classOf[SpringdemoApplication], args: _*)
}
