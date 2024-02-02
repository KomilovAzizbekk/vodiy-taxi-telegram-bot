package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.taxiservicebot.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByChatId(String chatId);

    User findByChatId(String chatId);
}
