package uz.mediasolutions.taxiservicebot.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import uz.mediasolutions.taxiservicebot.entity.template.AbsUUID;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
@Entity(name = "tour_info")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE tour_info SET deleted=true WHERE id=?")
public class TourInfo extends AbsUUID {

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "people_count_delivery")
    private String peopleCountOrDelivery;

    @Column(name = "date_time")
    private String dateTime;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}
