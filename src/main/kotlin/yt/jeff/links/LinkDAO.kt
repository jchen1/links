package yt.jeff.links

import org.sql2o.Sql2o

class LinkDAO(val sql2o: Sql2o) {

    fun insert(url: String) : String {
        sql2o.beginTransaction().use {
            val id: String = it.createQuery("insert into links(url) VALUES (:url)", true)
                    .addParameter("url", url)
                    .executeUpdate()
                    .getKey(String::class.java)

            it.commit()
            return id
        }
    }

    fun getById(id: Long) : Link? {
        sql2o.beginTransaction().use {
            val links = it.createQuery("SELECT * FROM links WHERE id=:id")
                    .addParameter("id", id)
                    .executeAndFetch(Link::class.java)
            return links?.get(0)
        }
    }
}