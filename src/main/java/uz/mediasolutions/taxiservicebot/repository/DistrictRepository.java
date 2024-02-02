package uz.mediasolutions.taxiservicebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.mediasolutions.taxiservicebot.entity.District;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query(nativeQuery = true, value = "select d.name from district d join region r on r.id = d.region_id where r.name=:name")
    List<String> districtNameByRegion(String name);

    @Query(nativeQuery = true, value = "select name from district")
    List<String> districtName();

}
