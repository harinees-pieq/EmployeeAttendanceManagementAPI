package dao

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo


class RoleDao(private val jdbi: Jdbi) {
    fun findByName(name: String): String? {
        return jdbi.withHandle<String?, Exception> { handle ->
            handle.createQuery("SELECT name FROM role WHERE name = :name")
            .bind("name", name)
            .mapTo<String>().findFirst().orElse(null)
        }
    }
}