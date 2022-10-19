package de.lionralfs.springdemo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.{
  RequestMapping,
  RequestParam,
  RestController
}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import co.elastic.apm.attach.ElasticApmAttacher
import co.elastic.apm.api.Traced

@SpringBootApplication
@RestController
class SpringdemoApplication {
  @RequestMapping(path = Array("/hello"))
  def hello(
      @RequestParam(value = "name", defaultValue = "World") name: String
  ): String = {
    Await.result(asyncWork(name), Duration(2, TimeUnit.SECONDS))
  }

  @Traced
  def asyncWork(name: String): Future[String] = {
    Future {
      Thread.sleep(1000)
      s"Hello $name";
    }
  }
}

object SpringdemoApplication extends App {
  ElasticApmAttacher.attach();
  SpringApplication.run(classOf[SpringdemoApplication], args: _*)
}
