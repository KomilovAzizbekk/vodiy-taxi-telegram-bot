package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.mediasolutions.taxiservicebot.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Region findByName(String name);

    @Query(nativeQuery = true, value = "select r.name from region r join district d on r.id = d.region_id where d.name=:name")
    String findByDistrictName(String name);

}
