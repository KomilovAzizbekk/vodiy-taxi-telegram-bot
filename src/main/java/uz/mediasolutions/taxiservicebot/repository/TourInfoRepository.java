package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.mediasolutions.taxiservicebot.entity.TourInfo;

import java.util.List;
import java.util.UUID;

public interface TourInfoRepository extends JpaRepository<TourInfo, UUID> {

    List<TourInfo> findTourInfosByUserChatIdOrderByCreatedAtDesc(String chatId);


}
