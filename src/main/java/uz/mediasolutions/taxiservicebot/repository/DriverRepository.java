package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.mediasolutions.taxiservicebot.entity.Driver;

import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Driver findByChatId(String chatId);

    boolean existsByChatId(String chatId);

}
