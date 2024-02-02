package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.mediasolutions.taxiservicebot.entity.Car;

import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    @Query(nativeQuery = true, value = "select * from cars c join driver d on c.id = d.car_id where d.chat_id=:chatId")
    Car findByUserChatId(String chatId);

}
