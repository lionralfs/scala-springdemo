package de.lionralfs.springdemo

import io.opentelemetry.instrumentation.annotations.WithSpan
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoDatabase}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, RestController}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

@Component
@RestController
class Controller(db: MongoDatabase) {
  @RequestMapping(path = Array("/hello"))
  def hello(
      @RequestParam(value = "name", defaultValue = "World") name: String
  ): String = {
    Await.result(insertIntoDB(name), 2.seconds)

    val query = db
      .getCollection("test-collection")
      .find(Document())
      .toFuture()

    Await
      .result(query, 2.seconds)
      .map(doc => {
        doc
          .find(_._1 == "name")
          .map(_._2.asInstanceOf[BsonString].getValue)
          .getOrElse("UNKNOWN")
      })
      .mkString("<br>")
  }

  @WithSpan
  def insertIntoDB(name: String): Future[_] = {
    db.getCollection("test-collection")
      .insertOne(Document("name" -> name))
      .toFuture()
  }
}

@SpringBootApplication
class SpringdemoApplication {
  @Bean
  def mongoDatabase(): MongoDatabase = {
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27099")
    mongoClient.getDatabase("test-db")
  }
}

object SpringdemoApplication extends App {
  SpringApplication.run(classOf[SpringdemoApplication], args: _*)
}
