package services

import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "user_") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email: Rep[String] = column[String]("email")
    def nickname: Rep[String] = column[String]("nickname")
    def password: Rep[String] = column[String]("password")

    def * = (id, email, nickname, password) <> ((User.apply _).tupled, User.unapply)
  }

  val user = TableQuery[UserTable]

  def create(email: String, nickname: String, password: String): Future[User] = db.run {
    (user.map(u => (u.email, u.nickname, u.password))
      returning user.map(_.id)
      into { case ((email, nickname, password), id) => User(id, email, nickname, password) }
      ) += (email, nickname, password)
  }

  def getByIdOption(id: Long): Future[Option[User]] = db.run {
    user.filter(_.id === id).result.headOption
  }

  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def update(id: Long, new_user: User): Future[Int] = {
    val userToUpdate: User = new_user.copy(id)
    db.run(user.filter(_.id === id).update(userToUpdate))
  }

  def delete(id: Long): Future[Int] = db.run(user.filter(_.id === id).delete)
}
