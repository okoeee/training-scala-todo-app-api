package lib.persistence

import scala.concurrent.Future
import ixias.persistence.SlickRepository
import lib.model.User
import slick.jdbc.JdbcProfile

case class UserRepository[P <: JdbcProfile]()(implicit val driver: P)
  extends SlickRepository[User.Id, User, P]
  with db.SlickResourceProvider[P] {

  import api._

  /**
    * Get User Data
    */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(UserTable, "slave") { _.filter(_.id === id).result.headOption }

  /**
    * Get User By Email
    */
  def getByEmail(email: String): Future[Option[EntityEmbeddedId]] =
    RunDBAction(UserTable, "slave") {
      _.filter(_.email === email).result.headOption
    }

  /**
    * Add User Data
    */
  def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(UserTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  /**
    * Update User Data
    */
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(UserTable) { slick =>
      val row = slick.filter(_.id === entity.id)
      for {
        old <- row.result.headOption
        _ <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.update(entity.v)
        }
      } yield old
    }

  /**
    * Delete User Data
    */
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(UserTable) { slick =>
      val row = slick.filter(_.id === id)
      for {
        old <- row.result.headOption
        _ <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.delete
        }
      } yield old
    }
}
