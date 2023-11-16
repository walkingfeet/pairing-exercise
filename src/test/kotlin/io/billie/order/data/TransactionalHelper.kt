package io.billie.order.data

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionalHelper {

    @Transactional
    fun <T> executeInTransaction(block: () -> T) : T {
        return block();
    }

}