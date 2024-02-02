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
@Entity(name = "driver")
@Where(clause = "deleted=false")
@SQLDelete(sql = "UPDATE driver SET deleted=true WHERE id=?")
public class Driver extends AbsUUID {

    @Column(nullable = false, name = "chat_id")
    private String chatId;

    @Column(name = "fio")
    private String FIO;

    @Column(name = "phone_number_1")
    private String phoneNumber1;

    @Column(name = "phone_number_2")
    private String phoneNumber2;

// TODO TO'LOV QO'SHILSA ISHLAYDI

//    @Column(nullable = false, name = "is_active") //ACTIVE OR BLOCKED
//    private boolean isActive = false;
//
//    @Column(name = "paid_time")
//    private LocalDateTime paidTime;

    @OneToOne(fetch = FetchType.LAZY)
    private Car car;

}
