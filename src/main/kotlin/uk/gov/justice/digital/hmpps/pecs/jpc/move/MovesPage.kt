package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class MovesPage(moves: List<Move>, pageable: Pageable, total: Long) : PageImpl<Move>(moves, pageable, total)


class MovesPageable(pageNumber: Int, pageSize: Int, offset: Long, sort: Sort): Pageable {
    /**
     * Returns the page to be returned.
     *
     * @return the page to be returned.
     */
    override fun getPageNumber(): Int = pageNumber

    /**
     * Returns the number of items to be returned.
     *
     * @return the number of items of that page
     */
    override fun getPageSize() : Int = pageSize
    /**
     * Returns the offset to be taken according to the underlying page and page size.
     *
     * @return the offset to be taken
     */
    override fun getOffset(): Long = offset

    /**
     * Returns the sorting parameters.
     *
     * @return
     */
    override fun getSort(): Sort = sort

    /**
     * Returns the [Pageable] requesting the next [Page].
     *
     * @return
     */
    override fun next(): Pageable {
        TODO("Not yet implemented")
    }

    /**
     * Returns the previous [Pageable] or the first [Pageable] if the current one already is the first one.
     *
     * @return
     */
    override fun previousOrFirst(): Pageable {
        TODO("Not yet implemented")
    }

    /**
     * Returns the [Pageable] requesting the first page.
     *
     * @return
     */
    override fun first(): Pageable {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether there's a previous [Pageable] we can access from the current one. Will return
     * false in case the current [Pageable] already refers to the first page.
     *
     * @return
     */
    override fun hasPrevious(): Boolean {
        TODO("Not yet implemented")
    }
}