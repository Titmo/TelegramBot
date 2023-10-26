package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.sky.telegrambot.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
@Query(value="select n.\"time\" from notification n order by n.\"time\"",nativeQuery = true)
List<LocalDateTime> sortedTime();

    Notification findByTime(LocalDateTime time);
    List<LocalDateTime> findAllTime();
}
