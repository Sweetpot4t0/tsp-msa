package account.repository;

import account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // WHERE 절의 balance >= amount 조건 자체가 락 없이도 동시 차감을 안전하게 막는 핵심.
    // 영향받은 row 수가 0이면 잔액 부족(또는 계정 없음)으로 판단한다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :id AND a.balance >= :amount")
    int debit(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :id")
    int credit(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
